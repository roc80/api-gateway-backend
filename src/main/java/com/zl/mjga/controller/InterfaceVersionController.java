package com.zl.mjga.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.PageResponseDto;
import com.zl.mjga.dto.api.InterfaceVersionDto;
import com.zl.mjga.dto.api.InterfaceVersionQueryDto;
import com.zl.mjga.service.InterfaceVersionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/**
 * @author roc
 * @since 2025/12/31 17:21
 */
@Tag(name = "接口版本管理")
@RestController
@RequestMapping("/api/interfaces/versions")
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
    
}
