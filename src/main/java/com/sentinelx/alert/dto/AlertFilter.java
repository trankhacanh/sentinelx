package com.sentinelx.alert.dto;

import com.sentinelx.alert.entity.AlertStatus;
import com.sentinelx.common.model.Severity;
import java.util.UUID;

public record AlertFilter(AlertStatus status, Severity severity, UUID assignedTo) {
}