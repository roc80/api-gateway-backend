package com.zl.mjga.service;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.PageResponseDto;
import com.zl.mjga.dto.api.InterfaceVersionBatchDeleteDto;
import com.zl.mjga.dto.api.InterfaceVersionCreateDto;
import com.zl.mjga.dto.api.InterfaceVersionDto;
import com.zl.mjga.dto.api.InterfaceVersionQueryDto;
import com.zl.mjga.dto.api.InterfaceVersionUpdateDto;
import com.zl.mjga.exception.BusinessException;
import com.zl.mjga.repository.api.InterfaceRepository;
import com.zl.mjga.repository.api.InterfaceVersionRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterface;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceVersion;
import org.springframework.stereotype.Service;

/**
 * @author roc
 * @since 2025/12/31 17:17
 */
@Service
@RequiredArgsConstructor
public class InterfaceVersionService {

    private final InterfaceVersionRepository interfaceVersionRepository;
    private final InterfaceRepository interfaceRepository;

    public InterfaceVersionDto getInterfaceVersionById(
            @Positive(message = "接口版本ID必须为正整数") Long id) {
        ApiInterfaceVersion entity = interfaceVersionRepository.findById(id);
        if (entity == null) {
            throw new IllegalArgumentException(id + "对应的接口版本不存在");
        }
        return InterfaceVersionDto.fromEntity(entity);
    }

    public PageResponseDto<List<InterfaceVersionDto>> searchInterfaceVersion(
            @Valid PageRequestDto<InterfaceVersionQueryDto> pageRequestDto) {
        List<ApiInterfaceVersion> entities =
                interfaceVersionRepository.fetchByPageRequestDto(pageRequestDto);
        long total = interfaceVersionRepository.countByQueryDto(pageRequestDto.getRequest());
        return PageResponseDto.fromEntities(total, entities, InterfaceVersionDto::fromEntity);
    }

    /** todo@lp 更新接口版本后，新增一条记录。 */
    public InterfaceVersionDto createInterfaceVersion(
            @Valid InterfaceVersionCreateDto interfaceVersionCreateDto) {
        Long apiId = interfaceVersionCreateDto.apiId();
        String apiVersion = interfaceVersionCreateDto.version();
        validateInterfaceVersion(apiId, apiVersion);

        ApiInterfaceVersion entity = interfaceVersionCreateDto.toEntity();
        interfaceVersionRepository.insert(entity);
        return InterfaceVersionDto.fromEntity(entity);
    }

    /**
     * 校验数据库中的约束
     */
    private void validateInterfaceVersion(Long apiId, String apiVersion) {
        ApiInterface interfaceEntity = interfaceRepository.findById(apiId);
        if (interfaceEntity == null) {
            throw new IllegalArgumentException("接口id: " + apiId + "不存在");
        }
        ApiInterfaceVersion interfaceVersionEntity = interfaceVersionRepository.findByApiIdAndVersion(apiId, apiVersion);
        if (interfaceVersionEntity != null) {
            throw new IllegalArgumentException("接口id: " + apiId + "接口版本: " + apiVersion + "已存在");
        }
    }

    public InterfaceVersionDto updateInterfaceVersion(
            @Positive(message = "接口版本ID必须为正整数") Long id,
            @Valid InterfaceVersionUpdateDto interfaceVersionUpdateDto) {
        ApiInterfaceVersion entity = interfaceVersionRepository.fetchOneById(id);
        if (entity == null) {
            throw new IllegalArgumentException("id: " + id + " 不存在");
        }
        interfaceVersionUpdateDto.applyTo(entity);
        interfaceVersionRepository.update(entity);
        return InterfaceVersionDto.fromEntity(entity);
    }

    public void deleteInterfaceVersion(@Positive(message = "接口版本ID必须为正整数") Long id) {
        interfaceVersionRepository.logicDelete(id);
    }

    public void batchDeleteInterfaceVersion(@Valid InterfaceVersionBatchDeleteDto batchDeleteDto) {
        interfaceVersionRepository.logicDelete(batchDeleteDto.ids());
    }
}
