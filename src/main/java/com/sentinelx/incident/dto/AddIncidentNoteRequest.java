package com.sentinelx.incident.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddIncidentNoteRequest(@NotBlank @Size(max = 5000) String content) {
}