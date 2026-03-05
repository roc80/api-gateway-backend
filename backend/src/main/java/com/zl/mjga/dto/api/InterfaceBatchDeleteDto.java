package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * 批量删除请求
 *
 * @author roc
 * @since 2025/12/28
 */
@Schema(description = "批量删除请求")
public record InterfaceBatchDeleteDto(
        @Schema(
                        description = "要删除的接口ID列表",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "[1, 2, 3]")
                @NotEmpty(message = "ID列表不能为空") List<@Positive(message = "ID必须为正整数") Long> ids) {}
