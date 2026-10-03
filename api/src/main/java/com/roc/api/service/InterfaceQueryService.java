package com.roc.api.service;

import com.roc.api.dto.InvokeTargetInfo;

/**
 * 网关侧接口目标查询 RPC 契约，provider 为 backend，consumer 为 gateway
 *
 * @author roc
 * @since 2026/10/3
 */
public interface InterfaceQueryService {

    /**
     * 按 HTTP 方法与路径查询可调用的接口目标（接口启用、版本为当前版本且允许调用）
     *
     * @param httpMethod HTTP 方法，如 POST
     * @param path 请求路径，如 /api/name
     * @return 目标信息（apiId/versionId）；接口不存在、未启用或版本不允许调用时返回 null
     */
    InvokeTargetInfo getInvokeTarget(String httpMethod, String path);
}
