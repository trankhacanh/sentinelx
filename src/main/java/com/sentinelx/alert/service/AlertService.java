package com.sentinelx.alert.service;

import com.sentinelx.alert.dto.AlertFilter;
import com.sentinelx.alert.dto.AlertResponse;
import com.sentinelx.alert.entity.Alert;
import com.sentinelx.alert.entity.AlertStatus;
import com.sentinelx.alert.repository.AlertRepository;
import com.sentinelx.alert.repository.AlertSpecifications;
import com.sentinelx.audit.entity.AuditAction;
import com.sentinelx.audit.service.AuditLogService;
import com.sentinelx.common.response.PageResponse;
import com.sentinelx.detection.entity.Threat;
import com.sentinelx.user.service.AssignmentValidator;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {

    private static final int MAX_PAGE_SIZE = 100;

    private final AlertRepository alertRepository;
    private final AssignmentValidator assignmentValidator;
    private final AuditLogService auditLogService;

    public AlertService(AlertRepository alertRepository, AssignmentValidator assignmentValidator,
                        AuditLogService auditLogService) {
        this.alertRepository = alertRepository;
        this.assignmentValidator = assignmentValidator;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Alert createForThreat(Threat threat) {
        Alert alert = new Alert(threat, buildTitle(threat));
        return alertRepository.save(alert);
    }

    @Transactional(readOnly = true)
    public AlertResponse getById(UUID id) {
        return AlertResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<AlertResponse> list(AlertFilter filter, int page, int size) {
        var pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Order.desc("createdAt")));
        return PageResponse.from(
                alertRepository.findAll(AlertSpecifications.matching(filter), pageable).map(AlertResponse::from));
    }

    @Transactional
    public AlertResponse updateTitle(UUID id, String title) {
        Alert alert = findOrThrow(id);
        alert.updateTitle(title);
        return AlertResponse.from(alert);
    }

    @Transactional
    public AlertResponse updateStatus(UUID id, AlertStatus status, UUID actorId) {
        Alert alert = findOrThrow(id);
        alert.updateStatus(status);

        auditLogService.record(actorId, AuditAction.ALERT_UPDATED, "Alert", id, null,
                Map.of("newStatus", status.name()));

        return AlertResponse.from(alert);
    }

    @Transactional
    public AlertResponse assign(UUID id, UUID assigneeId, UUID actorId) {
        Alert alert = findOrThrow(id);

        if (assigneeId == null) {
            alert.assignTo(null);
        } else {
            assignmentValidator.validateAssignable(assigneeId);
            alert.assignTo(assigneeId);
        }

        auditLogService.record(actorId, AuditAction.ALERT_ASSIGNED, "Alert", id, null,
                Map.of("assignedTo", assigneeId == null ? "null" : assigneeId.toString()));

        return AlertResponse.from(alert);
    }

    private Alert findOrThrow(UUID id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new com.sentinelx.common.exception.ResourceNotFoundException("Alert", id));
    }

    private String buildTitle(Threat threat) {
        String ip = threat.getSourceIp();
        return switch (threat.getThreatType()) {
            case BRUTE_FORCE -> "Brute force login attempts from " + ip;
            case PORT_SCAN -> "Port scan detected from " + ip;
            case SUSPICIOUS_AUTHENTICATION -> "Suspicious login from " + ip;
            case SQL_INJECTION -> "SQL injection attempt from " + ip;
            case HIGH_REQUEST_RATE -> "High request rate from " + ip;
        };
    }
}