package com.zl.mjga.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.roc.api.dto.UserAuthInfo;
import com.roc.contract.SignUtil;
import com.zl.mjga.gateway.auth.service.NonceService;
import com.zl.mjga.gateway.auth.service.UserAuthProvider;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关全局过滤器测试：覆盖 ak 查询、用户启用校验、防重放与验签的完整链路
 *
 * @author roc
 * @since 2026/10/2
 */
@ExtendWith(MockitoExtension.class)
class CustomGlobalFilterTest {

    private static final String ACCESS_KEY = "ak-user-01";
    private static final String SECRET_KEY = "sk-secret-01";
    private static final String NONCE = "9f3k2";
    private static final String BODY_JSON = "{\"username\":\"dave\"}";

    @Mock private NonceService nonceService;

    @Mock private UserAuthProvider userAuthProvider;

    @Mock private GatewayFilterChain chain;

    private CustomGlobalFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CustomGlobalFilter(nonceService, userAuthProvider);
    }

    @Test
    void givenValidSign_shouldForwardToDownstream() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        when(userAuthProvider.getAuthByAccessKey(ACCESS_KEY))
                .thenReturn(Mono.just(new UserAuthInfo("dave", SECRET_KEY, true)));
        when(nonceService.verifyAndRecordNonce(NONCE)).thenReturn(Mono.just(true));
        when(chain.filter(any())).thenReturn(Mono.empty());
        MockServerWebExchange exchange = exchange(timestamp, validSign(timestamp, SECRET_KEY));

        filter.filter(exchange, chain).block();

        // mock 的 chain 不写响应：成功路径的断言点是"已转发且未设置任何错误状态"
        assertThat(exchange.getResponse().getStatusCode()).isNull();
        verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    void givenWrongSecretKey_shouldReturnForbidden() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        when(userAuthProvider.getAuthByAccessKey(ACCESS_KEY))
                .thenReturn(Mono.just(new UserAuthInfo("dave", SECRET_KEY, true)));
        when(nonceService.verifyAndRecordNonce(NONCE)).thenReturn(Mono.just(true));
        MockServerWebExchange exchange = exchange(timestamp, validSign(timestamp, "sk-other"));

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(chain, never()).filter(any());
    }

    @Test
    void givenUnknownAccessKey_shouldReturnForbidden() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        when(userAuthProvider.getAuthByAccessKey(ACCESS_KEY)).thenReturn(Mono.empty());
        MockServerWebExchange exchange = exchange(timestamp, validSign(timestamp, SECRET_KEY));

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(chain, never()).filter(any());
    }

    @Test
    void givenDisabledUser_shouldReturnForbidden() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        when(userAuthProvider.getAuthByAccessKey(ACCESS_KEY))
                .thenReturn(Mono.just(new UserAuthInfo("dave", SECRET_KEY, false)));
        MockServerWebExchange exchange = exchange(timestamp, validSign(timestamp, SECRET_KEY));

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(chain, never()).filter(any());
    }

    @Test
    void givenExpiredTimestamp_shouldReturnRequestTimeout() {
        String expiredTimestamp = String.valueOf(System.currentTimeMillis() - 10 * 60 * 1000L);
        MockServerWebExchange exchange =
                exchange(expiredTimestamp, validSign(expiredTimestamp, SECRET_KEY));

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.REQUEST_TIMEOUT);
        verify(chain, never()).filter(any());
    }

    @Test
    void givenDuplicateNonce_shouldReturnForbidden() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        when(userAuthProvider.getAuthByAccessKey(ACCESS_KEY))
                .thenReturn(Mono.just(new UserAuthInfo("dave", SECRET_KEY, true)));
        when(nonceService.verifyAndRecordNonce(NONCE)).thenReturn(Mono.just(false));
        MockServerWebExchange exchange = exchange(timestamp, validSign(timestamp, SECRET_KEY));

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(chain, never()).filter(any());
    }

    private MockServerWebExchange exchange(String timestamp, String sign) {
        MockServerHttpRequest request =
                MockServerHttpRequest.post("http://localhost:9090/api/name")
                        .remoteAddress(new InetSocketAddress("127.0.0.1", 50000))
                        .header("access-key", ACCESS_KEY)
                        .header("nonce", NONCE)
                        .header("timestamp", timestamp)
                        .header("sign", sign)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BODY_JSON);
        return MockServerWebExchange.from(request);
    }

    private String validSign(String timestamp, String secretKey) {
        return SignUtil.genSignString(ACCESS_KEY, NONCE, timestamp, secretKey, BODY_JSON);
    }
}
