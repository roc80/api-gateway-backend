package com.zl.mjga.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zl.mjga.client.GatewayApiClient;
import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.PageResponseDto;
import com.zl.mjga.dto.api.*;
import com.zl.mjga.exception.BusinessException;
import com.zl.mjga.repository.UserRepository;
import com.zl.mjga.service.InterfaceCallLogService;
import com.zl.mjga.service.InterfaceService;
import com.zl.mjga.service.InterfaceVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceVersion;
import org.jooq.generated.api_gateway.tables.pojos.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 接口管理控制器
 *
 * @author roc
 * @since 2025/12/25 20:14
 */
@RestController
@RequestMapping("/interfaces")
@RequiredArgsConstructor
@Validated
@Slf4j
public class InterfaceController {

    private final InterfaceService interfaceService;
    private final InterfaceVersionService interfaceVersionService;
    private final UserRepository userRepository;
    private final InterfaceCallLogService interfaceCallLogService;
    private final GatewayApiClient gatewayApiClient;
    private final ObjectMapper objectMapper;

    /** 创建接口 */
    @Operation(summary = "创建接口", description = "创建新的接口信息")
    @PreAuthorize("hasAuthority(T(com.zl.mjga.model.urp.EPermission).WRITE_INTERFACE)")
    @PostMapping
    public InterfaceDto create(@Valid @RequestBody InterfaceCreateDto createDto) {
        return interfaceService.createInterface(createDto);
    }

    /** 更新接口 */
    @Operation(summary = "更新接口", description = "根据 ID 更新接口信息")
    @PreAuthorize("hasAuthority(T(com.zl.mjga.model.urp.EPermission).WRITE_INTERFACE)")
    @PutMapping("/{id}")
    public InterfaceDto update(
            @Parameter(description = "接口ID", required = true)
                    @PathVariable
                    @Positive(message = "接口ID必须为正整数") Long id,
            @Valid @RequestBody InterfaceUpdateDto updateDto) {
        return interfaceService.updateInterface(id, updateDto);
    }

    /** 部分更新接口 (仅更新启用状态) */
    @Operation(summary = "更新接口启用状态", description = "启用或禁用接口")
    @PreAuthorize("hasAuthority(T(com.zl.mjga.model.urp.EPermission).WRITE_INTERFACE)")
    @PatchMapping("/{id}/enabled")
    public InterfaceDto patchEnabled(
            @Parameter(description = "接口ID", required = true)
                    @PathVariable
                    @Positive(message = "接口ID必须为正整数") Long id,
            @Parameter(description = "是否启用", required = true) @RequestParam Boolean enabled) {
        return interfaceService.updateEnabled(id, enabled);
    }

    /** 根据 ID 查询接口 */
    @Operation(summary = "查询接口详情", description = "根据 ID 查询接口详细信息")
    @GetMapping("/{id}")
    public InterfaceDto getById(
            @Parameter(description = "接口ID", required = true)
                    @PathVariable
                    @Positive(message = "接口ID必须为正整数") Long id) {
        return interfaceService.getInterfaceById(id);
    }

    /** 分页查询接口列表（复杂查询） */
    @Operation(summary = "分页查询接口列表", description = "根据条件分页查询接口列表")
    @PostMapping("/search")
    public PageResponseDto<List<InterfaceDto>> search(
            @Valid @RequestBody PageRequestDto<InterfaceQueryDto> pageRequestDto) {
        return interfaceService.searchInterfaces(pageRequestDto);
    }

    /** 删除接口 */
    @Operation(summary = "删除接口", description = "根据 ID 删除接口")
    @PreAuthorize("hasAuthority(T(com.zl.mjga.model.urp.EPermission).WRITE_INTERFACE)")
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "接口ID", required = true)
                    @PathVariable
                    @Positive(message = "接口ID必须为正整数") Long id) {
        interfaceService.deleteInterface(id);
    }

    /** 批量删除接口 */
    @Operation(summary = "批量删除接口", description = "根据 ID 列表批量删除接口")
    @PreAuthorize("hasAuthority(T(com.zl.mjga.model.urp.EPermission).WRITE_INTERFACE)")
    @DeleteMapping("/batch")
    public void batchDelete(@Valid @RequestBody InterfaceBatchDeleteDto batchDeleteDto) {
        interfaceService.batchDeleteInterfaces(batchDeleteDto.ids());
    }

    /** 用户上传API（初始禁用，待管理员审核启用） */
    @Operation(summary = "上传API", description = "注册用户自己的API定义，提交后为待审核状态，管理员审核通过后启用")
    @PostMapping("/upload")
    public InterfaceDto upload(
            @Parameter(description = "上传的接口定义") @Valid @RequestBody InterfaceUploadDto uploadDto,
            Principal principal) {
        return interfaceService.uploadInterface(uploadDto, principal.getName());
    }

    /** 在线调用：按 apiId 解析当前版本的 method/path，经网关以当前用户身份调用 */
    @Operation(summary = "在线调用API", description = "按接口ID解析当前版本路径并经网关调用，记录调用日志")
    @PostMapping("/invoke/{apiId}")
    public Object invoke(
            @Parameter(description = "接口ID", required = true)
                    @PathVariable
                    @Positive(message = "接口ID必须为正整数") Long apiId,
            @RequestBody(required = false) Object body,
            Principal principal) {
        String name = principal.getName();
        User loginUser = userRepository.fetchOneByUsername(name);
        InterfaceDto interfaceDto = interfaceService.getInterfaceById(apiId);
        if (!Boolean.TRUE.equals(interfaceDto.enabled())) {
            throw new BusinessException("接口未启用，无法调用: " + apiId);
        }
        ApiInterfaceVersion currentVersion = interfaceVersionService.fetchCurrentVersion(apiId);
        String bodyJson = body == null ? "" : toJson(body);
        long startTime = System.currentTimeMillis();
        String response =
                "GET".equalsIgnoreCase(currentVersion.getHttpMethod())
                        ? gatewayApiClient.getForBody(
                                currentVersion.getPath(),
                                loginUser.getAccessKey(),
                                loginUser.getSecretKey())
                        : gatewayApiClient.postForBody(
                                currentVersion.getPath(),
                                bodyJson,
                                loginUser.getAccessKey(),
                                loginUser.getSecretKey());
        InterfaceCallLogCreateDto interfaceCallLogCreateDto =
                new InterfaceCallLogCreateDto(
                        apiId,
                        currentVersion.getId(),
                        loginUser.getUsername(),
                        bodyJson,
                        response,
                        200,
                        true,
                        (int) (System.currentTimeMillis() - startTime));
        InterfaceCallLogDto interfaceCallLog =
                interfaceCallLogService.createInterfaceCallLog(interfaceCallLogCreateDto);
        if (interfaceCallLog == null) {
            log.error("create interface call log failed: {}", interfaceCallLogCreateDto);
        }

        return response;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("serialize request body failed", e);
        }
    }
}
