package com.sentinelx.detection.dto;

import com.sentinelx.common.model.Severity;
import com.sentinelx.detection.entity.DetectionRule;
import com.sentinelx.detection.entity.RuleCode;
import com.sentinelx.detection.entity.ThreatType;
import java.time.Instant;
import java.util.UUID;

public record DetectionRuleResponse(UUID id, RuleCode ruleCode, String name, String description,
                                    ThreatType threatType, Severity severity, int baseRiskScore,
                                    boolean enabled, Instant createdAt) {

    public static DetectionRuleResponse from(DetectionRule r) {
        return new DetectionRuleResponse(r.getId(), r.getRuleCode(), r.getName(), r.getDescription(),
                r.getThreatType(), r.getSeverity(), r.getBaseRiskScore(), r.isEnabled(), r.getCreatedAt());
    }
}