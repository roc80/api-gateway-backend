package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * 调用趋势统计的时间粒度
 *
 * @author roc
 * @since 2026/10/6
 */
@Schema(description = "趋势统计时间粒度：day / hour")
public enum TrendGranularity {
    DAY("day", ChronoUnit.DAYS),
    HOUR("hour", ChronoUnit.HOURS);

    /** PostgreSQL date_trunc 的时间单位名 */
    private final String unit;

    /** 与 date_trunc 单位对应的 java.time 粒度，用于服务层生成对齐的连续时间桶 */
    private final ChronoUnit javaTimeUnit;

    TrendGranularity(String unit, ChronoUnit javaTimeUnit) {
        this.unit = unit;
        this.javaTimeUnit = javaTimeUnit;
    }

    /** PostgreSQL date_trunc 的单位字面量 */
    public String pgUnit() {
        return unit;
    }

    /** 用于服务层生成 UTC 对齐连续时间桶的 java.time 粒度 */
    public ChronoUnit javaTimeUnit() {
        return javaTimeUnit;
    }

    /**
     * 大小写不敏感解析，非法取值抛出 IllegalArgumentException
     *
     * @param value 前端传入的粒度字符串，如 day、HOUR
     * @return 对应粒度枚举
     */
    public static TrendGranularity fromString(String value) {
        if (value == null || value.isBlank()) {
            return DAY;
        }
        try {
            return TrendGranularity.valueOf(value.trim().toUpperCase(Locale.US));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("不支持的统计粒度 '%s'，可选值：day / hour", value), e);
        }
    }
}
