package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.jooq.JSONB;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceCallLog;

/**
 * @author roc
 * @since 2025/12/31 17:35
 */
public record InterfaceCallLogCreateDto(
        @Schema(description = "被调接口ID")
                @Positive(message = "接口ID必须是正整数") @NotEmpty(message = "接口ID不能为空") Long apiId,
        @Schema(description = "接口版本ID")
                @Positive(message = "接口版本ID必须是正整数") @NotEmpty(message = "接口版本ID不能为空") Long versionId,
        @Schema(description = "调用方") @NotEmpty(message = "调用方不能为空") String caller,
        @Schema(description = "请求JSON") JSONB requestData,
        @Schema(description = "响应JSON") JSONB responseData,
        @Schema(description = "响应状态码")
                @Positive(message = "响应状态码必须是正整数") @NotEmpty(message = "响应状态码不能为空") Integer statusCode,
        @Schema(description = "是否调用成功") @NotEmpty(message = "是否调用成功不能为空") Boolean success,
        @Schema(description = "请求耗时（ms）") @Positive(message = "请求耗时必须是正整数") Integer durationMs) {
    public ApiInterfaceCallLog toEntity() {
        return new ApiInterfaceCallLog()
                .setApiId(apiId)
                .setVersionId(versionId)
                .setCaller(caller)
                .setRequestData(requestData)
                .setResponseData(responseData)
                .setStatusCode(statusCode)
                .setSuccess(success)
                .setDurationMs(durationMs);
    }
}
