package com.sentinelx.incident.dto;

import java.util.UUID;

/** assignedTo = null nghĩa là hủy gán, giống AssignAlertRequest. */
public record AssignIncidentRequest(UUID assignedTo) {
}