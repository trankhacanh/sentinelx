package com.sentinelx.alert.service;

import com.sentinelx.alert.dto.AlertFilter;
import com.sentinelx.alert.dto.AlertResponse;
import com.sentinelx.alert.entity.Alert;
import com.sentinelx.alert.entity.AlertStatus;
import com.sentinelx.alert.repository.AlertRepository;
import com.sentinelx.alert.repository.AlertSpecifications;
import com.sentinelx.common.exception.BadRequestException;
import com.sentinelx.common.exception.ResourceNotFoundException;
import com.sentinelx.common.response.PageResponse;
import com.sentinelx.detection.entity.Threat;
import com.sentinelx.user.entity.RoleName;
import com.sentinelx.user.entity.User;
import com.sentinelx.user.repository.UserRepository;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {

    private static final int MAX_PAGE_SIZE = 100;

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    public AlertService(AlertRepository alertRepository, UserRepository userRepository) {
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
    }

    /**
     * Gọi từ DetectionRuleEngine, CÙNG transaction với việc lưu Threat (xem quyết định kiến trúc
     * 2a) — không dùng REQUIRES_NEW, tránh lặp lại lỗi FK-chưa-commit đã sửa ở Phase 4.
     */
    @Transactional
    public Alert createForThreat(Threat threat) {
        Alert alert = new Alert(threat, buildTitle(threat));
        return alertRepository.save(alert);
    }

    @Transactional(readOnly = true)
    public AlertResponse getById(UUID id) {
        return AlertResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<AlertResponse> list(AlertFilter filter, int page, int size) {
        var pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Order.desc("createdAt")));
        return PageResponse.from(
                alertRepository.findAll(AlertSpecifications.matching(filter), pageable).map(AlertResponse::from));
    }

    @Transactional
    public AlertResponse updateTitle(UUID id, String title) {
        Alert alert = findOrThrow(id);
        alert.updateTitle(title);
        return AlertResponse.from(alert);
    }

    @Transactional
    public AlertResponse updateStatus(UUID id, AlertStatus status) {
        Alert alert = findOrThrow(id);
        alert.updateStatus(status);
        return AlertResponse.from(alert);
    }

    @Transactional
    public AlertResponse assign(UUID id, UUID assigneeId) {
        Alert alert = findOrThrow(id);

        if (assigneeId == null) {
            alert.assignTo(null);
            return AlertResponse.from(alert);
        }

        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", assigneeId));

        // Giao việc điều tra cho một tài khoản chỉ có quyền xem (VIEWER) là vô nghĩa về nghiệp vụ.
        boolean canInvestigate = assignee.getRoles().stream()
                .anyMatch(r -> r.getName() != RoleName.VIEWER);
        if (!canInvestigate) {
            throw new BadRequestException("Cannot assign an alert to a user who only has the VIEWER role");
        }

        alert.assignTo(assigneeId);
        return AlertResponse.from(alert);
    }

    private Alert findOrThrow(UUID id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", id));
    }

    private String buildTitle(Threat threat) {
        String ip = threat.getSourceIp();
        return switch (threat.getThreatType()) {
            case BRUTE_FORCE -> "Brute force login attempts from " + ip;
            case PORT_SCAN -> "Port scan detected from " + ip;
            case SUSPICIOUS_AUTHENTICATION -> "Suspicious login from " + ip;
            case SQL_INJECTION -> "SQL injection attempt from " + ip;
            case HIGH_REQUEST_RATE -> "High request rate from " + ip;
        };
    }
}