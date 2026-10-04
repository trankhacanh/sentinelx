package com.sentinelx.detection.service;

import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Danh sách tài khoản "nhạy cảm" dùng chung giữa SuspiciousLoginDetector (một trong các tín hiệu)
 * và RiskScoreService (thành phần Target Sensitivity) — tách ra một nơi duy nhất để tránh lệch
 * dữ liệu nếu sau này chỉ một nơi được cập nhật.
 */
@Component
public class SensitiveAccounts {

    private static final Set<String> SENSITIVE_USERNAMES = Set.of("admin", "root", "administrator");

    public boolean contains(String username) {
        return username != null && SENSITIVE_USERNAMES.contains(username);
    }
}