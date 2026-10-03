package com.roc.api.service;

import com.roc.api.dto.UserAuthInfo;

/**
 * 网关侧调用方认证查询 RPC 契约，provider 为 backend，consumer 为 gateway
 *
 * @author roc
 * @since 2026/10/2
 */
public interface UserAuthService {

    /**
     * 按 accessKey 查询调用方认证信息
     *
     * @param accessKey 用户 accessKey
     * @return 认证信息（含 secretKey 与启用状态）；accessKey 不存在或为空时返回 null
     */
    UserAuthInfo getAuthByAccessKey(String accessKey);
}
