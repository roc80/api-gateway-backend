package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户调用量排行项（按调用量降序）
 *
 * @author roc
 * @since 2026/10/6
 */
public record InterfaceUserRankDto(
        @Schema(description = "调用方用户名") String caller,
        @Schema(description = "总调用次数") long totalCalls,
        @Schema(description = "成功调用次数") long successCalls,
        @Schema(description = "失败调用次数") long failCalls,
        @Schema(description = "成功率（0~1）") double successRate,
        @Schema(description = "平均耗时（ms）") long avgDurationMs) {}
