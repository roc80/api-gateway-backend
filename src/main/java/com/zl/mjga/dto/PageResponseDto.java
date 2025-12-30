package com.zl.mjga.dto;

import java.util.List;
import java.util.function.Function;

import jakarta.annotation.Nullable;
import lombok.*;

@Data
public class PageResponseDto<T> {
    private long total;
    private T data;

    public PageResponseDto(long total, @Nullable T data) {
        if (total < 0) {
            throw new IllegalArgumentException("total must not be less than zero");
        }
        this.total = total;
        this.data = data;
    }

    public static <T, R> PageResponseDto<List<R>> fromEntities(long total, List<T> entities, Function<T, R> mapper) {
        List<R> data = entities.stream()
                .map(mapper)
                .toList();
        return new PageResponseDto<>(total, data);
    }

    public static <T> PageResponseDto<T> empty() {
        return new PageResponseDto<>(0, null);
    }
}
