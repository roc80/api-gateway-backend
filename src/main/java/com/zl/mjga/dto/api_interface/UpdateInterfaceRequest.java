package com.zl.mjga.dto.api_interface;

import java.time.OffsetDateTime;

/**
 * @author roc
 * @since 2025/12/26 8:37
 */
public record UpdateInterfaceRequest(

        Long id,
        String name,
        String description,
        Boolean enabled,
        String category
) {
}
