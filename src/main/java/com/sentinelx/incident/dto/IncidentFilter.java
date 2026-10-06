package com.sentinelx.incident.dto;

import com.sentinelx.common.model.Severity;
import com.sentinelx.incident.entity.IncidentStatus;
import java.util.UUID;

public record IncidentFilter(IncidentStatus status, Severity severity, UUID assignedTo) {
}