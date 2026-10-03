package com.roc.api.dto;

import java.io.Serializable;

/**
 * 网关可调用的接口目标信息（RPC 传输对象）
 *
 * @author roc
 * @since 2026/10/3
 */
public class InvokeTargetInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long apiId;
    private Long versionId;

    public InvokeTargetInfo() {}

    public InvokeTargetInfo(Long apiId, Long versionId) {
        this.apiId = apiId;
        this.versionId = versionId;
    }

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    @Override
    public String toString() {
        return "InvokeTargetInfo(apiId=" + apiId + ", versionId=" + versionId + ")";
    }
}
