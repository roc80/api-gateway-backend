package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/**
 * 接口调用统计总览
 *
 * @author roc
 * @since 2026/10/6
 */
public record InterfaceStatsOverviewDto(
        @Schema(description = "统计时间范围起点") OffsetDateTime startTime,
        @Schema(description = "统计时间范围终点") OffsetDateTime endTime,
        @Schema(description = "总调用次数") long totalCalls,
        @Schema(description = "成功调用次数") long successCalls,
        @Schema(description = "失败调用次数") long failCalls,
        @Schema(description = "成功率（0~1）") double successRate,
        @Schema(description = "平均耗时（ms）") long avgDurationMs,
        @Schema(description = "最大耗时（ms）") long maxDurationMs,
        @Schema(description = "活跃调用方数量") long callerCount,
        @Schema(description = "被调用接口数量") long interfaceCount) {}
