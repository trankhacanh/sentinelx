package com.sentinelx.detection.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ThreatStatisticsResponse(
        Instant from,
        Instant to,
        long totalThreats,
        Map<String, Long> bySeverity,
        Map<String, Long> byType,
        List<SourceIpCount> topSourceIps) {
}