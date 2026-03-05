package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

import org.jooq.generated.api_gateway.tables.pojos.ApiInterface;

/**
 * @author roc
 * @since 2025/12/26 8:32
 */
public record InterfaceCreateDto(
        @Schema(description = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "接口名称不能为空")
        @Size(max = 100, message = "接口名称长度不能超过100个字符")
        String name,

        @Schema(description = "接口描述", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 500, message = "接口描述长度不能超过500个字符")
        String description,

        @Schema(description = "接口分类", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 50, message = "接口分类长度不能超过50个字符")
        String category,

        @Schema(description = "接口所有者", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "接口所有者不能为空")
        String owner
) {
    public ApiInterface toEntity() {
        return new ApiInterface()
                .setName(name)
                .setCode(UUID.randomUUID().toString())
                .setDescription(description)
                .setCategory(category)
                .setOwner(owner);
    }
}
