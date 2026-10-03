package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用户上传API的请求体：接口基础信息 + 首个版本定义
 *
 * @author roc
 * @since 2026/10/3
 */
public record InterfaceUploadDto(
        @Schema(description = "接口唯一标识", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "接口标识不能为空") @Pattern(
                        regexp = "^[a-zA-Z][a-zA-Z0-9_-]{1,63}$",
                        message = "接口标识需以字母开头，仅含字母数字、下划线和中划线，长度2-64")
                String code,
        @Schema(description = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "接口名称不能为空") @Size(max = 100, message = "接口名称长度不能超过100个字符") String name,
        @Schema(description = "接口描述") @Size(max = 500, message = "接口描述长度不能超过500个字符") String description,
        @Schema(description = "接口分类") @Size(max = 100, message = "接口分类长度不能超过100个字符") String category,
        @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "版本号不能为空") @Pattern(regexp = "^v\\d+(\\.\\d+){0,2}$", message = "版本号格式：v1 / v1.0 / v1.0.0")
                String version,
        @Schema(description = "HTTP请求方法", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "HTTP请求方法不能为空") @Pattern(
                        regexp = "GET|POST|PUT|DELETE|PATCH",
                        message = "HTTP请求方法仅支持 GET/POST/PUT/DELETE/PATCH")
                String httpMethod,
        @Schema(description = "HTTP请求路径", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "HTTP请求路径不能为空") @Pattern(
                        regexp = "^/[a-zA-Z0-9/_-]{1,254}$",
                        message = "请求路径需以/开头，仅含字母数字、斜杠、下划线和中划线")
                String path,
        @Schema(description = "请求体示例") @Size(max = 2000, message = "请求体示例长度不能超过2000个字符") String requestBody,
        @Schema(description = "响应示例") @Size(max = 2000, message = "响应示例长度不能超过2000个字符") String responseExample) {}
