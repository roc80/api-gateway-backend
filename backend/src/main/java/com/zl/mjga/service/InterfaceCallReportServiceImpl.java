package com.zl.mjga.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roc.api.dto.InterfaceCallReport;
import com.roc.api.service.InterfaceCallReportService;
import com.zl.mjga.dto.api.InterfaceCallLogCreateDto;
import com.zl.mjga.repository.api.InterfaceCallLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 网关侧接口调用埋点上报 RPC 实现：把网关转发的每次调用写入 api_interface_call_log， 作为接口/用户调用统计分析的唯一数据源
 *
 * @author roc
 * @since 2026/10/6
 */
@DubboService
@Slf4j
public class InterfaceCallReportServiceImpl implements InterfaceCallReportService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final InterfaceCallLogRepository interfaceCallLogRepository;

    public InterfaceCallReportServiceImpl(InterfaceCallLogRepository interfaceCallLogRepository) {
        this.interfaceCallLogRepository = interfaceCallLogRepository;
    }

    @Override
    public void reportCall(InterfaceCallReport report) {
        if (report == null || report.getApiId() == null || report.getVersionId() == null) {
            log.warn("Ignore invalid interface call report: {}", report);
            return;
        }
        InterfaceCallLogCreateDto callLog =
                new InterfaceCallLogCreateDto(
                        report.getApiId(),
                        report.getVersionId(),
                        StringUtils.defaultIfBlank(report.getCaller(), "anonymous"),
                        jsonOrNull(report.getRequestData()),
                        jsonOrNull(report.getResponseData()),
                        report.getStatusCode(),
                        report.getSuccess(),
                        report.getDurationMs() == null ? 0 : report.getDurationMs());
        interfaceCallLogRepository.insert(callLog.toEntity());
        log.debug(
                "Interface call report persisted: apiId={}, caller={}, statusCode={}",
                report.getApiId(),
                report.getCaller(),
                report.getStatusCode());
    }

    /** 仅当内容是合法 JSON 时保留（存入 JSONB 列），空白或非法 JSON（如纯文本响应）存为 null，避免整条埋点落库失败 */
    private static String jsonOrNull(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            OBJECT_MAPPER.readTree(value);
            return value;
        } catch (Exception e) {
            return null;
        }
    }
}
