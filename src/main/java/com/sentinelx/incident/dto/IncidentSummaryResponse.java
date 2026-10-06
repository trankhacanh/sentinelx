package com.sentinelx.incident.dto;

import com.sentinelx.common.model.Severity;
import com.sentinelx.incident.entity.Incident;
import com.sentinelx.incident.entity.IncidentStatus;
import java.time.Instant;
import java.util.UUID;

/** Dùng cho danh sách — chỉ đếm số alert liên kết, không nạp chi tiết từng alert. */
public record IncidentSummaryResponse(UUID id, String title, Severity severity, IncidentStatus status,
                                      UUID assignedTo, int alertCount, Instant createdAt,
                                      Instant updatedAt, Instant resolvedAt) {

    public static IncidentSummaryResponse from(Incident incident) {
        return new IncidentSummaryResponse(incident.getId(), incident.getTitle(), incident.getSeverity(),
                incident.getStatus(), incident.getAssignedTo(), incident.getAlerts().size(),
                incident.getCreatedAt(), incident.getUpdatedAt(), incident.getResolvedAt());
    }
}