package com.sentinelx.event.dto;

import com.sentinelx.common.model.Severity;
import com.sentinelx.common.validation.ValidIpAddress;
import com.sentinelx.event.entity.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;

/**
 * Không có id và createdAt: client không được quyết định hai giá trị này.
 * Field lạ trong JSON (ví dụ "id") sẽ bị bỏ qua.
 */
public record CreateSecurityEventRequest(
        @NotNull Instant timestamp,
        @NotNull EventType eventType,
        @NotBlank @Size(max = 100) String source,
        @NotBlank @ValidIpAddress String sourceIp,
        @ValidIpAddress String destinationIp,
        @Size(max = 100) String username,
        @NotNull Severity severity,
        Map<String, Object> payload) {
}