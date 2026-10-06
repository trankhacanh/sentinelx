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
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PortScanDetectorTest {

    private static final Instant NOW = Instant.parse("2026-10-04T10:00:00Z");
    private static final String IP = "10.0.0.7";

    @Mock
    private SecurityEventRepository eventRepository;

    @Mock
    private ThreatRepository threatRepository;

    private PortScanDetector detector;

    @BeforeEach
    void setUp() {
        detector = new PortScanDetector(eventRepository, threatRepository);
    }

    private SecurityEvent event(EventType type, Integer port) {
        Map<String, Object> payload = port == null ? null : Map.of("destinationPort", port);
        return new SecurityEvent(NOW, type, "FIREWALL", IP, null, null, Severity.HIGH, payload);
    }

    @Test
    void ignoresNonPortScanEvents() {
        assertFalse(detector.detect(event(EventType.HTTP_REQUEST, 22)));
    }

    @Test
    void ignoresEventsMissingDestinationPort() {
        assertFalse(detector.detect(event(EventType.PORT_SCAN, null)));
    }

    @Test
    void returnsFalse_whenBelowThreshold() {
        when(eventRepository.countDistinctDestinationPorts(eq(IP), any(), any())).thenReturn(18L);

        assertFalse(detector.detect(event(EventType.PORT_SCAN, 443)));
    }

    @Test
    void returnsTrue_whenThresholdReachedAndNoExistingThreat() {
        // Phase 8: không còn +1, mock trả thẳng đúng ngưỡng.
        when(eventRepository.countDistinctDestinationPorts(eq(IP), any(), any())).thenReturn(20L);
        when(threatRepository.existsByThreatTypeAndSourceIpAndDetectedAtBetween(
                eq(ThreatType.PORT_SCAN), eq(IP), any(), any())).thenReturn(false);

        assertTrue(detector.detect(event(EventType.PORT_SCAN, 8080)));
    }

    @Test
    void returnsFalse_whenThreatAlreadyExistsInWindow() {
        when(eventRepository.countDistinctDestinationPorts(eq(IP), any(), any())).thenReturn(25L);
        when(threatRepository.existsByThreatTypeAndSourceIpAndDetectedAtBetween(
                eq(ThreatType.PORT_SCAN), eq(IP), any(), any())).thenReturn(true);

        assertFalse(detector.detect(event(EventType.PORT_SCAN, 8080)));
    }
}