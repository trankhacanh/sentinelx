package com.sentinelx.incident.dto;

import com.sentinelx.alert.dto.AlertResponse;
import com.sentinelx.common.model.Severity;
import com.sentinelx.incident.entity.Incident;
import com.sentinelx.incident.entity.IncidentStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record IncidentDetailResponse(UUID id, String title, String description, Severity severity,
                                     IncidentStatus status, UUID assignedTo, List<AlertResponse> alerts,
                                     List<IncidentNoteResponse> notes, Instant createdAt,
                                     Instant updatedAt, Instant resolvedAt) {

    public static IncidentDetailResponse from(Incident incident, List<IncidentNoteResponse> notes) {
        List<AlertResponse> alertResponses = incident.getAlerts().stream()
                .map(AlertResponse::from)
                .toList();
        return new IncidentDetailResponse(incident.getId(), incident.getTitle(), incident.getDescription(),
                incident.getSeverity(), incident.getStatus(), incident.getAssignedTo(), alertResponses, notes,
                incident.getCreatedAt(), incident.getUpdatedAt(), incident.getResolvedAt());
    }
}