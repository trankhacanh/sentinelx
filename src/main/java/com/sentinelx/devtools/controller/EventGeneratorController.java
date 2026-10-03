package com.sentinelx.devtools.controller;

import com.sentinelx.common.response.ApiResponse;
import com.sentinelx.devtools.dto.GenerateBruteForceRequest;
import com.sentinelx.devtools.dto.GenerateNoiseRequest;
import com.sentinelx.devtools.dto.GenerationResult;
import com.sentinelx.devtools.service.EventGeneratorService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sentinelx.devtools.dto.GeneratePortScanRequest;

/**
 * Chỉ đăng ký khi profile "dev" đang bật. Nếu chạy không có profile này (ví dụ production),
 * Spring KHÔNG tạo bean này -> endpoint không tồn tại -> request trả 404, không phải 403.
 * Đây là lớp phòng thủ "secure by default": sai cấu hình sẽ lộ ra ngay (404 bất ngờ) thay vì
 * âm thầm cho phép sinh dữ liệu giả trên production.
 */
@RestController
@RequestMapping("/api/dev/event-generator")
@Profile("dev")
@PreAuthorize("hasRole('ADMIN')")
public class EventGeneratorController {

    private final EventGeneratorService service;

    public EventGeneratorController(EventGeneratorService service) {
        this.service = service;
    }

    @PostMapping("/brute-force")
    public ApiResponse<GenerationResult> bruteForce(@Valid @RequestBody GenerateBruteForceRequest request) {
        return ApiResponse.ok("Brute force events generated", service.generateBruteForce(request));
    }

    @PostMapping("/noise")
    public ApiResponse<GenerationResult> noise(@Valid @RequestBody GenerateNoiseRequest request) {
        return ApiResponse.ok("Noise events generated", service.generateNoise(request));
    }

        @PostMapping("/port-scan")
    public ApiResponse<GenerationResult> portScan(@Valid @RequestBody GeneratePortScanRequest request) {
        return ApiResponse.ok("Port scan events generated", service.generatePortScan(request));
    }
    
}