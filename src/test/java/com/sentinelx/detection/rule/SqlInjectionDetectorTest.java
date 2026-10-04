package com.sentinelx.detection.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sentinelx.common.model.Severity;
import com.sentinelx.event.entity.EventType;
import com.sentinelx.event.entity.SecurityEvent;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SqlInjectionDetectorTest {

    private static final Instant NOW = Instant.parse("2026-10-04T10:00:00Z");

    private final SqlInjectionDetector detector = new SqlInjectionDetector();

    private SecurityEvent httpEvent(String path, String query) {
        return new SecurityEvent(NOW, EventType.HTTP_REQUEST, "WEB_SERVER", "10.0.0.1", null, null,
                Severity.MEDIUM, Map.of("requestPath", path, "queryString", query));
    }

    @Test
    void ignoresNonHttpRequestEvents() {
        var event = new SecurityEvent(NOW, EventType.LOGIN_FAILED, "AUTH_SERVICE", "10.0.0.1", null,
                "admin", Severity.MEDIUM, null);
        assertFalse(detector.detect(event));
    }

    @Test
    void cleanRequest_doesNotTrigger() {
        assertFalse(detector.detect(httpEvent("/api/products", "id=42&sort=price")));
    }

    @Test
    void missingPayload_doesNotTrigger() {
        var event = new SecurityEvent(NOW, EventType.HTTP_REQUEST, "WEB_SERVER", "10.0.0.1", null, null,
                Severity.MEDIUM, null);
        assertFalse(detector.detect(event));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "id=1' OR '1'='1",
            "id=1 OR 1=1",
            "id=1 UNION SELECT username,password FROM users",
            "id=1; DROP TABLE users",
            "id=1-- comment cut off",
            "id=1/* block comment */",
            "id=1 AND SLEEP(5)",
            "id=1; WAITFOR DELAY '0:0:5'"
    })
    void knownInjectionPatterns_areDetected(String maliciousQuery) {
        assertTrue(detector.detect(httpEvent("/api/products", maliciousQuery)));
    }

    @Test
    void patternInPathInsteadOfQuery_isAlsoDetected() {
        assertTrue(detector.detect(httpEvent("/api/products' OR '1'='1", "")));
    }

    @Test
    void caseInsensitive_isDetected() {
        assertTrue(detector.detect(httpEvent("/api/products", "id=1 uNioN SeLeCt 1,2,3")));
    }
}