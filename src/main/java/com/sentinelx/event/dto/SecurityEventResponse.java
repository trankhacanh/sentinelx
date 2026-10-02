package com.sentinelx.event.dto;

import com.sentinelx.common.model.Severity;
import com.sentinelx.event.entity.EventType;
import com.sentinelx.event.entity.SecurityEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SecurityEventResponse(
        UUID id,
        Instant timestamp,
        EventType eventType,
        String source,
        String sourceIp,
        String destinationIp,
        String username,
        Severity severity,
        Map<String, Object> payload,
        Instant createdAt) {

    public static SecurityEventResponse from(SecurityEvent e) {
        return new SecurityEventResponse(e.getId(), e.getTimestamp(), e.getEventType(), e.getSource(),
                e.getSourceIp(), e.getDestinationIp(), e.getUsername(), e.getSeverity(),
                e.getPayload(), e.getCreatedAt());
    }
}