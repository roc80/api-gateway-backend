package com.zl.mjga.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zl.mjga.dto.api.InterfaceCallTrendPointDto;
import com.zl.mjga.dto.api.TrendGranularity;
import com.zl.mjga.repository.api.InterfaceCallLogRepository;
import com.zl.mjga.service.InterfaceStatsService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 调用统计分析服务测试：缺省时间窗口、UTC 桶对齐与空档补零、入参校验
 *
 * @author roc
 * @since 2026/10/6
 */
@ExtendWith(MockitoExtension.class)
class InterfaceStatsServiceTest {

    @Mock private InterfaceCallLogRepository interfaceCallLogRepository;

    @InjectMocks private InterfaceStatsService interfaceStatsService;

    @Test
    void trend_givenMissingBuckets_shouldZeroFillAlignedToUtc() {
        // 10:30 +08:00 == 02:30 UTC，按小时粒度应从 02:00 UTC 桶开始
        OffsetDateTime start = OffsetDateTime.parse("2026-10-01T10:30:00+08:00");
        OffsetDateTime end = OffsetDateTime.parse("2026-10-01T15:10:00+08:00");
        OffsetDateTime bucket03 = OffsetDateTime.parse("2026-10-01T03:00:00Z");
        when(interfaceCallLogRepository.fetchTrendBuckets(
                        any(OffsetDateTime.class),
                        any(OffsetDateTime.class),
                        eq(TrendGranularity.HOUR)))
                .thenReturn(List.of(new InterfaceCallTrendPointDto(bucket03, 4, 3, 1, 0.75, 120)));

        List<InterfaceCallTrendPointDto> points =
                interfaceStatsService.trend(start, end, TrendGranularity.HOUR);

        // 02:00~07:00 UTC 共 6 个桶，只有 03:00 有数据，其余补零
        assertThat(points).hasSize(6);
        assertThat(points.get(0).time()).isEqualTo(OffsetDateTime.parse("2026-10-01T02:00:00Z"));
        assertThat(points.get(1))
                .extracting(
                        InterfaceCallTrendPointDto::time,
                        InterfaceCallTrendPointDto::totalCalls,
                        InterfaceCallTrendPointDto::successCalls,
                        InterfaceCallTrendPointDto::failCalls,
                        InterfaceCallTrendPointDto::avgDurationMs)
                .containsExactly(bucket03, 4L, 3L, 1L, 120L);
        assertThat(points.get(0).totalCalls()).isZero();
        assertThat(points.get(2).totalCalls()).isZero();
        assertThat(points.get(5).time()).isEqualTo(OffsetDateTime.parse("2026-10-01T07:00:00Z"));
    }

    @Test
    void trend_givenNonUtcInput_shouldQueryWithOriginalRange() {
        OffsetDateTime start = OffsetDateTime.parse("2026-10-01T10:30:00+08:00");
        OffsetDateTime end = OffsetDateTime.parse("2026-10-01T11:30:00+08:00");
        when(interfaceCallLogRepository.fetchTrendBuckets(any(), any(), eq(TrendGranularity.DAY)))
                .thenReturn(List.of());

        interfaceStatsService.trend(start, end, TrendGranularity.DAY);

        verify(interfaceCallLogRepository)
                .fetchTrendBuckets(eq(start), eq(end), eq(TrendGranularity.DAY));
    }

    @Test
    void givenNoTimeWindow_shouldDefaultToLastSevenDays() {
        ArgumentCaptor<OffsetDateTime> startCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> endCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        OffsetDateTime before = OffsetDateTime.now();

        interfaceStatsService.overview(null, null);

        verify(interfaceCallLogRepository)
                .fetchOverview(startCaptor.capture(), endCaptor.capture());
        OffsetDateTime start = startCaptor.getValue();
        OffsetDateTime end = endCaptor.getValue();
        assertThat(start).isAfterOrEqualTo(before.minusDays(7).minusMinutes(1));
        assertThat(start).isBeforeOrEqualTo(before.minusDays(7).plusMinutes(1));
        assertThat(end).isAfterOrEqualTo(before);
    }

    @Test
    void givenIllegalRange_shouldThrow() {
        OffsetDateTime start = OffsetDateTime.of(2026, 10, 6, 12, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime end = start.minusHours(1);

        assertThatThrownBy(() -> interfaceStatsService.overview(start, end))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> interfaceStatsService.overview(start, start))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void trend_givenTooManyBuckets_shouldThrow() {
        // 100 天按小时聚合约 2400 桶，超过上限
        OffsetDateTime end = OffsetDateTime.of(2026, 10, 6, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime start = end.minusDays(100);

        assertThatThrownBy(() -> interfaceStatsService.trend(start, end, TrendGranularity.HOUR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("桶数超过上限");
    }

    @Test
    void givenLimitOutOfRange_shouldClampToDefaultOrMax() {
        OffsetDateTime start = OffsetDateTime.of(2026, 9, 29, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime end = OffsetDateTime.of(2026, 10, 6, 0, 0, 0, 0, ZoneOffset.UTC);

        interfaceStatsService.topInterfaces(start, end, 0);
        interfaceStatsService.topInterfaces(start, end, 500);
        interfaceStatsService.topUsers(start, end, null);

        verify(interfaceCallLogRepository).fetchTopInterfaces(start, end, 10);
        verify(interfaceCallLogRepository).fetchTopInterfaces(start, end, 100);
        verify(interfaceCallLogRepository).fetchTopUsers(start, end, 10);
    }
}
