package com.zl.mjga.repository.api;

import static org.jooq.impl.DSL.noCondition;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.api.InterfaceApiRankDto;
import com.zl.mjga.dto.api.InterfaceCallLogQueryDto;
import com.zl.mjga.dto.api.InterfaceCallTrendPointDto;
import com.zl.mjga.dto.api.InterfaceStatsOverviewDto;
import com.zl.mjga.dto.api.InterfaceUserRankDto;
import com.zl.mjga.dto.api.TrendGranularity;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.generated.api_gateway.tables.ApiInterface;
import org.jooq.generated.api_gateway.tables.ApiInterfaceCallLog;
import org.jooq.generated.api_gateway.tables.daos.ApiInterfaceCallLogDao;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author roc
 * @since 2025/12/25 20:09
 */
@Repository
public class InterfaceCallLogRepository extends ApiInterfaceCallLogDao {
    @Autowired
    public InterfaceCallLogRepository(Configuration configuration) {
        super(configuration);
    }

    /** 时间范围内的调用统计总览（总量/成功率/耗时/活跃调用方与接口数） */
    public InterfaceStatsOverviewDto fetchOverview(
            OffsetDateTime startTime, OffsetDateTime endTime) {
        Field<Integer> successCount = successCountField();
        return ctx().select(
                        DSL.count(),
                        successCount,
                        DSL.countDistinct(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.CALLER),
                        DSL.countDistinct(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.API_ID),
                        DSL.avg(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.DURATION_MS),
                        DSL.max(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.DURATION_MS))
                .from(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG)
                .where(timeRangeCondition(startTime, endTime))
                .fetchOne(
                        row -> {
                            long totalCalls = row.value1();
                            long successCalls = row.value2() == null ? 0 : row.value2();
                            return new InterfaceStatsOverviewDto(
                                    startTime,
                                    endTime,
                                    totalCalls,
                                    successCalls,
                                    totalCalls - successCalls,
                                    successRate(totalCalls, successCalls),
                                    avgDurationRounded(row.value5()),
                                    row.value6() == null ? 0 : row.value6(),
                                    row.value3(),
                                    row.value4());
                        });
    }

    /** 按时间粒度聚合的调用趋势桶（只返回有数据的桶，空档由服务层补零）；桶时间统一归一化为 UTC offset，便于服务层按时间合并 */
    public List<InterfaceCallTrendPointDto> fetchTrendBuckets(
            OffsetDateTime startTime, OffsetDateTime endTime, TrendGranularity granularity) {
        Field<OffsetDateTime> bucket = trendBucketField(granularity);
        return ctx().select(
                        bucket,
                        DSL.count(),
                        successCountField(),
                        DSL.avg(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.DURATION_MS))
                .from(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG)
                .where(timeRangeCondition(startTime, endTime))
                .groupBy(bucket)
                .orderBy(bucket.asc())
                .fetch(
                        row ->
                                new InterfaceCallTrendPointDto(
                                        row.value1().withOffsetSameInstant(ZoneOffset.UTC),
                                        row.value2(),
                                        row.value3(),
                                        row.value2() - row.value3(),
                                        successRate(row.value2(), row.value3()),
                                        avgDurationRounded(row.value4())));
    }

    /** 接口调用量排行（联表补齐接口名称，按调用量降序） */
    public List<InterfaceApiRankDto> fetchTopInterfaces(
            OffsetDateTime startTime, OffsetDateTime endTime, int limit) {
        return ctx().select(
                        ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.API_ID,
                        ApiInterface.API_INTERFACE.NAME,
                        ApiInterface.API_INTERFACE.CODE,
                        ApiInterface.API_INTERFACE.CATEGORY,
                        DSL.count(),
                        successCountField(),
                        DSL.avg(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.DURATION_MS))
                .from(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG)
                .join(ApiInterface.API_INTERFACE)
                .on(
                        ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.API_ID.eq(
                                ApiInterface.API_INTERFACE.ID))
                .where(timeRangeCondition(startTime, endTime))
                .groupBy(
                        ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.API_ID,
                        ApiInterface.API_INTERFACE.NAME,
                        ApiInterface.API_INTERFACE.CODE,
                        ApiInterface.API_INTERFACE.CATEGORY)
                .orderBy(
                        DSL.count().desc(), ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.API_ID.asc())
                .limit(limit)
                .fetch(
                        row ->
                                new InterfaceApiRankDto(
                                        row.value1(),
                                        row.value2(),
                                        row.value3(),
                                        row.value4(),
                                        row.value5(),
                                        row.value6(),
                                        row.value5() - row.value6(),
                                        successRate(row.value5(), row.value6()),
                                        avgDurationRounded(row.value7())));
    }

