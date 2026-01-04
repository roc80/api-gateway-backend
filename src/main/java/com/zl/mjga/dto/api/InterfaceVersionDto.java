package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import org.jooq.JSONB;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceVersion;

/**
 * @author roc
 * @since 2025/12/31 17:34
 */
public record InterfaceVersionDto(
        Long id,
        @Schema(description = "接口id") Long apiId,
        @Schema(description = "接口版本") String version,
        @Schema(description = "是否是当前版本") Boolean current,
        @Schema(description = "HTTP方法") String httpMethod,
        @Schema(description = "接口路径") String path,
        @Schema(description = "请求头") JSONB requestHeaders,
        @Schema(description = "请求参数") JSONB requestParams,
        @Schema(description = "请求体") JSONB requestBody,
        @Schema(description = "响应体") JSONB responseBody,
        @Schema(description = "响应示例") JSONB responseExample,
        @Schema(description = "示例curl") String exampleCurl,
        @Schema(description = "示例代码") JSONB exampleCode,
        @Schema(description = "认证类型") String authType,
        @Schema(description = "是否允许调用") Boolean allowInvoke,
        @Schema(description = "创建时间") OffsetDateTime createTime,
        @Schema(description = "更新时间") OffsetDateTime updateTime) {
    public static InterfaceVersionDto fromEntity(ApiInterfaceVersion entity) {
        return new InterfaceVersionDto(
                entity.getId(),
                entity.getApiId(),
                entity.getVersion(),
                entity.getIsCurrent(),
                entity.getHttpMethod(),
                entity.getPath(),
                entity.getRequestHeaders(),
                entity.getRequestParams(),
                entity.getRequestBody(),
                entity.getResponseBody(),
                entity.getResponseExample(),
                entity.getExampleCurl(),
                entity.getExampleCode(),
                entity.getAuthType(),
                entity.getAllowInvoke(),
                entity.getCreateTime(),
                entity.getUpdateTime());
    }
}
