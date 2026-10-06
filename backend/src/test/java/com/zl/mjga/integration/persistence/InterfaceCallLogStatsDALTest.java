package com.zl.mjga.integration.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.zl.mjga.dto.api.InterfaceApiRankDto;
import com.zl.mjga.dto.api.InterfaceCallTrendPointDto;
import com.zl.mjga.dto.api.InterfaceStatsOverviewDto;
import com.zl.mjga.dto.api.InterfaceUserRankDto;
import com.zl.mjga.dto.api.TrendGranularity;
import com.zl.mjga.repository.api.InterfaceCallLogRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * 接口调用日志统计聚合 DAL 测试：用固定 UTC 时间的数据验证总览/趋势/排行的 SQL 聚合口径
 *
 * @author roc
 * @since 2026/10/6
 */
class InterfaceCallLogStatsDALTest extends AbstractDataAccessLayerTest {

    @Autowired private InterfaceCallLogRepository interfaceCallLogRepository;

    private static final OffsetDateTime RANGE_START = OffsetDateTime.parse("2026-10-01T10:00:00Z");
    private static final OffsetDateTime RANGE_END = OffsetDateTime.parse("2026-10-01T12:00:00Z");

    @Test
    @Sql(
            statements = {
                "DELETE FROM api_gateway.api_interface_call_log",
                "DELETE FROM api_gateway.api_interface_version",
                "DELETE FROM api_gateway.api_interface",
                "INSERT INTO api_gateway.api_interface (id, name, code, enabled, category)"
                        + " VALUES (1, '接口A', 'api-a', TRUE, 'mock')",
                "INSERT INTO api_gateway.api_interface (id, name, code, enabled, category)"
                        + " VALUES (2, '接口B', 'api-b', TRUE, 'mock')",
                "INSERT INTO api_gateway.api_interface_call_log"
                        + " (api_id, version_id, caller, status_code, success, duration_ms,"
                        + " create_time) VALUES (1, 11, 'alice', 200, TRUE, 100,"
                        + " TIMESTAMPTZ '2026-10-01 10:15:00+00')",
                "INSERT INTO api_gateway.api_interface_call_log"
                        + " (api_id, version_id, caller, status_code, success, duration_ms,"
                        + " create_time) VALUES (1, 11, 'alice', 500, FALSE, 200,"
                        + " TIMESTAMPTZ '2026-10-01 10:40:00+00')",
                "INSERT INTO api_gateway.api_interface_call_log"
                        + " (api_id, version_id, caller, status_code, success, duration_ms,"
                        + " create_time) VALUES (2, 22, 'bob', 200, TRUE, 300,"
                        + " TIMESTAMPTZ '2026-10-01 11:05:00+00')"
            })
    void fetchOverview_shouldAggregateCallsInRange() {
        InterfaceStatsOverviewDto overview =
                interfaceCallLogRepository.fetchOverview(RANGE_START, RANGE_END);

        assertThat(overview.totalCalls()).isEqualTo(3);
        assertThat(overview.successCalls()).isEqualTo(2);
        assertThat(overview.failCalls()).isEqualTo(1);
        assertThat(overview.successRate()).isEqualTo(0.6667);
        assertThat(overview.avgDurationMs()).isEqualTo(200);
        assertThat(overview.maxDurationMs()).isEqualTo(300);
        assertThat(overview.callerCount()).isEqualTo(2);
        assertThat(overview.interfaceCount()).isEqualTo(2);
    }

