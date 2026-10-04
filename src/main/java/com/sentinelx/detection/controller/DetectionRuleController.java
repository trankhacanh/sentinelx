package com.sentinelx.detection.controller;

import com.sentinelx.common.response.ApiResponse;
import com.sentinelx.detection.dto.DetectionRuleResponse;
import com.sentinelx.detection.repository.DetectionRuleRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sentinelx.common.exception.ResourceNotFoundException;
import com.sentinelx.detection.dto.UpdateRuleEnabledRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Chỉ đọc ở bước này. CRUD (tạo/sửa/bật-tắt rule) sẽ thêm khi cần, cùng Bước 4.2. */
@RestController
@RequestMapping("/api/detection-rules")
@PreAuthorize("hasAnyRole('ADMIN', 'SECURITY_ENGINEER')")
public class DetectionRuleController {

    private final DetectionRuleRepository repository;

    public DetectionRuleController(DetectionRuleRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ApiResponse<List<DetectionRuleResponse>> list() {
        return ApiResponse.ok(repository.findAll().stream().map(DetectionRuleResponse::from).toList());
    }

        /**
     * Chỉ bật/tắt rule ở bước này — đủ để test Detection Engine phản ứng ngay khi rule bị tắt.
     * CRUD đầy đủ (sửa ngưỡng, risk, tạo rule mới) sẽ thêm nếu một phase sau cần tới.
     */
    @PutMapping("/{id}/enabled")
    @Transactional
    public ApiResponse<DetectionRuleResponse> setEnabled(@PathVariable UUID id,
                                                         @Valid @RequestBody UpdateRuleEnabledRequest request) {
        var rule = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detection rule", id));
        rule.setEnabled(request.enabled());
        // Entity đang managed trong transaction -> dirty checking tự UPDATE khi commit, không cần save()
        return ApiResponse.ok("Rule updated", DetectionRuleResponse.from(rule));
    }
    
}