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
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
            return requestForbidden(exchange, requestId);
        }

        // todo@lp 用户是否可以调用api
        // todo@lp 接口是否存在


        return processRequest(exchange, chain, requestId, originalRequest);
    }

    private Mono<Void> processRequest(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            String requestId,
            ServerHttpRequest request) {
        RequestHeaders headers = extractHeaders(request);
        boolean hasBody = isRequestBodyMethod(request.getMethod().name());

        log.debug(
                "[{}] Processing {} request (hasBody={})", requestId, request.getMethod(), hasBody);

        if (hasBody) {
            return processWithBody(exchange, chain, requestId, headers);
        } else {
            return processWithoutBody(exchange, chain, requestId, headers);
        }
    }

    private Mono<Void> processWithBody(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            String requestId,
            RequestHeaders headers) {
        if ("0".equals(headers.contentLength)) {
            log.warn("[{}] [EMPTY-CONTENT] POST request with Content-Length=0", requestId);
            return requestForbidden(exchange, requestId);
        }

        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(
                        dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);

                            String bodyJson = new String(bytes, StandardCharsets.UTF_8);
                            log.info("[{}] [HAS-BODY] Request body: {}", requestId, bodyJson);

                            return authenticateAndForward(
                                    headers, bodyJson, exchange, chain, requestId, bytes);
                        })
                .doOnError(error -> log.error("[{}] ERROR in processWithBody", requestId, error));
    }

    private Mono<Void> processWithoutBody(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            String requestId,
            RequestHeaders headers) {
        return authenticateAndForward(headers, "", exchange, chain, requestId, null);
    }

    private Mono<Void> authenticateAndForward(
            RequestHeaders headers,
            String bodyJson,
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            String requestId,
            byte[] bodyBytes) {
        return nonceService
                .verifyAndRecordNonce(headers.nonce)
                .flatMap(
                        nonceValid -> {
                            if (!nonceValid) {
                                log.warn(
                                        "[{}] Duplicate nonce detected: {}",
                                        requestId,
                                        headers.nonce);
                                return requestForbidden(exchange, requestId);
                            }

                            ApiResponse authResult =
                                    authenticateInternal(headers, bodyJson, requestId);

                            if (!authResult.isSuccess()) {
                                log.warn(
                                        "[{}] Authentication failed: {}",
                                        requestId,
                                        authResult.getMessage());
                                return requestForbidden(exchange, requestId);
                            }

                            // todo@lp 接口调用次数统计
                            log.info(
                                    "[{}] Authentication SUCCESS, forwarding to downstream",
                                    requestId);

                            // todo@lp 响应日志记录，获取response?

                            if (bodyBytes != null) {
                                ServerHttpRequest mutatedRequest =
                                        new ServerHttpRequestDecorator(exchange.getRequest()) {
                                            @Override
                                            @NonNull public Flux<DataBuffer> getBody() {
                                                return Flux.just(
                                                        exchange.getResponse()
                                                                .bufferFactory()
                                                                .wrap(bodyBytes));
                                            }
                                        };
                                return chain.filter(
                                        exchange.mutate().request(mutatedRequest).build());
                            } else {
                                return chain.filter(exchange);
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
                "456");
    }

    private ApiResponse authenticateInternal(
            RequestHeaders headers, String bodyJson, String requestId) {
        log.debug(
                "[{}] Authenticating: accessKey={}, nonce={}, timestamp={}, bodyLength={}",
                requestId,
                headers.accessKey,
                headers.nonce,
                headers.timestamp,
                bodyJson.length());

        if (headers.timestamp == null || headers.timestamp.isEmpty()) {
            log.warn("[{}] Timestamp is null or empty", requestId);
            return ApiResponse.fail("Header: timestamp must not be null or empty");
        }

        long requestTime = Long.parseLong(headers.timestamp);
        long timeDiff = System.currentTimeMillis() - requestTime;
        if (timeDiff > ApiSignConstant.REQUEST_VALID_MINUTES * 60 * 1000) {
            log.warn(
                    "[{}] Request expired: {}ms > {}ms",
                    requestId,
                    timeDiff,
                    ApiSignConstant.REQUEST_VALID_MINUTES * 60 * 1000);
            return ApiResponse.fail(
                    "request was expired, more than "
                            + ApiSignConstant.REQUEST_VALID_MINUTES
                            + " minutes");
        }

        return verifySignature(
                headers.accessKey,
                headers.secretKey,
                headers.sign,
                headers.nonce,
                headers.timestamp,
                bodyJson,
                requestId);
    }

    private ApiResponse verifySignature(
            String accessKey,
            String secretKey,
            String signature,
            String nonce,
            String timestamp,
            String bodyJson,
            String requestId) {
        byte[] genned = SignUtil.genSignBySha512(accessKey, nonce, timestamp, secretKey, bodyJson);
        if (genned == null || genned.length == 0) {
            log.error("[{}] Failed to generate signature", requestId);
            return ApiResponse.fail("无法生成签名");
        }
        String expectedSign = Arrays.toString(genned);
        if (!expectedSign.equals(signature)) {
            log.warn(
                    "[{}] Signature mismatch: expected={}, got={}",
                    requestId,
                    expectedSign,
                    signature);
            return ApiResponse.fail("验签失败");
        }
        log.debug("[{}] Signature verified successfully", requestId);
        return ApiResponse.success(null);
    }

    private Mono<Void> requestForbidden(ServerWebExchange exchange, String requestId) {
        log.warn("[{}] Returning FORBIDDEN (403)", requestId);
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    private void logRequest(ServerHttpRequest request, String requestId) {
        log.info(
                "[{}] Request: {} {} from {}",
                requestId,
                request.getMethod(),
                request.getURI().getPath(),
                IPUtil.getClientIp(request));
    }

    private boolean isRequestBodyMethod(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

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
            String secretKey) {}
}
