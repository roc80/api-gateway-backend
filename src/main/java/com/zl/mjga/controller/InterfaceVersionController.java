package com.zl.mjga.controller;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.PageResponseDto;
import com.zl.mjga.dto.api.InterfaceVersionBatchDeleteDto;
import com.zl.mjga.dto.api.InterfaceVersionCreateDto;
import com.zl.mjga.dto.api.InterfaceVersionDto;
import com.zl.mjga.dto.api.InterfaceVersionQueryDto;
import com.zl.mjga.dto.api.InterfaceVersionUpdateDto;
import com.zl.mjga.service.InterfaceVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author roc
 * @since 2025/12/31 17:21
 */
@RestController
@RequestMapping("/interfaces/versions")
@RequiredArgsConstructor
@Validated
public class InterfaceVersionController {
    private final InterfaceVersionService interfaceVersionService;

    /** 根据 ID 查询 */
    @Operation(summary = "查询接口版本详情", description = "根据 ID 查询接口版本详细信息")
    @GetMapping("/{id}")
    public InterfaceVersionDto getById(
            @Parameter(description = "接口版本ID", required = true)
                    @PathVariable
                    @Positive(message = "接口版本ID必须为正整数") Long id) {
        return interfaceVersionService.getInterfaceVersionById(id);
    }

    /** 分页查询 */
    @Operation(summary = "分页查询接口版本列表", description = "根据条件分页查询接口版本列表")
    @PostMapping("/search")
    public PageResponseDto<List<InterfaceVersionDto>> search(
            @Valid @RequestBody PageRequestDto<InterfaceVersionQueryDto> pageRequestDto) {
        return interfaceVersionService.searchInterfaceVersion(pageRequestDto);
    }

    /** 更新接口版本 */
    @Operation(summary = "更新接口版本")
    @PostMapping("/{id}")
    public InterfaceVersionDto update(
            @Parameter(description = "接口版本ID", required = true)
                    @PathVariable
                    @Positive(message = "接口版本ID必须为正整数") Long id,
            @Valid @RequestBody InterfaceVersionUpdateDto interfaceVersionUpdateDto) {
        return interfaceVersionService.updateInterfaceVersion(id, interfaceVersionUpdateDto);
    }

    /** 删除接口版本 */
    @Operation(summary = "删除接口版本")
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "接口版本ID", required = true)
                    @Positive(message = "接口版本ID必须为正整数") @PathVariable
                    Long id) {
        interfaceVersionService.deleteInterfaceVersion(id);
    }

    /** 批量删除接口版本 */
    @Operation(summary = "批量删除接口版本")
    @DeleteMapping("/batch")
    public void batchDelete(@Valid @RequestBody InterfaceVersionBatchDeleteDto batchDeleteDto) {
        interfaceVersionService.batchDeleteInterfaceVersion(batchDeleteDto);
    }

    /** 新增接口版本 */
    @Operation(summary = "新增接口版本")
    @PostMapping
    public InterfaceVersionDto create(
            @Valid @RequestBody InterfaceVersionCreateDto interfaceVersionCreateDto) {
        return interfaceVersionService.createInterfaceVersion(interfaceVersionCreateDto);
    }
}
