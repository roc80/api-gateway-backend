package com.zl.mjga.service;

import java.util.List;

import com.zl.mjga.exception.BusinessException;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceCallLog;
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
        ApiInterfaceCallLog entity = interfaceCallLogRepository.findById(id);
        if (entity == null) {
            throw new BusinessException(String.format("id = %d 的接口调用日志不存在", id));
        }
        return InterfaceCallLogDto.fromEntity(entity);
    }

    public PageResponseDto<List<InterfaceCallLogDto>> searchInterfaceCallLogs(@Valid PageRequestDto<InterfaceCallLogQueryDto> pageRequestDto) {
        long total = interfaceCallLogRepository.countByQueryDto(pageRequestDto.getRequest());
        List<ApiInterfaceCallLog> entities = interfaceCallLogRepository.fetchByPageRequestDto(pageRequestDto);
        return PageResponseDto.fromEntities(total, entities, InterfaceCallLogDto::fromEntity);
    }

    public InterfaceCallLogDto createInterfaceCallLog(@Valid InterfaceCallLogCreateDto interfaceCallLogCreateDto) {
        // todo@lp 调用发生后，新增一条记录
        return null;
    }
}
