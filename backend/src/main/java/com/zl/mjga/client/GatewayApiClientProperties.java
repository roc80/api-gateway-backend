package com.zl.mjga.client;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * api.client 前缀配置，指向平台网关
 *
 * @author roc
 * @since 2026/10/2
 */
@ConfigurationProperties(prefix = "api.client")
public record GatewayApiClientProperties(
        @DefaultValue("http://localhost:9090") String gatewayBaseUrl,
        @DefaultValue("3s") Duration connectTimeout,
        @DefaultValue("10s") Duration readTimeout) {}
