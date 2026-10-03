package com.sentinelx.devtools.dto;

import java.util.List;
import java.util.UUID;

public record GenerationResult(int eventsCreated, List<UUID> eventIds) {
}