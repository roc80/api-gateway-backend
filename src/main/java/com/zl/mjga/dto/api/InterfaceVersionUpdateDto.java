package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.function.BiConsumer;
import org.jooq.JSONB;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceVersion;

/**
 * @author roc
 * @since 2026/1/4 10:25
 */
public record InterfaceVersionUpdateDto(
        @Schema(description = "是否是当前版本") Boolean current,
        @Schema(description = "HTTP请求方法") String httpMethod,
        @Schema(description = "HTTP请求路径") String path,
        @Schema(description = "HTTP请求头") String requestHeaders,
        @Schema(description = "HTTP请求参数") String requestParams,
        @Schema(description = "HTTP请求体") String requestBody,
        @Schema(description = "HTTP响应体") String responseBody,
        @Schema(description = "HTTP响应示例") String responseExample,
        @Schema(description = "CURL请求示例") String exampleCurl,
        @Schema(description = "代码示例") String exampleCode,
        @Schema(description = "认证方式") String authType,
        @Schema(description = "是否允许调用") Boolean allowInvoke) {
    private static <T> void setIfNotNull(
            T value, BiConsumer<ApiInterfaceVersion, T> setter, ApiInterfaceVersion entity) {
        if (value != null) {
            setter.accept(entity, value);
        }
    }

    private static void setJsonBIfNotNull(
            String value, BiConsumer<ApiInterfaceVersion, JSONB> setter, ApiInterfaceVersion entity) {
        if (value != null) {
            setter.accept(entity, JSONB.jsonb(value));
        }
    }

    public void applyTo(ApiInterfaceVersion entity) {
        setIfNotNull(current, ApiInterfaceVersion::setIsCurrent, entity);
        setIfNotNull(httpMethod, ApiInterfaceVersion::setHttpMethod, entity);
        setIfNotNull(path, ApiInterfaceVersion::setPath, entity);
        setJsonBIfNotNull(requestHeaders, ApiInterfaceVersion::setRequestHeaders, entity);
        setJsonBIfNotNull(requestParams, ApiInterfaceVersion::setRequestParams, entity);
        setJsonBIfNotNull(requestBody, ApiInterfaceVersion::setRequestBody, entity);

        setJsonBIfNotNull(responseBody, ApiInterfaceVersion::setResponseBody, entity);
        setJsonBIfNotNull(responseExample, ApiInterfaceVersion::setResponseExample, entity);
        setIfNotNull(exampleCurl, ApiInterfaceVersion::setExampleCurl, entity);
        setJsonBIfNotNull(exampleCode, ApiInterfaceVersion::setExampleCode, entity);
        setIfNotNull(authType, ApiInterfaceVersion::setAuthType, entity);
        setIfNotNull(allowInvoke, ApiInterfaceVersion::setAllowInvoke, entity);
    }
}
