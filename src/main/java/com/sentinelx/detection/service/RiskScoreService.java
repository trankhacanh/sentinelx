package com.sentinelx.detection.service;

import com.sentinelx.detection.entity.DetectionRule;
import com.sentinelx.detection.repository.ThreatRepository;
import com.sentinelx.event.entity.SecurityEvent;
import java.time.Clock;
import java.time.Duration;
import org.springframework.stereotype.Service;

/**
 * Risk Score = Base Score + Frequency + Target Sensitivity (đặc tả mục 11, MVP).
 * Source Reputation và Behavior Anomaly chưa triển khai — cần threat intelligence feed / ML,
 * thuộc Phase 15 (Advanced Features).
 */
@Service
public class RiskScoreService {

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;

    // Frequency: mỗi lần tái phạm (cùng rule, cùng IP) trong cửa sổ cộng thêm, có giới hạn trên
    // để một kẻ bị phát hiện rất nhiều lần không kéo điểm vượt xa ý nghĩa của thang 100.
    private static final Duration FREQUENCY_WINDOW = Duration.ofHours(24);
    private static final int FREQUENCY_POINTS_PER_PRIOR_THREAT = 5;
    private static final int FREQUENCY_CAP = 20;

    private static final int TARGET_SENSITIVITY_POINTS = 15;

    private final ThreatRepository threatRepository;
    private final SensitiveAccounts sensitiveAccounts;
    private final Clock clock;

    public RiskScoreService(ThreatRepository threatRepository, SensitiveAccounts sensitiveAccounts, Clock clock) {
        this.threatRepository = threatRepository;
        this.sensitiveAccounts = sensitiveAccounts;
        this.clock = clock;
    }

    public int calculate(DetectionRule rule, SecurityEvent event) {
        int base = rule.getBaseRiskScore();
        int frequency = frequencyComponent(rule, event);
        int targetSensitivity = targetSensitivityComponent(event);

        int total = base + frequency + targetSensitivity;
        return clamp(total);
    }

    private int frequencyComponent(DetectionRule rule, SecurityEvent event) {
        var windowStart = clock.instant().minus(FREQUENCY_WINDOW);
        long priorThreatCount = threatRepository.countByRule_IdAndSourceIpAndDetectedAtAfter(
                rule.getId(), event.getSourceIp(), windowStart);
        return (int) Math.min(priorThreatCount * FREQUENCY_POINTS_PER_PRIOR_THREAT, FREQUENCY_CAP);
    }

    private int targetSensitivityComponent(SecurityEvent event) {
        return sensitiveAccounts.contains(event.getUsername()) ? TARGET_SENSITIVITY_POINTS : 0;
    }

    private int clamp(int score) {
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
    }
}