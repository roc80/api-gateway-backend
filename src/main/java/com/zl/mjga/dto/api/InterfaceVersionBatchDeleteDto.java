package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * @author lipeng
 * @since 2026/1/4 11:13
 */
@Schema(description = "批量删除接口版本请求")
public record InterfaceVersionBatchDeleteDto(
        @Schema(
                        description = "要删除的接口版本ID列表",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "[1, 2, 3]")
                @NotEmpty(message = "ID列表不能为空") List<@Positive(message = "ID必须为正整数") Long> ids) {}
