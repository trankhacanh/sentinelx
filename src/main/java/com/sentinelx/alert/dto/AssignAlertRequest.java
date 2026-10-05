package com.sentinelx.alert.dto;

import java.util.UUID;

/** assignedTo = null nghĩa là hủy gán (unassign) — cố ý KHÔNG @NotNull. */
public record AssignAlertRequest(UUID assignedTo) {
}