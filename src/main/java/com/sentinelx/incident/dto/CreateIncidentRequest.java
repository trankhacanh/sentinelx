package com.sentinelx.incident.dto;

import com.sentinelx.common.model.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record CreateIncidentRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        @NotNull Severity severity,
        Set<UUID> alertIds) {

    public Set<UUID> alertIdsOrEmpty() {
        return alertIds == null ? Set.of() : alertIds;
    }
}