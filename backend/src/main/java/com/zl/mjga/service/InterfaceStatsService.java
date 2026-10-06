package com.zl.mjga.service;

import com.zl.mjga.dto.api.InterfaceApiRankDto;
import com.zl.mjga.dto.api.InterfaceCallTrendPointDto;
import com.zl.mjga.dto.api.InterfaceStatsOverviewDto;
import com.zl.mjga.dto.api.InterfaceUserRankDto;
import com.zl.mjga.dto.api.TrendGranularity;
import com.zl.mjga.repository.api.InterfaceCallLogRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 接口调用统计分析：总览指标、时间趋势（空档补零，便于前端直接绘图）、接口/用户调用量排行
 *
 * @author roc
 * @since 2026/10/6
 */
@Service
@RequiredArgsConstructor
public class InterfaceStatsService {

    private static final int DEFAULT_WINDOW_DAYS = 7;

    /** 趋势桶数量上限，防止过大时间范围 + 过细粒度拉爆响应 */
    private static final int MAX_TREND_BUCKETS = 1000;

    private static final int DEFAULT_LIMIT = 10;

    private static final int MAX_LIMIT = 100;

    private final InterfaceCallLogRepository interfaceCallLogRepository;

    /** 调用统计总览 */
    public InterfaceStatsOverviewDto overview(OffsetDateTime startTime, OffsetDateTime endTime) {
        TimeWindow window = normalizeWindow(startTime, endTime);
        return interfaceCallLogRepository.fetchOverview(window.startTime(), window.endTime());
    }

    /** 调用趋势序列（按粒度分桶，空档补零） */
    public List<InterfaceCallTrendPointDto> trend(
            OffsetDateTime startTime, OffsetDateTime endTime, TrendGranularity granularity) {
        TimeWindow window = normalizeWindow(startTime, endTime);
        List<InterfaceCallTrendPointDto> buckets =
                interfaceCallLogRepository.fetchTrendBuckets(
                        window.startTime(), window.endTime(), granularity);
        // 数据库桶按 UTC 对齐（date_trunc ... at time zone 'UTC'），这里用同一口径生成连续桶补零
        Map<OffsetDateTime, InterfaceCallTrendPointDto> bucketByTime = new HashMap<>();
        for (InterfaceCallTrendPointDto bucket : buckets) {
            bucketByTime.put(bucket.time(), bucket);
        }
        List<InterfaceCallTrendPointDto> result = new ArrayList<>();
        OffsetDateTime cursor = alignToUtcBucket(window.startTime(), granularity);
        while (!cursor.isAfter(window.endTime())) {
            result.add(bucketByTime.getOrDefault(cursor, emptyPoint(cursor, granularity)));
            cursor = cursor.plus(1, granularity.javaTimeUnit());
            if (result.size() >= MAX_TREND_BUCKETS) {
                throw new IllegalArgumentException(
                        String.format("时间范围与粒度组合的桶数超过上限 %d，请缩短时间范围或使用更粗粒度", MAX_TREND_BUCKETS));
            }
        }
        return result;
    }

    /** 接口调用量排行 */
    public List<InterfaceApiRankDto> topInterfaces(
            OffsetDateTime startTime, OffsetDateTime endTime, Integer limit) {
        TimeWindow window = normalizeWindow(startTime, endTime);
        return interfaceCallLogRepository.fetchTopInterfaces(
                window.startTime(), window.endTime(), normalizeLimit(limit));
    }

    /** 用户调用量排行 */
    public List<InterfaceUserRankDto> topUsers(
            OffsetDateTime startTime, OffsetDateTime endTime, Integer limit) {
        TimeWindow window = normalizeWindow(startTime, endTime);
        return interfaceCallLogRepository.fetchTopUsers(
                window.startTime(), window.endTime(), normalizeLimit(limit));
    }

    /** 缺省时间窗口：最近 7 天；仅提供一端时另一端按缺省窗口推导 */
    private TimeWindow normalizeWindow(OffsetDateTime startTime, OffsetDateTime endTime) {
        OffsetDateTime end = endTime != null ? endTime : OffsetDateTime.now();
        OffsetDateTime start = startTime != null ? startTime : end.minusDays(DEFAULT_WINDOW_DAYS);
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("统计起始时间必须早于结束时间");
        }
        return new TimeWindow(start, end);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private OffsetDateTime alignToUtcBucket(OffsetDateTime time, TrendGranularity granularity) {
        return time.withOffsetSameInstant(ZoneOffset.UTC).truncatedTo(granularity.javaTimeUnit());
    }

    private InterfaceCallTrendPointDto emptyPoint(
            OffsetDateTime time, TrendGranularity granularity) {
        return new InterfaceCallTrendPointDto(alignToUtcBucket(time, granularity), 0, 0, 0, 0.0, 0);
    }

    private record TimeWindow(OffsetDateTime startTime, OffsetDateTime endTime) {}
}
