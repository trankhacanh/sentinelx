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

    // Tham số thuật toán cố định theo đặc tả mục 10 (Rule 1). Ngưỡng risk/severity nằm trong DB (DetectionRule),
    // nhưng "bao nhiêu lần, trong bao lâu" là bản chất thuật toán, không phải cấu hình vận hành nên để ở code.
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

        // QUAN TRỌNG: evaluate() chạy trong transaction REQUIRES_NEW (connection khác), nên tại thời
        // điểm này, event hiện tại CHƯA được commit bởi transaction ingest() đang tạm dừng. Dưới
        // isolation level mặc định READ COMMITTED, query COUNT dưới đây sẽ KHÔNG bao giờ đếm được
        // chính event đang xử lý. Ta cộng thêm 1 một cách tường minh, vì event này chắc chắn thỏa
        // điều kiện (đã kiểm tra eventType ở trên) nên không cần hỏi lại DB.
        long priorFailedCount = eventRepository.countByEventTypeAndSourceIpAndTimestampBetween(
                EventType.LOGIN_FAILED, event.getSourceIp(), windowStart, event.getTimestamp());
        long totalFailedCount = priorFailedCount + 1;

        if (totalFailedCount < FAILED_LOGIN_THRESHOLD) {
            return false;
        }

        boolean alreadyDetected = threatRepository.existsByThreatTypeAndSourceIpAndDetectedAtBetween(
                ThreatType.BRUTE_FORCE, event.getSourceIp(), windowStart, event.getTimestamp());

        return !alreadyDetected;
    }
}