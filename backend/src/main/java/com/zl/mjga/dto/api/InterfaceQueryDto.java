package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/**
 * @author roc
 * @since 2025/12/28 19:21
 */
public record InterfaceQueryDto(
        @Schema(description = "接口名称") String name,
        @Schema(description = "接口唯一标识") String code,
        @Schema(description = "接口描述") String description,
        @Schema(description = "接口是否启用") Boolean enabled,
        @Schema(description = "接口分类") String category,
        @Schema(description = "接口所有者") String owner,
        @Schema(description = "创建时间开始") OffsetDateTime createTimeStart,
        @Schema(description = "创建时间结束") OffsetDateTime createTimeEnd,
        @Schema(description = "更新时间开始") OffsetDateTime updateTimeStart,
        @Schema(description = "更新时间结束") OffsetDateTime updateTimeEnd) {}
