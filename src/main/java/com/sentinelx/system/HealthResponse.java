package com.sentinelx.system;

import java.time.Instant;

public record HealthResponse(String status, String application, String database, Instant checkedAt) {
}