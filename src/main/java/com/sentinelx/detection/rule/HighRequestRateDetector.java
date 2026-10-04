package com.sentinelx.detection.rule;

import com.sentinelx.detection.entity.RuleCode;
import com.sentinelx.detection.entity.ThreatType;
import com.sentinelx.detection.repository.ThreatRepository;
import com.sentinelx.event.entity.EventType;
import com.sentinelx.event.entity.SecurityEvent;
import com.sentinelx.event.repository.SecurityEventRepository;
import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * Cấu trúc giống hệt BruteForceDetector (đếm theo eventType + sourceIp + cửa sổ thời gian), chỉ
 * khác eventType theo dõi, ngưỡng và độ dài cửa sổ. Tái sử dụng method repository đã kiểm chứng
 * ở Bước 4.2 thay vì viết query mới, vì phép đếm có cùng bản chất.
 */
@Component
public class HighRequestRateDetector implements ThreatDetector {

    // Tham số thuật toán theo đặc tả mục 10 (Rule 5).
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

        // Event hiện tại chưa commit (cùng lý do đã gặp ở BruteForceDetector) nên cộng thêm 1
        // một cách tường minh thay vì chỉ dựa vào kết quả COUNT từ DB.
        long priorRequestCount = eventRepository.countByEventTypeAndSourceIpAndTimestampBetween(
                EventType.HTTP_REQUEST, event.getSourceIp(), windowStart, event.getTimestamp());
        long totalRequestCount = priorRequestCount + 1;

        if (totalRequestCount < REQUEST_THRESHOLD) {
            return false;
        }

        boolean alreadyDetected = threatRepository.existsByThreatTypeAndSourceIpAndDetectedAtBetween(
                ThreatType.HIGH_REQUEST_RATE, event.getSourceIp(), windowStart, event.getTimestamp());

        return !alreadyDetected;
    }
}