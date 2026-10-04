package com.sentinelx.detection.rule;

import com.sentinelx.detection.entity.RuleCode;
import com.sentinelx.detection.entity.ThreatType;
import com.sentinelx.detection.repository.ThreatRepository;
import com.sentinelx.event.entity.EventType;
import com.sentinelx.event.entity.SecurityEvent;
import com.sentinelx.event.repository.SecurityEventRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SuspiciousLoginDetector implements ThreatDetector {

    // Xem quyết định kiến trúc 2b-2f trong hướng dẫn: mỗi tham số dưới đây là MỘT tín hiệu,
    // không phải điều kiện độc lập. Ngưỡng kích hoạt tổng hợp nằm ở SIGNAL_THRESHOLD.
    private static final int SIGNAL_THRESHOLD = 2;
    private static final int UNUSUAL_HOUR_START = 0;
    private static final int UNUSUAL_HOUR_END = 5; // [00:00, 05:00) UTC coi là giờ bất thường
    private static final int FAILED_ATTEMPTS_THRESHOLD = 3;
    private static final Duration FAILED_ATTEMPTS_WINDOW = Duration.ofMinutes(5);
    private static final Set<String> SENSITIVE_USERNAMES = Set.of("admin", "root", "administrator");
    private static final Duration DEDUP_WINDOW = Duration.ofMinutes(5);

    private final SecurityEventRepository eventRepository;
    private final ThreatRepository threatRepository;

    public SuspiciousLoginDetector(SecurityEventRepository eventRepository, ThreatRepository threatRepository) {
        this.eventRepository = eventRepository;
        this.threatRepository = threatRepository;
    }

    @Override
    public RuleCode ruleCode() {
        return RuleCode.SUSPICIOUS_LOGIN;
    }

    @Override
    public boolean detect(SecurityEvent event) {
        if (event.getEventType() != EventType.LOGIN_SUCCESS || event.getUsername() == null) {
            return false;
        }

        int signalCount = 0;
        if (isNewIp(event)) {
            signalCount++;
        }
        if (isUnusualHour(event.getTimestamp())) {
            signalCount++;
        }
        if (hasRecentFailedAttempts(event)) {
            signalCount++;
        }
        if (SENSITIVE_USERNAMES.contains(event.getUsername())) {
            signalCount++;
        }

        if (signalCount < SIGNAL_THRESHOLD) {
            return false;
        }

        var windowStart = event.getTimestamp().minus(DEDUP_WINDOW);
        boolean alreadyDetected = threatRepository.existsByThreatTypeAndSourceIpAndDetectedAtBetween(
                ThreatType.SUSPICIOUS_AUTHENTICATION, event.getSourceIp(), windowStart, event.getTimestamp());

        return !alreadyDetected;
    }

    private boolean isNewIp(SecurityEvent event) {
        return !eventRepository.existsByEventTypeAndSourceIpAndUsernameAndTimestampBefore(
                EventType.LOGIN_SUCCESS, event.getSourceIp(), event.getUsername(), event.getTimestamp());
    }

    private boolean isUnusualHour(Instant timestamp) {
        int hour = timestamp.atZone(ZoneOffset.UTC).getHour();
        return hour >= UNUSUAL_HOUR_START && hour < UNUSUAL_HOUR_END;
    }

    private boolean hasRecentFailedAttempts(SecurityEvent event) {
        var windowStart = event.getTimestamp().minus(FAILED_ATTEMPTS_WINDOW);
        long failedCount = eventRepository.countByEventTypeAndUsernameAndTimestampBetween(
                EventType.LOGIN_FAILED, event.getUsername(), windowStart, event.getTimestamp());
        return failedCount >= FAILED_ATTEMPTS_THRESHOLD;
    }
}