    /** 用户调用量排行（按调用量降序，忽略匿名调用） */
    public List<InterfaceUserRankDto> fetchTopUsers(
            OffsetDateTime startTime, OffsetDateTime endTime, int limit) {
        return ctx().select(
                        ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.CALLER,
                        DSL.count(),
                        successCountField(),
                        DSL.avg(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.DURATION_MS))
                .from(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG)
                .where(timeRangeCondition(startTime, endTime))
                .and(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.CALLER.isNotNull())
                .groupBy(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.CALLER)
                .orderBy(
                        DSL.count().desc(), ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.CALLER.asc())
                .limit(limit)
                .fetch(
                        row ->
                                new InterfaceUserRankDto(
                                        row.value1(),
                                        row.value2(),
                                        row.value3(),
                                        row.value2() - row.value3(),
                                        successRate(row.value2(), row.value3()),
                                        avgDurationRounded(row.value4())));
    }

    private Condition timeRangeCondition(OffsetDateTime startTime, OffsetDateTime endTime) {
        return ApiInterfaceCallLog.API_INTERFACE_CALL_LOG
                .CREATE_TIME
                .ge(startTime)
                .and(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.CREATE_TIME.le(endTime));
    }

    /** count(*) filter (where success) 成功次数聚合列 */
    private static Field<Integer> successCountField() {
        return DSL.count().filterWhere(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.SUCCESS.eq(true));
    }

    /** 按 UTC 对齐的时间桶列：date_trunc(unit, create_time at time zone 'UTC')，与会话时区无关 */
    private static Field<OffsetDateTime> trendBucketField(TrendGranularity granularity) {
        return DSL.field(
                "date_trunc({0}, {1} at time zone {2}) at time zone {2}",
                OffsetDateTime.class,
                DSL.inline(granularity.pgUnit()),
                ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.CREATE_TIME,
                DSL.inline("UTC"));
    }

    private static double successRate(long totalCalls, long successCalls) {
        return totalCalls <= 0 ? 0.0 : Math.round(successCalls * 10000.0 / totalCalls) / 10000.0;
    }

    private static long avgDurationRounded(BigDecimal avgDuration) {
        return avgDuration == null ? 0L : Math.round(avgDuration.doubleValue());
    }

    public long countByQueryDto(InterfaceCallLogQueryDto queryDto) {
        return ctx().fetchCount(
                        ApiInterfaceCallLog.API_INTERFACE_CALL_LOG, buildCondition(queryDto));
    }

    public List<org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceCallLog>
            fetchByPageRequestDto(@Valid PageRequestDto<InterfaceCallLogQueryDto> pageRequestDto) {
        Condition condition = buildCondition(pageRequestDto.getRequest());

        return ctx().selectFrom(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG)
                .where(condition)
                .orderBy(pageRequestDto.getSortFields())
                .limit(pageRequestDto.getSize())
                .offset(pageRequestDto.getOffset())
                .fetchInto(org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceCallLog.class);
    }

    private Condition buildCondition(InterfaceCallLogQueryDto queryDto) {
        if (queryDto == null) {
            return noCondition();
        }

        List<Condition> conditions = new ArrayList<>();

        if (queryDto.apiId() != null) {
            conditions.add(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.API_ID.eq(queryDto.apiId()));
        }
        if (queryDto.versionId() != null) {
            conditions.add(
                    ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.VERSION_ID.eq(queryDto.versionId()));
        }
        if (StringUtils.isNotBlank(queryDto.caller())) {
            conditions.add(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.CALLER.eq(queryDto.caller()));
        }
        if (queryDto.statusCode() != null) {
            conditions.add(
                    ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.STATUS_CODE.eq(
                            queryDto.statusCode()));
        }
        if (queryDto.success() != null) {
            conditions.add(
                    ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.SUCCESS.eq(queryDto.success()));
        }
        if (queryDto.durationMs() != null) {
            conditions.add(
                    ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.DURATION_MS.eq(
                            queryDto.durationMs()));
        }
        if (queryDto.createTimeStart() != null) {
            conditions.add(
                    ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.CREATE_TIME.greaterOrEqual(
                            queryDto.createTimeStart()));
        }
        if (queryDto.createTimeEnd() != null) {
            conditions.add(
                    ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.CREATE_TIME.lessOrEqual(
                            queryDto.createTimeEnd()));
        }

        return conditions.isEmpty() ? noCondition() : DSL.and(conditions);
    }
}
