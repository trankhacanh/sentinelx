package com.sentinelx.alert.controller;

import com.sentinelx.alert.dto.AlertFilter;
import com.sentinelx.alert.dto.AlertResponse;
import com.sentinelx.alert.dto.AssignAlertRequest;
import com.sentinelx.alert.dto.UpdateAlertStatusRequest;
import com.sentinelx.alert.dto.UpdateAlertTitleRequest;
import com.sentinelx.alert.service.AlertService;
import com.sentinelx.common.response.ApiResponse;
import com.sentinelx.common.response.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RBAC khác Threat (xem quyết định 2e): VIEWER được XEM alert (đặc tả liệt kê "View alerts" cho
 * VIEWER), nhưng không được sửa. Class-level permitAll-4-role chỉ áp dụng cho GET; các PUT override
 * bằng @PreAuthorize method-level chặt hơn (Spring Security: method-level thay thế hoàn toàn
 * class-level, không cộng dồn).
 */
@RestController
@RequestMapping("/api/alerts")
@PreAuthorize("hasAnyRole('ADMIN', 'SOC_ANALYST', 'SECURITY_ENGINEER', 'VIEWER')")
public class AlertController {

    private static final String CAN_WRITE = "hasAnyRole('ADMIN', 'SOC_ANALYST', 'SECURITY_ENGINEER')";

    private final AlertService service;

    public AlertController(AlertService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResponse<AlertResponse>> list(
            @ModelAttribute AlertFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list(filter, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<AlertResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public ApiResponse<AlertResponse> updateTitle(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateAlertTitleRequest request) {
        return ApiResponse.ok("Alert updated", service.updateTitle(id, request.title()));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize(CAN_WRITE)
    public ApiResponse<AlertResponse> updateStatus(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateAlertStatusRequest request) {
        return ApiResponse.ok("Alert status updated", service.updateStatus(id, request.status()));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize(CAN_WRITE)
    public ApiResponse<AlertResponse> assign(@PathVariable UUID id,
                                             @Valid @RequestBody AssignAlertRequest request) {
        return ApiResponse.ok("Alert assigned", service.assign(id, request.assignedTo()));
    }
}