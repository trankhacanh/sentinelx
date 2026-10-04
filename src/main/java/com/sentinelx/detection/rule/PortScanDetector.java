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

    // Tham số thuật toán theo đặc tả mục 10 (Rule 2).
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
            // Không đủ dữ liệu để đánh giá -> bỏ qua an toàn, không ném lỗi (xem quyết định 2c).
            return false;
        }

        var windowStart = event.getTimestamp().minus(DETECTION_WINDOW);

        // Event hiện tại CHƯA commit (cùng lý do đã gặp ở BruteForceDetector), nên DB chưa đếm
        // được nó. Ta đếm các event TRƯỚC đó rồi cộng thêm cổng hiện tại một cách tường minh.
        // Vì không biết các cổng trước đó có trùng currentPort hay không chỉ từ 1 con số COUNT,
        // ta chấp nhận ước lượng: nếu count DISTINCT trước đó đã >= ngưỡng, chắc chắn đạt ngưỡng;
        // nếu đúng bằng ngưỡng - 1, cộng thêm 1 cổng mới (currentPort nhiều khả năng chưa từng
        // xuất hiện trong một đợt quét tăng dần cổng) để quyết định có kích hoạt hay không.
        long priorDistinctPorts = eventRepository.countDistinctDestinationPorts(
                event.getSourceIp(), windowStart, event.getTimestamp());
        long totalDistinctPorts = priorDistinctPorts + 1;

        if (totalDistinctPorts < DISTINCT_PORT_THRESHOLD) {
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