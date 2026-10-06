package com.sentinelx.detection.rule;

import com.sentinelx.detection.entity.RuleCode;
import com.sentinelx.detection.entity.ThreatType;
import com.sentinelx.detection.repository.ThreatRepository;
import com.sentinelx.event.entity.EventType;
import com.sentinelx.event.entity.SecurityEvent;
import com.sentinelx.event.repository.SecurityEventRepository;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class BruteForceDetector implements ThreatDetector {

    private static final int FAILED_LOGIN_THRESHOLD = 5;
    private static final Duration DETECTION_WINDOW = Duration.ofSeconds(60);

    private final SecurityEventRepository eventRepository;
    private final ThreatRepository threatRepository;

    public BruteForceDetector(SecurityEventRepository eventRepository, ThreatRepository threatRepository) {
        this.eventRepository = eventRepository;
        this.threatRepository = threatRepository;
    }

    @Override
    public RuleCode ruleCode() {
        return RuleCode.BRUTE_FORCE_LOGIN;
    }

    @Override
    public boolean detect(SecurityEvent event) {
        if (event.getEventType() != EventType.LOGIN_FAILED) {
            return false;
        }

        var windowStart = event.getTimestamp().minus(DETECTION_WINDOW);

        // Từ Phase 8: DetectionRuleEngine.evaluate() chạy trong consumer RabbitMQ, SAU KHI
        // transaction ghi event đã commit thật (publish chỉ xảy ra ở AFTER_COMMIT). Event đang
        // xét vì vậy ĐÃ tồn tại và được mọi transaction khác nhìn thấy -> không cần cộng +1 thủ
        // công như giai đoạn detection chạy đồng bộ (Phase 4). Nếu sau này detection bị chuyển
        // lại thành đồng bộ trong cùng transaction ingest, phép +1 phải được khôi phục.
        long failedCount = eventRepository.countByEventTypeAndSourceIpAndTimestampBetween(
                EventType.LOGIN_FAILED, event.getSourceIp(), windowStart, event.getTimestamp());

        if (failedCount < FAILED_LOGIN_THRESHOLD) {
            return false;
        }

        boolean alreadyDetected = threatRepository.existsByThreatTypeAndSourceIpAndDetectedAtBetween(
                ThreatType.BRUTE_FORCE, event.getSourceIp(), windowStart, event.getTimestamp());

        return !alreadyDetected;
    }
}