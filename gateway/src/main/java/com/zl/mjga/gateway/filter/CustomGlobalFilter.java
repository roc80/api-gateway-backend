package com.zl.mjga.gateway.filter;

import com.roc.apiclientsdk.module.ApiResponse;
import com.roc.apiclientsdk.util.SignUtil;
import com.zl.mjga.gateway.auth.constant.ApiSignConstant;
import com.zl.mjga.gateway.auth.service.NonceService;
import com.zl.mjga.gateway.auth.util.IPUtil;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author roc
 */
@Order(-100)
@Component
@Slf4j
@AllArgsConstructor
public class CustomGlobalFilter implements GlobalFilter {

    private final NonceService nonceService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = java.util.UUID.randomUUID().toString().substring(0, 8);
        exchange.getAttributes().put("requestId", requestId);

        log.info("[{}] >>> CustomGlobalFilter.filter START", requestId);
        ServerHttpRequest originalRequest = exchange.getRequest();

        logRequest(originalRequest, requestId);
        if (!isInWhiteList(originalRequest)) {
            log.warn("[{}] IP NOT in whitelist, returning FORBIDDEN", requestId);
            return requestError(exchange, requestId, HttpStatus.FORBIDDEN);
        }

        // todo@lp 用户是否可以调用api
        // todo@lp 接口是否存在


