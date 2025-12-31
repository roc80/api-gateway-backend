package com.zl.mjga.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.PageResponseDto;
import com.zl.mjga.dto.api.InterfaceCallLogCreateDto;
import com.zl.mjga.dto.api.InterfaceCallLogDto;
import com.zl.mjga.dto.api.InterfaceCallLogQueryDto;
import com.zl.mjga.repository.api.InterfaceCallLogRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/**
 * @author roc
 * @since 2025/12/31 17:17
 */
@Service
@RequiredArgsConstructor
public class InterfaceCallLogService {

    private final InterfaceCallLogRepository interfaceCallLogRepository;


    public InterfaceCallLogDto getInterfaceCallLogById(@Positive(message = "接口调用日志ID必须为正整数") Long id) {
        // todo@Lp
        return null;
    }

    public PageResponseDto<List<InterfaceCallLogDto>> searchInterfaces(@Valid PageRequestDto<InterfaceCallLogQueryDto> pageRequestDto) {
        // todo@Lp
        return null;
    }

    public InterfaceCallLogDto createInterfaceCallLog(@Valid InterfaceCallLogCreateDto interfaceCallLogCreateDto) {
        // todo@lp 调用发生后，新增一条记录
        return null;
    }
}
