package com.sentinelx.detection.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.sentinelx.common.model.Severity;
import com.sentinelx.detection.entity.ThreatType;
import com.sentinelx.detection.repository.ThreatRepository;
import com.sentinelx.event.entity.EventType;
import com.sentinelx.event.entity.SecurityEvent;
import com.sentinelx.event.repository.SecurityEventRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HighRequestRateDetectorTest {

    private static final Instant NOW = Instant.parse("2026-10-04T10:00:00Z");
    private static final String IP = "203.0.113.200";

    @Mock
    private SecurityEventRepository eventRepository;

    @Mock
    private ThreatRepository threatRepository;

    private HighRequestRateDetector detector;

    @BeforeEach
    void setUp() {
        detector = new HighRequestRateDetector(eventRepository, threatRepository);
    }

    private SecurityEvent event(EventType type, String ip) {
        return new SecurityEvent(NOW, type, "WEB_SERVER", ip, null, null, Severity.LOW, null);
    }

    @Test
    void ignoresNonHttpRequestEvents() {
        assertFalse(detector.detect(event(EventType.LOGIN_FAILED, IP)));
    }

    @Test
    void returnsFalse_whenBelowThreshold() {
        when(eventRepository.countByEventTypeAndSourceIpAndTimestampBetween(
                eq(EventType.HTTP_REQUEST), eq(IP), any(), any())).thenReturn(98L);

        assertFalse(detector.detect(event(EventType.HTTP_REQUEST, IP)));
    }

    @Test
    void returnsTrue_whenThresholdReachedAndNoExistingThreat() {
        // Phase 8: không còn +1, mock trả thẳng đúng ngưỡng.
        when(eventRepository.countByEventTypeAndSourceIpAndTimestampBetween(
                eq(EventType.HTTP_REQUEST), eq(IP), any(), any())).thenReturn(100L);
        when(threatRepository.existsByThreatTypeAndSourceIpAndDetectedAtBetween(
                eq(ThreatType.HIGH_REQUEST_RATE), eq(IP), any(), any())).thenReturn(false);

        assertTrue(detector.detect(event(EventType.HTTP_REQUEST, IP)));
    }

    @Test
    void returnsFalse_whenThreatAlreadyExistsInWindow() {
        when(eventRepository.countByEventTypeAndSourceIpAndTimestampBetween(
                eq(EventType.HTTP_REQUEST), eq(IP), any(), any())).thenReturn(150L);
        when(threatRepository.existsByThreatTypeAndSourceIpAndDetectedAtBetween(
                eq(ThreatType.HIGH_REQUEST_RATE), eq(IP), any(), any())).thenReturn(true);

        assertFalse(detector.detect(event(EventType.HTTP_REQUEST, IP)));
    }
}