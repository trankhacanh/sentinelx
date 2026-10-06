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
public class PortScanDetector implements ThreatDetector {

    private static final int DISTINCT_PORT_THRESHOLD = 20;
    private static final Duration DETECTION_WINDOW = Duration.ofSeconds(30);

    private final SecurityEventRepository eventRepository;
    private final ThreatRepository threatRepository;

    public PortScanDetector(SecurityEventRepository eventRepository, ThreatRepository threatRepository) {
        this.eventRepository = eventRepository;
        this.threatRepository = threatRepository;
    }

    @Override
    public RuleCode ruleCode() {
        return RuleCode.PORT_SCAN;
    }

    @Override
    public boolean detect(SecurityEvent event) {
        if (event.getEventType() != EventType.PORT_SCAN) {
            return false;
        }

        Integer currentPort = extractDestinationPort(event);
        if (currentPort == null) {
            return false;
        }

        var windowStart = event.getTimestamp().minus(DETECTION_WINDOW);

        // Từ Phase 8: event hiện tại đã commit thật (xem ghi chú tương tự trong BruteForceDetector)
        // nên không còn cộng +1 thủ công như Phase 4.
        long distinctPorts = eventRepository.countDistinctDestinationPorts(
                event.getSourceIp(), windowStart, event.getTimestamp());

        if (distinctPorts < DISTINCT_PORT_THRESHOLD) {
            return false;
        }

        boolean alreadyDetected = threatRepository.existsByThreatTypeAndSourceIpAndDetectedAtBetween(
                ThreatType.PORT_SCAN, event.getSourceIp(), windowStart, event.getTimestamp());

        return !alreadyDetected;
    }

    private Integer extractDestinationPort(SecurityEvent event) {
        if (event.getPayload() == null) {
            return null;
        }
        Object value = event.getPayload().get("destinationPort");
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }
}