package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.NonNull;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceCallLog;

/**
 * @author roc
 * @since 2025/12/31 17:34
 */
public record InterfaceCallLogDto(
        Long id,
        @Schema(description = "被调用的接口ID") Long apiId,
        @Schema(description = "该接口的版本ID") Long versionId,
        @Schema(description = "调用者") String caller,
        @Schema(description = "请求JSON") String requestData,
        @Schema(description = "响应JSON") String responseData,
        @Schema(description = "响应状态码") Integer statusCode,
        @Schema(description = "是否调用成功") Boolean success,
        @Schema(description = "请求耗时（ms）") Integer durationMs,
        @Schema(description = "调用日志创建时间") OffsetDateTime createTime) {
    public static InterfaceCallLogDto fromEntity(@NonNull ApiInterfaceCallLog entity) {
        return new InterfaceCallLogDto(
                entity.getId(),
                entity.getApiId(),
                entity.getVersionId(),
                entity.getCaller(),
                jsonBToString(entity.getRequestData()),
                jsonBToString(entity.getResponseData()),
                entity.getStatusCode(),
                entity.getSuccess(),
                entity.getDurationMs(),
                entity.getCreateTime());
    }

    private static String jsonBToString(Object jsonB) {
        if (jsonB == null) {
            return null;
        }
        if (jsonB instanceof org.jooq.JSONB) {
            Object data = ((org.jooq.JSONB) jsonB).data();
            return data != null ? data.toString() : null;
        }
        return jsonB.toString();
    }
}
