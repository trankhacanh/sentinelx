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
public class HighRequestRateDetector implements ThreatDetector {

    private static final int REQUEST_THRESHOLD = 100;
    private static final Duration DETECTION_WINDOW = Duration.ofSeconds(10);

    private final SecurityEventRepository eventRepository;
    private final ThreatRepository threatRepository;

    public HighRequestRateDetector(SecurityEventRepository eventRepository, ThreatRepository threatRepository) {
        this.eventRepository = eventRepository;
        this.threatRepository = threatRepository;
    }

    @Override
    public RuleCode ruleCode() {
        return RuleCode.HIGH_REQUEST_RATE;
    }

    @Override
    public boolean detect(SecurityEvent event) {
        if (event.getEventType() != EventType.HTTP_REQUEST) {
            return false;
        }

        var windowStart = event.getTimestamp().minus(DETECTION_WINDOW);

        // Từ Phase 8: event hiện tại đã commit thật, không còn cộng +1 thủ công như Phase 4.
        long requestCount = eventRepository.countByEventTypeAndSourceIpAndTimestampBetween(
                EventType.HTTP_REQUEST, event.getSourceIp(), windowStart, event.getTimestamp());

        if (requestCount < REQUEST_THRESHOLD) {
            return false;
        }

        boolean alreadyDetected = threatRepository.existsByThreatTypeAndSourceIpAndDetectedAtBetween(
                ThreatType.HIGH_REQUEST_RATE, event.getSourceIp(), windowStart, event.getTimestamp());

        return !alreadyDetected;
    }
}