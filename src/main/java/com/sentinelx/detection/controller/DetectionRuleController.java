package com.sentinelx.detection.controller;

import com.sentinelx.audit.entity.AuditAction;
import com.sentinelx.audit.service.AuditLogService;
import com.sentinelx.auth.security.UserPrincipal;
import com.sentinelx.common.exception.ResourceNotFoundException;
import com.sentinelx.common.response.ApiResponse;
import com.sentinelx.detection.dto.DetectionRuleResponse;
import com.sentinelx.detection.dto.UpdateRuleEnabledRequest;
import com.sentinelx.detection.repository.DetectionRuleRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/detection-rules")
@PreAuthorize("hasAnyRole('ADMIN', 'SECURITY_ENGINEER')")
public class DetectionRuleController {

    private final DetectionRuleRepository repository;
    private final AuditLogService auditLogService;

    public DetectionRuleController(DetectionRuleRepository repository, AuditLogService auditLogService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<DetectionRuleResponse>> list() {
        return ApiResponse.ok(repository.findAll().stream().map(DetectionRuleResponse::from).toList());
    }

    @PutMapping("/{id}/enabled")
    @Transactional
    @CacheEvict(cacheNames = "detectionRules", key = "#result.data.ruleCode")
    public ApiResponse<DetectionRuleResponse> setEnabled(@PathVariable UUID id,
                                                         @Valid @RequestBody UpdateRuleEnabledRequest request,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        var rule = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detection rule", id));
        rule.setEnabled(request.enabled());

        auditLogService.record(principal.getId(), AuditAction.RULE_UPDATED, "DetectionRule", id, null,
                Map.of("ruleCode", rule.getRuleCode().name(), "enabled", request.enabled()));

        return ApiResponse.ok("Rule updated", DetectionRuleResponse.from(rule));
    }
}