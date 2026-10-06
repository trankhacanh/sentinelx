package com.sentinelx.incident.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LinkAlertRequest(@NotNull UUID alertId) {
}