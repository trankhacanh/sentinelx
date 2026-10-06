package com.sentinelx.incident.controller;

import com.sentinelx.auth.security.UserPrincipal;
import com.sentinelx.common.response.ApiResponse;
import com.sentinelx.common.response.PageResponse;
import com.sentinelx.incident.dto.AddIncidentNoteRequest;
import com.sentinelx.incident.dto.AssignIncidentRequest;
import com.sentinelx.incident.dto.CreateIncidentRequest;
import com.sentinelx.incident.dto.IncidentDetailResponse;
import com.sentinelx.incident.dto.IncidentFilter;
import com.sentinelx.incident.dto.IncidentNoteResponse;
import com.sentinelx.incident.dto.IncidentSummaryResponse;
import com.sentinelx.incident.dto.LinkAlertRequest;
import com.sentinelx.incident.dto.UpdateIncidentRequest;
import com.sentinelx.incident.dto.UpdateIncidentStatusRequest;
import com.sentinelx.incident.service.IncidentService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** GET mở cho cả 4 role (VIEWER có quyền "View incidents"). Mọi thao tác ghi override chặt hơn. */
@RestController
@RequestMapping("/api/incidents")
@PreAuthorize("hasAnyRole('ADMIN', 'SOC_ANALYST', 'SECURITY_ENGINEER', 'VIEWER')")
public class IncidentController {

    private static final String CAN_WRITE = "hasAnyRole('ADMIN', 'SOC_ANALYST', 'SECURITY_ENGINEER')";

    private final IncidentService service;

    public IncidentController(IncidentService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public ResponseEntity<ApiResponse<IncidentDetailResponse>> create(
            @Valid @RequestBody CreateIncidentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Incident created", service.create(request)));
    }

    @GetMapping
    public ApiResponse<PageResponse<IncidentSummaryResponse>> list(
            @ModelAttribute IncidentFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list(filter, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<IncidentDetailResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public ApiResponse<IncidentDetailResponse> update(@PathVariable UUID id,
                                                      @Valid @RequestBody UpdateIncidentRequest request) {
        return ApiResponse.ok("Incident updated", service.updateDetails(id, request.title(), request.description()));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize(CAN_WRITE)
    public ApiResponse<IncidentDetailResponse> updateStatus(@PathVariable UUID id,
                                                            @Valid @RequestBody UpdateIncidentStatusRequest request) {
        return ApiResponse.ok("Incident status updated", service.updateStatus(id, request.status()));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize(CAN_WRITE)
    public ApiResponse<IncidentDetailResponse> assign(@PathVariable UUID id,
                                                       @Valid @RequestBody AssignIncidentRequest request) {
        return ApiResponse.ok("Incident assigned", service.assign(id, request.assignedTo()));
    }

    @PostMapping("/{id}/alerts")
    @PreAuthorize(CAN_WRITE)
    public ApiResponse<IncidentDetailResponse> linkAlert(@PathVariable UUID id,
                                                         @Valid @RequestBody LinkAlertRequest request) {
        return ApiResponse.ok("Alert linked", service.linkAlert(id, request.alertId()));
    }

    @PostMapping("/{id}/notes")
    @PreAuthorize(CAN_WRITE)
    public ResponseEntity<ApiResponse<IncidentNoteResponse>> addNote(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddIncidentNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Note added", service.addNote(id, principal.getId(), request.content())));
    }
}