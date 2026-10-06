package com.sentinelx.incident.dto;

import com.sentinelx.incident.entity.IncidentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateIncidentStatusRequest(@NotNull IncidentStatus status) {
}