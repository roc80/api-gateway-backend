package com.zl.mjga.gateway.auth.service;

import com.roc.api.dto.UserAuthInfo;
import com.roc.api.service.UserAuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 调用方认证信息提供方：经 Dubbo 查询 backend 的 {@link UserAuthService}， 阻塞 RPC 调用包装为 Mono 并切换到
 * boundedElastic，避免阻塞网关事件循环线程
 *
 * @author roc
 * @since 2026/10/2
 */
@Component
@Slf4j
public class UserAuthProvider {

    @DubboReference(scope = "remote", timeout = 3000)
    private UserAuthService userAuthService;

    /**
     * 按 accessKey 查询调用方认证信息
     *
     * @param accessKey 请求头中的用户 accessKey
     * @return 认证信息；accessKey 不存在时为空 Mono
     */
    public Mono<UserAuthInfo> getAuthByAccessKey(String accessKey) {
        return Mono.fromCallable(() -> userAuthService.getAuthByAccessKey(accessKey))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
