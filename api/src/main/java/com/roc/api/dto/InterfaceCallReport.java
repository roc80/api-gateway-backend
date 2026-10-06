package com.roc.api.dto;

import java.io.Serializable;

/**
 * 网关侧接口调用埋点（RPC 传输对象）
 *
 * @author roc
 * @since 2026/10/6
 */
public class InterfaceCallReport implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long apiId;
    private Long versionId;
    private String caller;
    private String requestData;
    private String responseData;
    private Integer statusCode;
    private Boolean success;
    private Integer durationMs;

    public InterfaceCallReport() {}

    public InterfaceCallReport(
            Long apiId,
            Long versionId,
            String caller,
            String requestData,
            String responseData,
            Integer statusCode,
            Boolean success,
            Integer durationMs) {
        this.apiId = apiId;
        this.versionId = versionId;
        this.caller = caller;
        this.requestData = requestData;
        this.responseData = responseData;
        this.statusCode = statusCode;
        this.success = success;
        this.durationMs = durationMs;
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

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }

    @Override
    public String toString() {
        // 不输出请求/响应体，避免大对象与敏感数据进入日志
        return "InterfaceCallReport(apiId="
                + apiId
                + ", versionId="
                + versionId
                + ", caller="
                + caller
                + ", statusCode="
                + statusCode
                + ", success="
                + success
                + ", durationMs="
                + durationMs
                + ")";
    }
}
