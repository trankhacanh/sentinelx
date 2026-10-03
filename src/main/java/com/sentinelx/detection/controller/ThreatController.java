package com.sentinelx.detection.controller;

import com.sentinelx.common.response.ApiResponse;
import com.sentinelx.common.response.PageResponse;
import com.sentinelx.detection.dto.ThreatFilter;
import com.sentinelx.detection.dto.ThreatResponse;
import com.sentinelx.detection.dto.ThreatStatisticsResponse;
import com.sentinelx.detection.service.ThreatService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/threats")
@PreAuthorize("hasAnyRole('ADMIN', 'SOC_ANALYST', 'SECURITY_ENGINEER')")
public class ThreatController {

    private final ThreatService service;

    public ThreatController(ThreatService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResponse<ThreatResponse>> list(
            @Valid @ModelAttribute ThreatFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list(filter, page, size));
    }

    /** "/statistics" là đường dẫn cố định, phải khai báo trước "/{id}" để không bị hiểu nhầm thành id. */
    @GetMapping("/statistics")
    public ApiResponse<ThreatStatisticsResponse> statistics(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        return ApiResponse.ok(service.statistics(from, to));
    }

    @GetMapping("/{id}")
    public ApiResponse<ThreatResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.getById(id));
    }
}