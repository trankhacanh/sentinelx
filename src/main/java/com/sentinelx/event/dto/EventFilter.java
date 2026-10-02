package com.sentinelx.event.dto;

import com.sentinelx.common.model.Severity;
import com.sentinelx.event.entity.EventType;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Các tham số lọc từ query string. Mọi trường đều tùy chọn. from/to đều bao gồm (inclusive). */
public record EventFilter(
        EventType eventType,
        Severity severity,
        @Size(max = 45) String sourceIp,
        @Size(max = 100) String username,
        Instant from,
        Instant to) {
}