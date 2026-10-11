package com.sentinelx.audit.controller;

import com.sentinelx.audit.dto.AuditLogFilter;
import com.sentinelx.audit.dto.AuditLogResponse;
import com.sentinelx.audit.repository.AuditLogRepository;
import com.sentinelx.audit.repository.AuditLogSpecifications;
import com.sentinelx.common.response.ApiResponse;
import com.sentinelx.common.response.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogRepository repository;

    public AuditLogController(AuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ApiResponse<PageResponse<AuditLogResponse>> list(
            @ModelAttribute AuditLogFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Order.desc("createdAt")));
        return ApiResponse.ok(PageResponse.from(
                repository.findAll(AuditLogSpecifications.matching(filter), pageable).map(AuditLogResponse::from)));
    }
}