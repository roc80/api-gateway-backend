package com.zl.mjga.service;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.PageResponseDto;
import com.zl.mjga.dto.api.InterfaceCreateDto;
import com.zl.mjga.dto.api.InterfaceDto;
import com.zl.mjga.dto.api.InterfaceQueryDto;
import com.zl.mjga.dto.api.InterfaceUpdateDto;
import com.zl.mjga.repository.api.InterfaceRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterface;
import org.springframework.stereotype.Service;

/**
 * @author roc
 * @since 2025/12/25 20:11
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InterfaceService {
    private final InterfaceRepository interfaceRepository;

    public InterfaceDto createInterface(@Valid InterfaceCreateDto createDto) {
        ApiInterface entity = createDto.toEntity();
        interfaceRepository.insert(entity);
        return InterfaceDto.fromEntity(entity);
    }

    public InterfaceDto updateInterface(
            @Positive(message = "接口ID必须为正整数") Long id, @Valid InterfaceUpdateDto updateDto) {
        ApiInterface entity = interfaceRepository.fetchOneById(id);
        if (entity == null) {
            throw new IllegalArgumentException(id + "对应的接口不存在");
        }
        updateDto.applyTo(entity);
        interfaceRepository.update(entity);
        return InterfaceDto.fromEntity(entity);
    }

    public InterfaceDto updateEnabled(@Positive(message = "接口ID必须为正整数") Long id, Boolean enabled) {
        ApiInterface entity = interfaceRepository.fetchOneById(id);
        if (entity == null) {
            throw new IllegalArgumentException(id + "对应的接口不存在");
        }
        entity.setEnabled(enabled);
        interfaceRepository.update(entity);
        return InterfaceDto.fromEntity(entity);
    }

    public InterfaceDto getInterfaceById(@Positive(message = "接口ID必须为正整数") Long id) {
        ApiInterface entity = interfaceRepository.fetchOneById(id);
        if (entity == null) {
            throw new IllegalArgumentException(id + "对应的接口不存在");
        }
        return InterfaceDto.fromEntity(entity);
    }

    public PageResponseDto<List<InterfaceDto>> searchInterfaces(
            @Valid PageRequestDto<InterfaceQueryDto> pageRequestDto) {
        long total = interfaceRepository.countByQueryDto(pageRequestDto.getRequest());
        List<ApiInterface> entities = interfaceRepository.fetchByPageRequestDto(pageRequestDto);
        return PageResponseDto.fromEntities(total, entities, InterfaceDto::fromEntity);
    }

    public void deleteInterface(@Positive(message = "接口ID必须为正整数") Long id) {
        interfaceRepository.deleteById(id);
    }

    public void batchDeleteInterfaces(@NotEmpty(message = "ID列表不能为空") List<Long> ids) {
        interfaceRepository.deleteById(ids);
    }
}
