package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.Optional;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterface;

/**
 * @author roc
 * @since 2025/12/26 8:37
 */
public record InterfaceUpdateDto(
        @NotBlank(message = "接口名称不能为空") @Size(max = 100, message = "接口名称长度不能超过100个字符") String name,
        @Size(max = 500, message = "接口描述长度不能超过500个字符") String description,
        @Schema(description = "接口分类") @Size(max = 50, message = "接口分类长度不能超过50个字符") String category) {
    public void applyTo(ApiInterface entity) {
        Optional.ofNullable(name).ifPresent(entity::setName);
        Optional.ofNullable(description).ifPresent(entity::setDescription);
        Optional.ofNullable(category).ifPresent(entity::setCategory);
    }
}
