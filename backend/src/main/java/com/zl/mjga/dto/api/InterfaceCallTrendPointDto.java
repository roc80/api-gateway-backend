package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/**
 * 接口调用趋势上的一段时间桶（按天或按小时聚合，空档补零）
 *
 * @author roc
 * @since 2026/10/6
 */
public record InterfaceCallTrendPointDto(
        @Schema(description = "时间桶起点") OffsetDateTime time,
        @Schema(description = "该时段总调用次数") long totalCalls,
        @Schema(description = "该时段成功调用次数") long successCalls,
        @Schema(description = "该时段失败调用次数") long failCalls,
        @Schema(description = "该时段成功率（0~1）") double successRate,
        @Schema(description = "该时段平均耗时（ms）") long avgDurationMs) {}
