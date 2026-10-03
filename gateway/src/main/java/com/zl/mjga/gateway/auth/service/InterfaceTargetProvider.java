package com.zl.mjga.gateway.auth.service;

import com.roc.api.dto.InvokeTargetInfo;
import com.roc.api.service.InterfaceQueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 接口目标提供方：经 Dubbo 查询 backend 的 {@link InterfaceQueryService}， 校验请求对应的接口已注册、已审核启用且允许调用；阻塞 RPC 包装为
 * boundedElastic 上的 Mono
 *
 * @author roc
 * @since 2026/10/3
 */
@Component
@Slf4j
public class InterfaceTargetProvider {

    @DubboReference(scope = "remote", timeout = 3000)
    private InterfaceQueryService interfaceQueryService;

    /**
     * 查询可调用的接口目标
     *
     * @param httpMethod HTTP 方法
     * @param path 请求路径
     * @return 目标信息；接口不存在或不可调用时为空 Mono
     */
    public Mono<InvokeTargetInfo> getInvokeTarget(String httpMethod, String path) {
        return Mono.fromCallable(() -> interfaceQueryService.getInvokeTarget(httpMethod, path))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