    @Test
    @Sql(
            statements = {
                "DELETE FROM api_gateway.api_interface_call_log",
                "INSERT INTO api_gateway.api_interface (id, name, code, enabled)"
                        + " VALUES (1, '接口A', 'api-a', TRUE) ON CONFLICT DO NOTHING",
                "INSERT INTO api_gateway.api_interface_call_log"
                        + " (api_id, version_id, caller, status_code, success, duration_ms,"
                        + " create_time) VALUES (1, 11, 'alice', 200, TRUE, 100,"
                        + " TIMESTAMPTZ '2026-10-01 10:15:00+00')",
                "INSERT INTO api_gateway.api_interface_call_log"
                        + " (api_id, version_id, caller, status_code, success, duration_ms,"
                        + " create_time) VALUES (1, 11, 'alice', 500, FALSE, 200,"
                        + " TIMESTAMPTZ '2026-10-01 10:40:00+00')",
                "INSERT INTO api_gateway.api_interface_call_log"
                        + " (api_id, version_id, caller, status_code, success, duration_ms,"
                        + " create_time) VALUES (1, 11, 'alice', 200, TRUE, 300,"
                        + " TIMESTAMPTZ '2026-10-01 11:05:00+00')"
            })
    void fetchTrendBuckets_shouldGroupByUtcHour() {
        List<InterfaceCallTrendPointDto> buckets =
                interfaceCallLogRepository.fetchTrendBuckets(
                        RANGE_START, RANGE_END, TrendGranularity.HOUR);

        assertThat(buckets).hasSize(2);
        assertThat(buckets.get(0).time()).isEqualTo(OffsetDateTime.parse("2026-10-01T10:00:00Z"));
        assertThat(buckets.get(0).totalCalls()).isEqualTo(2);
        assertThat(buckets.get(0).successCalls()).isEqualTo(1);
        assertThat(buckets.get(0).failCalls()).isEqualTo(1);
        assertThat(buckets.get(0).avgDurationMs()).isEqualTo(150);
        assertThat(buckets.get(1).time()).isEqualTo(OffsetDateTime.parse("2026-10-01T11:00:00Z"));
        assertThat(buckets.get(1).totalCalls()).isEqualTo(1);
    }

    @Test
    @Sql(
            statements = {
                "DELETE FROM api_gateway.api_interface_call_log",
                "DELETE FROM api_gateway.api_interface_version",
                "DELETE FROM api_gateway.api_interface",
                "INSERT INTO api_gateway.api_interface (id, name, code, enabled, category)"
                        + " VALUES (1, '接口A', 'api-a', TRUE, 'mock')",
                "INSERT INTO api_gateway.api_interface (id, name, code, enabled, category)"
                        + " VALUES (2, '接口B', 'api-b', TRUE, 'mock')",
                "INSERT INTO api_gateway.api_interface_call_log"
                        + " (api_id, version_id, caller, status_code, success, duration_ms,"
                        + " create_time) VALUES (1, 11, 'alice', 200, TRUE, 100,"
                        + " TIMESTAMPTZ '2026-10-01 10:15:00+00')",
                "INSERT INTO api_gateway.api_interface_call_log"
                        + " (api_id, version_id, caller, status_code, success, duration_ms,"
                        + " create_time) VALUES (1, 11, 'alice', 500, FALSE, 200,"
                        + " TIMESTAMPTZ '2026-10-01 10:40:00+00')",
                "INSERT INTO api_gateway.api_interface_call_log"
                        + " (api_id, version_id, caller, status_code, success, duration_ms,"
                        + " create_time) VALUES (2, 22, 'bob', 200, TRUE, 300,"
                        + " TIMESTAMPTZ '2026-10-01 11:05:00+00')"
            })
    void fetchTopInterfacesAndUsers_shouldRankByCallCountDesc() {
        List<InterfaceApiRankDto> apiRanks =
                interfaceCallLogRepository.fetchTopInterfaces(RANGE_START, RANGE_END, 10);

        assertThat(apiRanks).hasSize(2);
        assertThat(apiRanks.get(0))
                .extracting(InterfaceApiRankDto::apiId, InterfaceApiRankDto::name)
                .containsExactly(1L, "接口A");
        assertThat(apiRanks.get(0).totalCalls()).isEqualTo(2);
        assertThat(apiRanks.get(0).successRate()).isEqualTo(0.5);
        assertThat(apiRanks.get(1).apiId()).isEqualTo(2L);
        assertThat(apiRanks.get(1).totalCalls()).isEqualTo(1);

        List<InterfaceUserRankDto> userRanks =
                interfaceCallLogRepository.fetchTopUsers(RANGE_START, RANGE_END, 10);

        assertThat(userRanks).hasSize(2);
        assertThat(userRanks.get(0).caller()).isEqualTo("alice");
        assertThat(userRanks.get(0).totalCalls()).isEqualTo(2);
        assertThat(userRanks.get(1).caller()).isEqualTo("bob");
    }
}
