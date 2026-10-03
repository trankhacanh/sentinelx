package com.sentinelx.detection.dto;

import com.sentinelx.common.model.Severity;
import com.sentinelx.detection.entity.RuleCode;
import com.sentinelx.detection.entity.Threat;
import com.sentinelx.detection.entity.ThreatType;
import java.time.Instant;
import java.util.UUID;

public record ThreatResponse(UUID id, UUID eventId, RuleCode ruleCode, ThreatType threatType,
                             Severity severity, int riskScore, String sourceIp,
                             String description, Instant detectedAt) {

    public static ThreatResponse from(Threat t) {
        return new ThreatResponse(t.getId(), t.getEventId(), t.getRule().getRuleCode(),
                t.getThreatType(), t.getSeverity(), t.getRiskScore(), t.getSourceIp(),
                t.getDescription(), t.getDetectedAt());
    }
}