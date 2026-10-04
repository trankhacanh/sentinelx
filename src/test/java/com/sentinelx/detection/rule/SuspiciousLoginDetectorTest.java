package com.sentinelx.detection.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import com.sentinelx.detection.service.SensitiveAccounts;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SuspiciousLoginDetectorTest {

    private static final String IP = "198.51.100.5";
    private static final String USER = "alice";
    // 02:00 UTC -> nằm trong khung giờ bất thường [00:00, 05:00)
    private static final Instant UNUSUAL_TIME = Instant.parse("2026-10-04T02:00:00Z");
    // 14:00 UTC -> giờ bình thường
    private static final Instant NORMAL_TIME = Instant.parse("2026-10-04T14:00:00Z");

    @Mock
    private SecurityEventRepository eventRepository;
    @Mock
    private SensitiveAccounts sensitiveAccounts;

    @Mock
    private ThreatRepository threatRepository;

    private SuspiciousLoginDetector detector;

        @BeforeEach
    void setUp() {
        detector = new SuspiciousLoginDetector(eventRepository, threatRepository, sensitiveAccounts);
        lenient().when(sensitiveAccounts.contains(any())).thenAnswer(inv ->
                Set.of("admin", "root", "administrator").contains(inv.getArgument(0)));

        // Mặc định: IP quen thuộc, không có failed attempt, không có threat trùng lặp.
        // Mỗi test override lại đúng phần cần thiết.
        lenient().when(eventRepository.existsByEventTypeAndSourceIpAndUsernameAndTimestampBefore(
                any(), any(), any(), any())).thenReturn(true); // not new IP
        lenient().when(eventRepository.countByEventTypeAndUsernameAndTimestampBetween(
                any(), any(), any(), any())).thenReturn(0L);
        lenient().when(threatRepository.existsByThreatTypeAndSourceIpAndDetectedAtBetween(
                any(), any(), any(), any())).thenReturn(false);
    }

    private SecurityEvent event(EventType type, String username, Instant timestamp) {
        return new SecurityEvent(timestamp, type, "AUTH_SERVICE", IP, null, username, Severity.LOW, null);
    }

    @Test
    void ignoresNonLoginSuccessEvents() {
        assertFalse(detector.detect(event(EventType.LOGIN_FAILED, USER, NORMAL_TIME)));
    }

    @Test
    void onlyOneSignal_doesNotTrigger() {
        // Chỉ 1 tín hiệu: giờ bất thường. Không new IP, không failed attempt, không sensitive user.
        assertFalse(detector.detect(event(EventType.LOGIN_SUCCESS, USER, UNUSUAL_TIME)));
    }

    @Test
    void twoSignals_newIpAndUnusualHour_triggers() {
        when(eventRepository.existsByEventTypeAndSourceIpAndUsernameAndTimestampBefore(
                any(), any(), any(), any())).thenReturn(false); // new IP

        assertTrue(detector.detect(event(EventType.LOGIN_SUCCESS, USER, UNUSUAL_TIME)));
    }

    @Test
    void twoSignals_newIpAndRecentFailedAttempts_triggers() {
        when(eventRepository.existsByEventTypeAndSourceIpAndUsernameAndTimestampBefore(
                any(), any(), any(), any())).thenReturn(false); // new IP
        when(eventRepository.countByEventTypeAndUsernameAndTimestampBetween(
                any(), any(), any(), any())).thenReturn(3L); // đủ ngưỡng failed attempts

        assertTrue(detector.detect(event(EventType.LOGIN_SUCCESS, USER, NORMAL_TIME)));
    }

    @Test
    void sensitiveUsernamePlusUnusualHour_triggers() {
        assertTrue(detector.detect(event(EventType.LOGIN_SUCCESS, "admin", UNUSUAL_TIME)));
    }

    @Test
    void normalLogin_noSignals_doesNotTrigger() {
        assertFalse(detector.detect(event(EventType.LOGIN_SUCCESS, USER, NORMAL_TIME)));
    }

    @Test
    void doesNotDuplicate_whenThreatAlreadyExistsInWindow() {
        when(eventRepository.existsByEventTypeAndSourceIpAndUsernameAndTimestampBefore(
                any(), any(), any(), any())).thenReturn(false);
        when(threatRepository.existsByThreatTypeAndSourceIpAndDetectedAtBetween(
                eq(ThreatType.SUSPICIOUS_AUTHENTICATION), eq(IP), any(), any())).thenReturn(true);

        assertFalse(detector.detect(event(EventType.LOGIN_SUCCESS, USER, UNUSUAL_TIME)));
    }
}