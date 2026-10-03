package com.sentinelx.detection.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateRuleEnabledRequest(@NotNull Boolean enabled) {
}