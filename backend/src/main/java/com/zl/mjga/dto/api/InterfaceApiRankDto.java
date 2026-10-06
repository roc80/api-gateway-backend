package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 接口调用排行项（按调用量降序）
 *
 * @author roc
 * @since 2026/10/6
 */
public record InterfaceApiRankDto(
        @Schema(description = "接口ID") Long apiId,
        @Schema(description = "接口名称") String name,
        @Schema(description = "接口唯一标识") String code,
        @Schema(description = "接口分类") String category,
        @Schema(description = "总调用次数") long totalCalls,
        @Schema(description = "成功调用次数") long successCalls,
        @Schema(description = "失败调用次数") long failCalls,
        @Schema(description = "成功率（0~1）") double successRate,
        @Schema(description = "平均耗时（ms）") long avgDurationMs) {}
