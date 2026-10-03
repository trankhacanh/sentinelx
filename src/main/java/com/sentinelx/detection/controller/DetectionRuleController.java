package com.sentinelx.detection.controller;

import com.sentinelx.common.response.ApiResponse;
import com.sentinelx.detection.dto.DetectionRuleResponse;
import com.sentinelx.detection.repository.DetectionRuleRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}