package com.sentinelx.audit.dto;

import com.sentinelx.audit.entity.AuditLog;
import com.sentinelx.audit.entity.AuditAction;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(UUID id, UUID userId, AuditAction action, String resourceType,
                               UUID resourceId, String ipAddress, Map<String, Object> metadata,
                               Instant createdAt) {
    public static AuditLogResponse from(AuditLog a) {
        return new AuditLogResponse(a.getId(), a.getUserId(), a.getAction(), a.getResourceType(),
                a.getResourceId(), a.getIpAddress(), a.getMetadata(), a.getCreatedAt());
    }
}