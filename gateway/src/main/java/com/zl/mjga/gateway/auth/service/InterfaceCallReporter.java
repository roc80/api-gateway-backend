package com.zl.mjga.gateway.auth.service;

import com.roc.api.dto.InterfaceCallReport;
import com.roc.api.service.InterfaceCallReportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 接口调用埋点上报方：经 Dubbo 调用 backend 的 {@link InterfaceCallReportService} 落库， 阻塞 RPC 包装为 boundedElastic
 * 上的 Mono；fire-and-forget，失败仅记录日志，不影响网关响应
 *
 * @author roc
 * @since 2026/10/6
 */
@Component
@Slf4j
public class InterfaceCallReporter {

    @DubboReference(scope = "remote", timeout = 3000)
    private InterfaceCallReportService interfaceCallReportService;

    /**
     * 上报一次接口调用埋点
     *
     * @param report 调用埋点
     * @return 完成信号；上报失败时以 empty 终结并记录日志
     */
    public Mono<Void> report(InterfaceCallReport report) {
        return Mono.fromCallable(
                        () -> {
                            interfaceCallReportService.reportCall(report);
                            return true;
                        })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(
                        error -> {
                            log.warn(
                                    "Failed to report interface call: {}, cause: {}",
                                    report,
                                    error.getMessage());
                            return Mono.empty();
                        })
                .then();
    }
}
