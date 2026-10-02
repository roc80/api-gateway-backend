package com.zl.mjga.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.roc.contract.SignUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.zl.mjga.client.GatewayApiClient;
import com.zl.mjga.client.GatewayApiClientProperties;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

/**
 * 网关调用客户端测试：用本地临时 HttpServer 捕获真实请求，校验请求头签名与网关验签算法（api-contract）一致
 *
 * @author roc
 * @since 2026/10/2
 */
class GatewayApiClientTest {

    private static final String BODY_JSON = "{\"username\":\"dave\"}";
    private static final String ACCESS_KEY = "ak-user-01";
    private static final String SECRET_KEY = "sk-secret-01";

    private final AtomicReference<String> capturedAccessKey = new AtomicReference<>();
    private final AtomicReference<String> capturedNonce = new AtomicReference<>();
    private final AtomicReference<String> capturedTimestamp = new AtomicReference<>();
    private final AtomicReference<String> capturedSign = new AtomicReference<>();
    private final AtomicReference<String> capturedContentType = new AtomicReference<>();
    private final AtomicReference<String> capturedBody = new AtomicReference<>();

    private HttpServer server;
    private GatewayApiClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/api/name", this::handleMockName);
        server.start();
        String gatewayBaseUrl =
                "http://"
                        + server.getAddress().getAddress().getHostAddress()
                        + ":"
                        + server.getAddress().getPort();
        client =
                new GatewayApiClient(
                        new GatewayApiClientProperties(
                                gatewayBaseUrl, Duration.ofSeconds(3), Duration.ofSeconds(10)),
                        RestClient.builder());
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void postForBody_shouldReturnDownstreamBody() {
        assertThat(client.postForBody("/api/name", BODY_JSON, ACCESS_KEY, SECRET_KEY))
                .isEqualTo("\"dave\"");
    }

    @Test
    void postForBody_shouldSignRequestLikeGatewayExpects() {
        client.postForBody("/api/name", BODY_JSON, ACCESS_KEY, SECRET_KEY);

        assertThat(capturedSign.get())
                .isEqualTo(
                        SignUtil.genSignString(
                                ACCESS_KEY,
                                capturedNonce.get(),
                                capturedTimestamp.get(),
                                SECRET_KEY,
                                BODY_JSON));
    }

    @Test
    void postForBody_shouldForwardAccessKeyBodyAndContentType() {
        client.postForBody("/api/name", BODY_JSON, ACCESS_KEY, SECRET_KEY);

        assertThat(
                        new String[] {
                            capturedAccessKey.get(), capturedContentType.get(), capturedBody.get()
                        })
                .containsExactly(ACCESS_KEY, "application/json", BODY_JSON);
    }

    private void handleMockName(HttpExchange exchange) throws IOException {
        capturedAccessKey.set(exchange.getRequestHeaders().getFirst("access-key"));
        capturedNonce.set(exchange.getRequestHeaders().getFirst("nonce"));
        capturedTimestamp.set(exchange.getRequestHeaders().getFirst("timestamp"));
        capturedSign.set(exchange.getRequestHeaders().getFirst("sign"));
        capturedContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
        capturedBody.set(
                new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

        byte[] response = "\"dave\"".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
