package com.zl.mjga.service;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.PageResponseDto;
import com.zl.mjga.dto.api.InterfaceCreateDto;
import com.zl.mjga.dto.api.InterfaceDto;
import com.zl.mjga.dto.api.InterfaceQueryDto;
import com.zl.mjga.dto.api.InterfaceUpdateDto;
import com.zl.mjga.dto.api.InterfaceUploadDto;
import com.zl.mjga.exception.BusinessException;
import com.zl.mjga.repository.api.InterfaceRepository;
import com.zl.mjga.repository.api.InterfaceVersionRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.JSONB;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterface;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceVersion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author roc
 * @since 2025/12/25 20:11
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InterfaceService {
    private final InterfaceRepository interfaceRepository;

    private final InterfaceVersionRepository interfaceVersionRepository;

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
        interfaceRepository.logicDelete(id);
    }

    public void batchDeleteInterfaces(@NotEmpty(message = "ID列表不能为空") List<Long> ids) {
        interfaceRepository.logicDelete(ids);
    }

    /**
     * 用户上传API：创建接口（owner 为当前用户、初始 enabled=false 待审核）与其首个当前版本。 校验规则：code 全局唯一；(httpMethod, path)
     * 不与任何已注册的当前版本冲突（含待审核）； 通过审核后由管理员启用（PATCH /interfaces/{id}/enabled）方可在网关调用。
     */
    @Transactional(rollbackFor = BusinessException.class)
    public InterfaceDto uploadInterface(@Valid InterfaceUploadDto uploadDto, String owner) {
        if (interfaceRepository.fetchOneByCode(uploadDto.code()) != null) {
            throw new BusinessException("接口标识已存在: " + uploadDto.code());
        }
        if (interfaceVersionRepository.fetchCurrentByMethodPath(
                        uploadDto.httpMethod(), uploadDto.path())
                != null) {
            throw new BusinessException(
                    "请求方法与路径已被占用: " + uploadDto.httpMethod() + " " + uploadDto.path());
        }
        ApiInterface apiInterface =
                new ApiInterface()
                        .setCode(uploadDto.code())
                        .setName(uploadDto.name())
                        .setDescription(uploadDto.description())
                        .setCategory(uploadDto.category())
                        .setOwner(owner)
                        .setEnabled(false);
        interfaceRepository.insert(apiInterface);

        ApiInterfaceVersion version =
                new ApiInterfaceVersion()
                        .setApiId(apiInterface.getId())
                        .setVersion(uploadDto.version())
                        .setIsCurrent(true)
                        .setHttpMethod(uploadDto.httpMethod())
                        .setPath(uploadDto.path())
                        .setAllowInvoke(true)
                        .setRequestBody(toJsonb(uploadDto.requestBody()))
                        .setResponseExample(toJsonb(uploadDto.responseExample()));
        interfaceVersionRepository.insert(version);
        log.info(
                "Interface {} uploaded by {}, pending review (enabled=false)",
                uploadDto.code(),
                owner);
        return InterfaceDto.fromEntity(apiInterface);
    }

    private static JSONB toJsonb(String json) {
        return json == null || json.isBlank() ? null : JSONB.jsonb(json);
    }
}
