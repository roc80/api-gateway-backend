package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.jooq.JSONB;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceVersion;

/**
 * @author roc
 * @since 2025/12/31 17:35
 */
public record InterfaceVersionCreateDto(
        @Schema(description = "接口id", requiredMode = Schema.RequiredMode.REQUIRED) @Positive Long apiId,
        @Schema(description = "接口版本", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "接口版本不能为空") String version,
        @Schema(description = "是否是当前版本") Boolean current,
        @Schema(description = "HTTP请求方法", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "HTTP请求方法不能为空") String httpMethod,
        @Schema(description = "HTTP请求路径", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "HTTP请求路径不能为空") String path,
        @Schema(description = "HTTP请求头") JSONB requestHeaders,
        @Schema(description = "HTTP请求参数") JSONB requestParams,
        @Schema(description = "HTTP请求体") JSONB requestBody,
        @Schema(description = "HTTP响应体") JSONB responseBody,
        @Schema(description = "HTTP响应示例") JSONB responseExample,
        @Schema(description = "CURL请求示例") String exampleCurl,
        @Schema(description = "代码示例") JSONB exampleCode,
        @Schema(description = "认证方式") String authType) {
    public ApiInterfaceVersion toEntity() {
        return new ApiInterfaceVersion()
                .setApiId(apiId)
                .setVersion(version)
                .setIsCurrent(current)
                .setHttpMethod(httpMethod)
                .setPath(path)
                .setRequestHeaders(requestHeaders)
                .setRequestParams(requestParams)
                .setRequestBody(requestBody)
                .setResponseBody(responseBody)
                .setResponseExample(responseExample)
                .setExampleCurl(exampleCurl)
                .setAllowInvoke(true)
                .setExampleCode(exampleCode)
                .setAuthType(authType);
    }
}
