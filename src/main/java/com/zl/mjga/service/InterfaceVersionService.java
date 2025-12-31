package com.zl.mjga.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.PageResponseDto;
import com.zl.mjga.dto.api.InterfaceQueryDto;
import com.zl.mjga.dto.api.InterfaceVersionCreateDto;
import com.zl.mjga.dto.api.InterfaceVersionDto;
import com.zl.mjga.dto.api.InterfaceVersionQueryDto;
import com.zl.mjga.repository.api.InterfaceVersionRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/**
 * @author roc
 * @since 2025/12/31 17:17
 */
@Service
@RequiredArgsConstructor
public class InterfaceVersionService {

    private final InterfaceVersionRepository interfaceVersionRepository;

    public InterfaceVersionDto getInterfaceVersionById(@Positive(message = "接口版本ID必须为正整数") Long id) {
        // todo@lp
        return null;
    }

    public PageResponseDto<List<InterfaceVersionDto>> searchInterfaceVersion(@Valid PageRequestDto<InterfaceVersionQueryDto> pageRequestDto) {
        // todo@lp
        return null;
    }

    public InterfaceVersionDto createInterfaceVersion(@Valid InterfaceVersionCreateDto interfaceVersionCreateDto) {
        // todo@lp 更新接口版本后，新增一条记录。
        return null;
    }
}
