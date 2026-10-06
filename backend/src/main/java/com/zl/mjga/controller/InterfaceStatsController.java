package com.zl.mjga.controller;

import com.zl.mjga.dto.api.InterfaceApiRankDto;
import com.zl.mjga.dto.api.InterfaceCallTrendPointDto;
import com.zl.mjga.dto.api.InterfaceStatsOverviewDto;
import com.zl.mjga.dto.api.InterfaceUserRankDto;
import com.zl.mjga.dto.api.TrendGranularity;
import com.zl.mjga.service.InterfaceStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接口调用统计分析控制器：为前端图表提供总览指标、时间趋势与排行榜数据
 *
 * @author roc
 * @since 2026/10/6
 */
@RestController
@RequestMapping("/interfaces/stats")
@RequiredArgsConstructor
@Validated
public class InterfaceStatsController {

    private final InterfaceStatsService interfaceStatsService;

    /** 调用统计总览 */
    @Operation(summary = "接口调用统计总览", description = "返回时间范围内的调用量、成功率、耗时与活跃度指标")
    @GetMapping("/overview")
    public InterfaceStatsOverviewDto overview(
            @Parameter(description = "统计起始时间（ISO-8601，缺省为结束时间前7天）")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime startTime,
            @Parameter(description = "统计结束时间（ISO-8601，缺省为当前时间）")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime endTime) {
        return interfaceStatsService.overview(startTime, endTime);
    }

    /** 调用趋势（按天/小时分桶，空档补零） */
    @Operation(summary = "接口调用趋势", description = "按天或小时分桶返回调用量/成功数/耗时序列，空档补零，可直接用于图表")
    @GetMapping("/trend")
    public List<InterfaceCallTrendPointDto> trend(
            @Parameter(description = "统计起始时间（ISO-8601，缺省为结束时间前7天）")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime startTime,
            @Parameter(description = "统计结束时间（ISO-8601，缺省为当前时间）")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime endTime,
            @Parameter(description = "统计粒度：day / hour，缺省 day")
                    @RequestParam(required = false, defaultValue = "day")
                    String granularity) {
        return interfaceStatsService.trend(
                startTime, endTime, TrendGranularity.fromString(granularity));
    }

    /** 接口调用量排行 */
    @Operation(summary = "接口调用量排行", description = "返回时间范围内调用量最高的接口列表（含成功率和平均耗时）")
    @GetMapping("/top-interfaces")
    public List<InterfaceApiRankDto> topInterfaces(
            @Parameter(description = "统计起始时间（ISO-8601，缺省为结束时间前7天）")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime startTime,
            @Parameter(description = "统计结束时间（ISO-8601，缺省为当前时间）")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime endTime,
            @Parameter(description = "返回条数，缺省 10，最大 100") @RequestParam(required = false)
                    Integer limit) {
        return interfaceStatsService.topInterfaces(startTime, endTime, limit);
    }

    /** 用户调用量排行 */
    @Operation(summary = "用户调用量排行", description = "返回时间范围内调用量最高的用户列表（含成功率和平均耗时）")
    @GetMapping("/top-users")
    public List<InterfaceUserRankDto> topUsers(
            @Parameter(description = "统计起始时间（ISO-8601，缺省为结束时间前7天）")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime startTime,
            @Parameter(description = "统计结束时间（ISO-8601，缺省为当前时间）")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime endTime,
            @Parameter(description = "返回条数，缺省 10，最大 100") @RequestParam(required = false)
                    Integer limit) {
        return interfaceStatsService.topUsers(startTime, endTime, limit);
    }
}
