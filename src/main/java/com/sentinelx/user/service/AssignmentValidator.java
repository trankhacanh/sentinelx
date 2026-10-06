package com.sentinelx.user.service;

import com.sentinelx.common.exception.BadRequestException;
import com.sentinelx.common.exception.ResourceNotFoundException;
import com.sentinelx.user.entity.RoleName;
import com.sentinelx.user.entity.User;
import com.sentinelx.user.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Dùng chung giữa AlertService và IncidentService: một người chỉ có role VIEWER không được
 * giao nhiệm vụ điều tra (alert hay incident). Tách thành component riêng để tránh lặp lại
 * cùng một kiểm tra nghiệp vụ ở hai nơi (đặc tả mục 37: "Avoid duplicated business logic").
 */
@Component
public class AssignmentValidator {

    private final UserRepository userRepository;

    public AssignmentValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Ném ResourceNotFoundException nếu user không tồn tại, BadRequestException nếu chỉ có role VIEWER. */
    public void validateAssignable(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        boolean canInvestigate = user.getRoles().stream()
                .anyMatch(r -> r.getName() != RoleName.VIEWER);
        if (!canInvestigate) {
            throw new BadRequestException("Cannot assign to a user who only has the VIEWER role");
        }
    }
}