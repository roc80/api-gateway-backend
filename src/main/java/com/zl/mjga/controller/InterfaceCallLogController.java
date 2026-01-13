package com.zl.mjga.controller;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.PageResponseDto;
import com.zl.mjga.dto.api.InterfaceCallLogDto;
import com.zl.mjga.dto.api.InterfaceCallLogQueryDto;
import com.zl.mjga.service.InterfaceCallLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/interfaces/logs")
@RequiredArgsConstructor
@Validated
public class InterfaceCallLogController {
    private final InterfaceCallLogService interfaceCallLogService;

    /** 根据 ID 查询 */
    @Operation(summary = "查询接口调用日志详情", description = "根据 ID 查询接口调用日志详细信息")
    @GetMapping("/{id}")
    public InterfaceCallLogDto getById(
            @Parameter(description = "接口调用日志ID", required = true)
                    @PathVariable
                    @Positive(message = "接口调用日志ID必须为正整数") Long id) {
        return interfaceCallLogService.getInterfaceCallLogById(id);
    }

    /** 分页查询 */
    @Operation(summary = "分页查询接口调用日志列表", description = "根据条件分页查询接口调用日志列表")
    @PostMapping("/search")
    public PageResponseDto<List<InterfaceCallLogDto>> search(
            @Valid @RequestBody PageRequestDto<InterfaceCallLogQueryDto> pageRequestDto) {
        return interfaceCallLogService.searchInterfaceCallLogs(pageRequestDto);
    }
}