        return processRequest(exchange, chain, requestId, originalRequest);
    }

    private Mono<Void> processRequest(ServerWebExchange exchange, GatewayFilterChain chain, String requestId,
                                      ServerHttpRequest request) {
        RequestHeaders headers = extractHeaders(request);
        boolean hasBody = isRequestBodyMethod(request.getMethod().name());
        if (hasBody) {
            return processWithBody(exchange, chain, requestId, headers);
        } else {
            return processWithoutBody(exchange, chain, requestId, headers);
        }
    }

    private Mono<Void> processWithBody(ServerWebExchange exchange, GatewayFilterChain chain, String requestId,
                                       RequestHeaders headers) {
        if ("0".equals(headers.contentLength)) {
            log.warn("[{}] [EMPTY-CONTENT] POST request with Content-Length=0", requestId);
            return requestError(exchange, requestId, HttpStatus.FORBIDDEN);
        }

        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(
                        dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);

                            String bodyJson = new String(bytes, StandardCharsets.UTF_8);
                            log.info("[{}] [HAS-BODY] Request body: {}", requestId, bodyJson);

                            return authenticateAndForward(headers, bodyJson, exchange, chain, requestId, bytes);
                        })
                .doOnError(error -> log.error("[{}] ERROR in processWithBody", requestId, error));
    }

    private Mono<Void> processWithoutBody(ServerWebExchange exchange, GatewayFilterChain chain, String requestId, RequestHeaders headers) {
        return authenticateAndForward(headers, "", exchange, chain, requestId, null);
    }

    private Mono<Void> authenticateAndForward(RequestHeaders headers, String bodyJson, ServerWebExchange exchange,
                                              GatewayFilterChain chain, String requestId, byte[] bodyBytes) {
        long requestTime = Long.parseLong(headers.timestamp);
        long timeDiff = System.currentTimeMillis() - requestTime;
        long expiredMs = ApiSignConstant.REQUEST_VALID_MINUTES * 60 * 1000;
        if (timeDiff > expiredMs) {
            log.warn("[{}] Request expired: {}ms > {}ms", requestId, timeDiff, expiredMs);
            return requestError(exchange, requestId, HttpStatus.REQUEST_TIMEOUT);
        }
        return nonceService.verifyAndRecordNonce(headers.nonce)
                .flatMap(
                        nonceValid -> {
                            if (!nonceValid) {
                                log.warn("[{}] Duplicate nonce detected: {}", requestId, headers.nonce);
                                return requestError(exchange, requestId, HttpStatus.FORBIDDEN);
                            }
                            ApiResponse authResult = verifySign(headers.accessKey, headers.secretKey, headers.sign,
                                    headers.nonce, headers.timestamp, bodyJson, requestId);
                            if (!authResult.isSuccess()) {
                                log.warn("[{}] Authentication failed: {}", requestId, authResult.getMessage());
                                return requestError(exchange, requestId, HttpStatus.FORBIDDEN);
                            }
                            log.info("[{}] Authentication SUCCESS, forwarding to downstream", requestId);

                            // 装饰响应对象，用于记录响应日志
                            ServerHttpResponse decoratedResponse = new ServerHttpResponseDecorator(
                                    exchange.getResponse()) {
                                @Override
                                @NonNull
                                public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
                                    if (body instanceof Flux<? extends DataBuffer> fluxBody) {
                                        return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                                            DataBufferFactory bufferFactory = bufferFactory();
                                            DataBuffer join = bufferFactory.join(dataBuffers);
                                            byte[] content = new byte[join.readableByteCount()];
                                            join.read(content);
                                            DataBufferUtils.release(join);
                                            String responseBody = new String(content, StandardCharsets.UTF_8);
                                            log.info("[{}] Response: status={}, body={}", requestId,
                                                    getStatusCode(), responseBody);
                                            // todo@lp 接口调用次数统计

                                            return bufferFactory.wrap(content);
                                        }));
                                    }
                                    return super.writeWith(body);
                                }
                            };

                            if (bodyBytes != null) {
                                // 防止下游读不到请求体
                                ServerHttpRequest decoratedRequest =
                                        new ServerHttpRequestDecorator(exchange.getRequest()) {
                                            @Override
                                            @NonNull
                                            public Flux<DataBuffer> getBody() {
                                                return Flux.just(
                                                        // response和request的BufferFactory共享
                                                        exchange.getResponse()
                                                                .bufferFactory()
                                                                .wrap(bodyBytes));
                                            }
                                        };
                                return chain.filter(exchange.mutate()
                                        .request(decoratedRequest)
                                        .response(decoratedResponse)
                                        .build());
                            } else {
                                return chain.filter(exchange.mutate()
                                        .response(decoratedResponse)
                                        .build());
                            }
                        });
    }

    private RequestHeaders extractHeaders(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return new RequestHeaders(
                headers.getFirst("nonce"),
                headers.getFirst("timestamp"),
                headers.getFirst("sign"),
                headers.getFirst("access-key"),
                headers.getFirst("Content-Length"),
                "456"); // todo@lp 根据ak查找sk
    }

    /**
     * 验签
     *
     * @param accessKey 用户ak
     * @param secretKey 用户sk
     * @param sign      请求头 sign 防篡改
     * @param nonce     请求头 nonce 防重放
     * @param timestamp 请求头 timestamp 防止nonce池过大，定义请求有效期
     * @param bodyJson  请求体 JSON字符串
     * @param requestId 请求id 用于请求traceId
     * @return 封装的验签结果
     */
    private ApiResponse verifySign(String accessKey, String secretKey, String sign, String nonce,
                                   String timestamp, String bodyJson, String requestId) {
        log.debug("[{}] Authenticating: accessKey={}, nonce={}, timestamp={}, bodyLength={}",
                requestId, accessKey, nonce, timestamp, bodyJson.length());
        if (timestamp == null || timestamp.isEmpty()) {
            log.warn("[{}] Timestamp is null or empty", requestId);
            return ApiResponse.fail("Header: timestamp must not be null or empty");
        }
        byte[] genned = SignUtil.genSignBySha512(accessKey, nonce, timestamp, secretKey, bodyJson);
        if (genned == null || genned.length == 0) {
            log.error("[{}] Failed to generate sign", requestId);
            return ApiResponse.fail("无法生成签名");
        }
        String expectedSign = Arrays.toString(genned);
        if (!expectedSign.equals(sign)) {
            log.warn(
                    "[{}] Sign mismatch: expected={}, got={}",
                    requestId,
                    expectedSign,
                    sign);
            return ApiResponse.fail("验签失败");
        }
        log.debug("[{}] Sign verified successfully", requestId);
        return ApiResponse.success(null);
    }

    private Mono<Void> requestError(ServerWebExchange exchange, String requestId, HttpStatus responseCode) {
        log.warn("[{}] Returning {}", requestId, responseCode);
        exchange.getResponse().setStatusCode(responseCode);
        return exchange.getResponse().setComplete();
    }

    /**
     * 记录请求日志
     */
    private void logRequest(ServerHttpRequest request, String requestId) {
        log.info("[{}] Request: {} {} from {}", requestId, request.getMethod(), request.getURI().getPath(),
                IPUtil.getClientIp(request));
    }

    private boolean isRequestBodyMethod(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

    /**
     * 请求源IP白名单校验
     */
    private boolean isInWhiteList(ServerHttpRequest request) {
        String clientIp = IPUtil.getClientIp(request);
        log.debug("Checking IP: {}", clientIp);
        List<String> ipWhitelist = Arrays.asList("127.0.0.1", "0:0:0:0:0:0:0:1", "::1");
        return ipWhitelist.contains(clientIp);
    }

    private record RequestHeaders(
            String nonce,
            String timestamp,
            String sign,
            String accessKey,
            String contentLength,
            String secretKey) {
    }
}
