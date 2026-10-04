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
class BruteForceDetectorTest {

    private static final Instant NOW = Instant.parse("2026-10-02T10:00:00Z");
    private static final String IP = "192.168.1.50";

    @Mock
    private SecurityEventRepository eventRepository;

    @Mock
    private ThreatRepository threatRepository;

    private BruteForceDetector detector;

    @BeforeEach
    void setUp() {
        detector = new BruteForceDetector(eventRepository, threatRepository);
    }

    private SecurityEvent event(EventType type, String ip) {
        return new SecurityEvent(NOW, type, "AUTH_SERVICE", ip, null, "admin", Severity.MEDIUM, null);
    }

    @Test
    void ignoresNonLoginFailedEvents() {
        assertFalse(detector.detect(event(EventType.LOGIN_SUCCESS, IP)));
    }

        @Test
    void returnsFalse_whenBelowThreshold() {
        // 3 event trước đó đã commit + 1 (event hiện tại, chưa commit) = 4 < ngưỡng 5
        when(eventRepository.countByEventTypeAndSourceIpAndTimestampBetween(
                eq(EventType.LOGIN_FAILED), eq(IP), any(), any())).thenReturn(3L);

        assertFalse(detector.detect(event(EventType.LOGIN_FAILED, IP)));
    }

    @Test
    void returnsTrue_whenThresholdReachedAndNoExistingThreat() {
        // 4 event trước đó đã commit + 1 (event hiện tại) = 5 = đúng ngưỡng
        when(eventRepository.countByEventTypeAndSourceIpAndTimestampBetween(
                eq(EventType.LOGIN_FAILED), eq(IP), any(), any())).thenReturn(4L);
        when(threatRepository.existsByThreatTypeAndSourceIpAndDetectedAtBetween(
                eq(ThreatType.BRUTE_FORCE), eq(IP), any(), any())).thenReturn(false);

        assertTrue(detector.detect(event(EventType.LOGIN_FAILED, IP)));
    }

    @Test
    void returnsFalse_whenThreatAlreadyExistsInWindow() {
        when(eventRepository.countByEventTypeAndSourceIpAndTimestampBetween(
                eq(EventType.LOGIN_FAILED), eq(IP), any(), any())).thenReturn(8L);
        when(threatRepository.existsByThreatTypeAndSourceIpAndDetectedAtBetween(
                eq(ThreatType.BRUTE_FORCE), eq(IP), any(), any())).thenReturn(true);

        assertFalse(detector.detect(event(EventType.LOGIN_FAILED, IP)));
    }

    @Test
    void differentSourceIp_doesNotAffectCount() {
        when(eventRepository.countByEventTypeAndSourceIpAndTimestampBetween(
                eq(EventType.LOGIN_FAILED), eq("10.0.0.1"), any(), any())).thenReturn(5L);
        when(threatRepository.existsByThreatTypeAndSourceIpAndDetectedAtBetween(
                eq(ThreatType.BRUTE_FORCE), eq("10.0.0.1"), any(), any())).thenReturn(false);

        assertTrue(detector.detect(event(EventType.LOGIN_FAILED, "10.0.0.1")));
        // IP khác chưa được stub -> Mockito trả mặc định (0L / false) -> phải là false
        assertFalse(detector.detect(event(EventType.LOGIN_FAILED, IP)));
    }
}