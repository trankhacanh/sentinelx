package com.sentinelx.incident.service;

import com.sentinelx.alert.entity.Alert;
import com.sentinelx.alert.repository.AlertRepository;
import com.sentinelx.audit.entity.AuditAction;
import com.sentinelx.audit.service.AuditLogService;
import com.sentinelx.common.exception.ResourceNotFoundException;
import com.sentinelx.common.response.PageResponse;
import com.sentinelx.incident.dto.CreateIncidentRequest;
import com.sentinelx.incident.dto.IncidentDetailResponse;
import com.sentinelx.incident.dto.IncidentFilter;
import com.sentinelx.incident.dto.IncidentNoteResponse;
import com.sentinelx.incident.dto.IncidentSummaryResponse;
import com.sentinelx.incident.entity.Incident;
import com.sentinelx.incident.entity.IncidentNote;
import com.sentinelx.incident.entity.IncidentStatus;
import com.sentinelx.incident.repository.IncidentNoteRepository;
import com.sentinelx.incident.repository.IncidentRepository;
import com.sentinelx.incident.repository.IncidentSpecifications;
import com.sentinelx.user.service.AssignmentValidator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentService {

    private static final int MAX_PAGE_SIZE = 100;

    private final IncidentRepository incidentRepository;
    private final IncidentNoteRepository noteRepository;
    private final AlertRepository alertRepository;
    private final AssignmentValidator assignmentValidator;
    private final AuditLogService auditLogService;

    public IncidentService(IncidentRepository incidentRepository, IncidentNoteRepository noteRepository,
                           AlertRepository alertRepository, AssignmentValidator assignmentValidator,
                           AuditLogService auditLogService) {
        this.incidentRepository = incidentRepository;
        this.noteRepository = noteRepository;
        this.alertRepository = alertRepository;
        this.assignmentValidator = assignmentValidator;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public IncidentDetailResponse create(CreateIncidentRequest request, UUID actorId) {
        Incident incident = new Incident(request.title(), request.description(), request.severity());
        for (UUID alertId : request.alertIdsOrEmpty()) {
            incident.linkAlert(findAlertOrThrow(alertId));
        }
        Incident saved = incidentRepository.saveAndFlush(incident);

        auditLogService.record(actorId, AuditAction.INCIDENT_CREATED, "Incident", saved.getId(), null,
                Map.of("title", request.title()));

        return IncidentDetailResponse.from(saved, List.of());
    }

    @Transactional(readOnly = true)
    public PageResponse<IncidentSummaryResponse> list(IncidentFilter filter, int page, int size) {
        var pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Order.desc("createdAt")));
        return PageResponse.from(incidentRepository.findAll(IncidentSpecifications.matching(filter), pageable)
                .map(IncidentSummaryResponse::from));
    }

    @Transactional(readOnly = true)
    public IncidentDetailResponse getById(UUID id) {
        Incident incident = findOrThrow(id);
        List<IncidentNoteResponse> notes = noteRepository.findByIncident_IdOrderByCreatedAtAsc(id).stream()
                .map(IncidentNoteResponse::from)
                .toList();
        return IncidentDetailResponse.from(incident, notes);
    }

    @Transactional
    public IncidentDetailResponse updateDetails(UUID id, String title, String description) {
        Incident incident = findOrThrow(id);
        incident.updateDetails(title, description);
        return getById(id);
    }

    @Transactional
    public IncidentDetailResponse updateStatus(UUID id, IncidentStatus status, UUID actorId) {
        Incident incident = findOrThrow(id);
        incident.updateStatus(status);

        auditLogService.record(actorId, AuditAction.INCIDENT_STATUS_CHANGED, "Incident", id, null,
                Map.of("newStatus", status.name()));

        return getById(id);
    }

    @Transactional
    public IncidentDetailResponse assign(UUID id, UUID assigneeId, UUID actorId) {
        Incident incident = findOrThrow(id);
        if (assigneeId == null) {
            incident.assignTo(null);
        } else {
            assignmentValidator.validateAssignable(assigneeId);
            incident.assignTo(assigneeId);
        }

        auditLogService.record(actorId, AuditAction.INCIDENT_ASSIGNED, "Incident", id, null,
                Map.of("assignedTo", assigneeId == null ? "null" : assigneeId.toString()));

        return getById(id);
    }

    @Transactional
    public IncidentDetailResponse linkAlert(UUID id, UUID alertId) {
        Incident incident = findOrThrow(id);
        incident.linkAlert(findAlertOrThrow(alertId));
        return getById(id);
    }

    @Transactional
    public IncidentNoteResponse addNote(UUID id, UUID authorId, String content) {
        Incident incident = findOrThrow(id);
        IncidentNote note = new IncidentNote(incident, authorId, content);
        return IncidentNoteResponse.from(noteRepository.save(note));
    }

    private Incident findOrThrow(UUID id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));
    }

    private Alert findAlertOrThrow(UUID alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
    }
}