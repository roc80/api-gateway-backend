package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
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
        @Schema(description = "请求头") String requestHeaders,
        @Schema(description = "请求参数") String requestParams,
        @Schema(description = "请求体") String requestBody,
        @Schema(description = "响应体") String responseBody,
        @Schema(description = "响应示例") String responseExample,
        @Schema(description = "示例curl") String exampleCurl,
        @Schema(description = "示例代码") String exampleCode,
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
                jsonBToString(entity.getRequestHeaders()),
                jsonBToString(entity.getRequestParams()),
                jsonBToString(entity.getRequestBody()),
                jsonBToString(entity.getResponseBody()),
                jsonBToString(entity.getResponseExample()),
                entity.getExampleCurl(),
                jsonBToString(entity.getExampleCode()),
                entity.getAuthType(),
                entity.getAllowInvoke(),
                entity.getCreateTime(),
                entity.getUpdateTime());
    }

    private static String jsonBToString(Object jsonB) {
        if (jsonB == null) {
            return null;
        }
        // JSONB.data() 返回底层数据（String 或其他类型）
        if (jsonB instanceof org.jooq.JSONB) {
            Object data = ((org.jooq.JSONB) jsonB).data();
            return data != null ? data.toString() : null;
        }
        return jsonB.toString();
    }
}
