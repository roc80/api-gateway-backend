package com.zl.mjga.gateway.auth.util;

import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * @author lipeng
 * @since 2026/3/5 17:08
 */
public class IPUtil {
    public static String getClientIp(ServerHttpRequest request) {
        // 1. 从 X-Forwarded-For 头获取（代理服务器转发）
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For 可能包含多个 IP，第一个是客户端真实 IP
            return xForwardedFor.split(",")[0].trim();
        }

        // 2. 从 X-Real-IP 头获取
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // 3. 从 RemoteAddress 获取
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }
}
