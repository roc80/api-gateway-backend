package com.roc.api.service;

import com.roc.api.dto.InterfaceCallReport;

/**
 * 网关侧接口调用埋点上报 RPC 契约，provider 为 backend，consumer 为 gateway
 *
 * @author roc
 * @since 2026/10/6
 */
public interface InterfaceCallReportService {

    /**
     * 上报一次经网关转发的接口调用（fire-and-forget，provider 侧失败不应影响网关响应）
     *
     * @param report 调用埋点（apiId/versionId/caller/状态码/成功标记/耗时等）
     */
    void reportCall(InterfaceCallReport report);
}
