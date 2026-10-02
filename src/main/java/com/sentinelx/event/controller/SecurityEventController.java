package com.sentinelx.event.controller;

import com.sentinelx.common.response.ApiResponse;
import com.sentinelx.common.response.PageResponse;
import com.sentinelx.event.dto.CreateSecurityEventRequest;
import com.sentinelx.event.dto.EventFilter;
import com.sentinelx.event.dto.SecurityEventResponse;
import com.sentinelx.event.service.SecurityEventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class SecurityEventController {

    private static final String CAN_READ = "hasAnyRole('ADMIN', 'SOC_ANALYST', 'SECURITY_ENGINEER')";
    private static final String CAN_INGEST = "hasAnyRole('ADMIN', 'SECURITY_ENGINEER')";

    private final SecurityEventService service;

    public SecurityEventController(SecurityEventService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize(CAN_INGEST)
    public ResponseEntity<ApiResponse<SecurityEventResponse>> create(
            @Valid @RequestBody CreateSecurityEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Event ingested", service.ingest(request)));
    }

    @GetMapping
    @PreAuthorize(CAN_READ)
    public ApiResponse<PageResponse<SecurityEventResponse>> list(
            @Valid @ModelAttribute EventFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list(filter, page, size));
    }

    /**
     * "/search" là đường dẫn cố định nên được ưu tiên hơn "/{id}".
     * q tối thiểu 3 ký tự: chuỗi ngắn hơn không tận dụng được chỉ mục trigram và khớp gần như mọi dòng.
     */
    @GetMapping("/search")
    @PreAuthorize(CAN_READ)
    public ApiResponse<PageResponse<SecurityEventResponse>> search(
            @RequestParam @NotBlank @Size(min = 3, max = 100) String q,
            @Valid @ModelAttribute EventFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.search(q, filter, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public ApiResponse<SecurityEventResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.getById(id));
    }
}