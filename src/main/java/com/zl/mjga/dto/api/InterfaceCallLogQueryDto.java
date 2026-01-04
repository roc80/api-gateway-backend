package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/**
 * @author roc
 * @since 2025/12/31 17:44
 */
public record InterfaceCallLogQueryDto(
        @Schema(description = "被调用的接口ID") Long apiId,
        @Schema(description = "该接口的版本ID") Long versionId,
        @Schema(description = "调用者") String caller,
        @Schema(description = "响应状态码") Integer statusCode,
        @Schema(description = "是否调用成功") Boolean success,
        @Schema(description = "请求耗时（ms）") Integer durationMs,
        @Schema(description = "调用日志创建时间") OffsetDateTime createTime) {}
