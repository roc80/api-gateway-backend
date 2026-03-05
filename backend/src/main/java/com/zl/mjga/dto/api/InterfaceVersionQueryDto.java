package com.zl.mjga.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/**
 * @author roc
 * @since 2025/12/31 17:44
 */
public record InterfaceVersionQueryDto(
        @Schema(description = "接口id") Long apiId,
        @Schema(description = "接口版本") String version,
        @Schema(description = "是否是当前版本") Boolean current,
        @Schema(description = "HTTP请求方法") String httpMethod,
        @Schema(description = "HTTP请求路径") String path,
        @Schema(description = "HTTP请求头") String requestHeaders,
        @Schema(description = "认证方式") String authType,
        @Schema(description = "是否允许调用") Boolean allowInvoke,
        @Schema(description = "创建时间开始") OffsetDateTime createTimeStart,
        @Schema(description = "创建时间结束") OffsetDateTime createTimeEnd,
        @Schema(description = "更新时间开始") OffsetDateTime updateTimeStart,
        @Schema(description = "更新时间结束") OffsetDateTime updateTimeEnd) {}
