package com.zl.mjga.client;

import com.roc.contract.SignUtil;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 平台内部使用的网关调用客户端
 *
 * <p>替代原 demo 版 api-client-sdk 的 ApiClient：网关地址可配置、请求路径不写死、带连接与读取超时。 签名协议与 api-client-sdk 逐位一致，由
 * api-contract 模块提供权威实现。
 *
 * @author roc
 * @since 2026/10/2
 */
@Component
@Slf4j
public class GatewayApiClient {

    private final RestClient restClient;

    public GatewayApiClient(
            GatewayApiClientProperties properties, RestClient.Builder restClientBuilder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        requestFactory.setReadTimeout((int) properties.readTimeout().toMillis());
        this.restClient =
                restClientBuilder
                        .baseUrl(properties.gatewayBaseUrl())
                        .requestFactory(requestFactory)
                        .build();
    }

    /**
     * 以用户身份经由网关 POST 调用一个平台 API
     *
     * @param apiPath API 路径，如 /api/name
     * @param bodyJson 请求体 JSON 字符串（同时参与签名）
     * @param accessKey 用户 accessKey
     * @param secretKey 用户 secretKey
     * @return 下游响应体
     */
    public String postForBody(String apiPath, String bodyJson, String accessKey, String secretKey) {
        return exchange(HttpMethod.POST, apiPath, bodyJson, accessKey, secretKey);
    }

    /**
     * 以用户身份经由网关 GET 调用一个平台 API（签名按空请求体计算，与网关无体请求的验签口径一致）
     *
     * @param apiPath API 路径，如 /api/echo
     * @param accessKey 用户 accessKey
     * @param secretKey 用户 secretKey
     * @return 下游响应体
     */
    public String getForBody(String apiPath, String accessKey, String secretKey) {
        return exchange(HttpMethod.GET, apiPath, "", accessKey, secretKey);
    }

    private String exchange(
            HttpMethod httpMethod,
            String apiPath,
            String bodyJson,
            String accessKey,
            String secretKey) {
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String sign = SignUtil.genSignString(accessKey, nonce, timestamp, secretKey, bodyJson);
        log.debug("Invoke gateway api {} {} with accessKey {}", httpMethod, apiPath, accessKey);
        RestClient.RequestBodySpec spec =
                restClient
                        .method(httpMethod)
                        .uri(apiPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("access-key", accessKey)
                        .header("nonce", nonce)
                        .header("timestamp", timestamp)
                        .header("sign", sign);
        if (!bodyJson.isEmpty()) {
            spec = spec.body(bodyJson);
        }
        return spec.retrieve().body(String.class);
    }
}
