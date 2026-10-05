package com.sentinelx.alert.dto;

import com.sentinelx.alert.entity.Alert;
import com.sentinelx.alert.entity.AlertStatus;
import com.sentinelx.common.model.Severity;
import com.sentinelx.detection.entity.ThreatType;
import java.time.Instant;
import java.util.UUID;

/** severity/riskScore/sourceIp/threatType đọc từ Threat liên kết, không lưu trùng trong Alert (xem quyết định 2b). */
public record AlertResponse(
        UUID id, UUID threatId, ThreatType threatType, String sourceIp,
        String title, Severity severity, int riskScore, AlertStatus status,
        UUID assignedTo, Instant createdAt, Instant updatedAt) {

    public static AlertResponse from(Alert alert) {
        var threat = alert.getThreat();
        return new AlertResponse(alert.getId(), threat.getId(), threat.getThreatType(), threat.getSourceIp(),
                alert.getTitle(), threat.getSeverity(), threat.getRiskScore(), alert.getStatus(),
                alert.getAssignedTo(), alert.getCreatedAt(), alert.getUpdatedAt());
    }
}