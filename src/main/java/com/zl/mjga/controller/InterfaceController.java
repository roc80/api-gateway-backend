package com.zl.mjga.controller;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.PageResponseDto;
import com.zl.mjga.dto.api.*;
import com.zl.mjga.service.InterfaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
public class InterfaceController {

    private final InterfaceService interfaceService;

    /** 创建接口 */
    @Operation(summary = "创建接口", description = "创建新的接口信息")
    @PostMapping
    public InterfaceDto create(@Valid @RequestBody InterfaceCreateDto createDto) {
        return interfaceService.createInterface(createDto);
    }

    /** 更新接口 */
    @Operation(summary = "更新接口", description = "根据 ID 更新接口信息")
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
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "接口ID", required = true)
                    @PathVariable
                    @Positive(message = "接口ID必须为正整数") Long id) {
        interfaceService.deleteInterface(id);
    }

    /** 批量删除接口 */
    @Operation(summary = "批量删除接口", description = "根据 ID 列表批量删除接口")
    @DeleteMapping("/batch")
    public void batchDelete(@Valid @RequestBody InterfaceBatchDeleteDto batchDeleteDto) {
        interfaceService.batchDeleteInterfaces(batchDeleteDto.ids());
    }
}
