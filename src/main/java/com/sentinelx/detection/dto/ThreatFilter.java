package com.sentinelx.detection.dto;

import com.sentinelx.common.model.Severity;
import com.sentinelx.detection.entity.ThreatType;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record ThreatFilter(
        ThreatType threatType,
        Severity severity,
        @Size(max = 45) String sourceIp,
        Instant from,
        Instant to) {
}