package com.zl.mjga.dto.api_interface;

import jakarta.validation.constraints.NotEmpty;

import java.time.OffsetDateTime;

/**
 * 
 * @since 2025/12/26 8:32
 * @author roc
 */
public record CreateInterfaceRequest(
        @NotEmpty
        String name,
        String description,
        String category,
        @NotEmpty
        String owner
) {
}
