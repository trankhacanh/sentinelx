package com.sentinelx.detection.rule;

import com.sentinelx.detection.entity.RuleCode;
import com.sentinelx.event.entity.EventType;
import com.sentinelx.event.entity.SecurityEvent;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * CHỈ nhận diện (detection), không khai thác (exploitation) — đúng yêu cầu đặc tả mục 10 Rule 4.
 * Danh sách pattern dưới đây là các dấu hiệu cú pháp SQL injection phổ biến, công khai rộng rãi
 * trong tài liệu bảo mật (tương tự lớp chữ ký cơ bản của một WAF như ModSecurity CRS), hữu ích để
 * PHÁT HIỆN chuỗi khả nghi trong dữ liệu ĐẾN, không hữu ích để tự thực hiện một cuộc tấn công.
 */
@Component
public class SqlInjectionDetector implements ThreatDetector {

    private static final List<Pattern> SUSPICIOUS_PATTERNS = List.of(
            // Tautology kinh điển: ' OR '1'='1, " OR "1"="1, OR 1=1
            Pattern.compile("(?i)'\\s*OR\\s*'?\\d*'?\\s*=\\s*'?\\d*'?"),
            Pattern.compile("(?i)\\bOR\\b\\s+1\\s*=\\s*1\\b"),
            // UNION-based injection
            Pattern.compile("(?i)\\bUNION\\b\\s+(ALL\\s+)?\\bSELECT\\b"),
            // Stacked query / statement termination kèm lệnh phá hoại
            Pattern.compile("(?i);\\s*(DROP|DELETE|TRUNCATE|UPDATE|INSERT)\\b"),
            Pattern.compile("(?i)\\bDROP\\s+TABLE\\b"),
            // SQL comment dùng để cắt bỏ phần còn lại của câu lệnh gốc
            Pattern.compile("--\\s"),
            Pattern.compile("/\\*.*\\*/"),
            // Time-based blind injection phổ biến
            Pattern.compile("(?i)\\bSLEEP\\s*\\("),
            Pattern.compile("(?i)\\bWAITFOR\\s+DELAY\\b"));

    @Override
    public RuleCode ruleCode() {
        return RuleCode.SQL_INJECTION_PATTERN;
    }

    @Override
    public boolean detect(SecurityEvent event) {
        if (event.getEventType() != EventType.HTTP_REQUEST) {
            return false;
        }
        String combined = extractInspectableText(event);
        if (combined.isEmpty()) {
            return false;
        }
        return SUSPICIOUS_PATTERNS.stream().anyMatch(p -> p.matcher(combined).find());
    }

    private String extractInspectableText(SecurityEvent event) {
        if (event.getPayload() == null) {
            return "";
        }
        Object path = event.getPayload().get("requestPath");
        Object query = event.getPayload().get("queryString");
        StringBuilder sb = new StringBuilder();
        if (path instanceof String s) {
            sb.append(s).append(' ');
        }
        if (query instanceof String s) {
            sb.append(s);
        }
        return sb.toString();
    }
}