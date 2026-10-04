package com.sentinelx.detection.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.sentinelx.common.model.Severity;
import com.sentinelx.detection.entity.DetectionRule;
import com.sentinelx.detection.entity.ThreatType;
import com.sentinelx.detection.repository.ThreatRepository;
import com.sentinelx.event.entity.EventType;
import com.sentinelx.event.entity.SecurityEvent;
import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RiskScoreServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-04T10:00:00Z");
    private static final UUID RULE_ID = UUID.randomUUID();
    private static final String IP = "203.0.113.50";

    @Mock
    private ThreatRepository threatRepository;

    @Mock
    private SensitiveAccounts sensitiveAccounts;

    private RiskScoreService service;

    @BeforeEach
    void setUp() {
        service = new RiskScoreService(threatRepository, sensitiveAccounts, Clock.fixed(NOW, ZoneOffset.UTC));
    }
    private DetectionRule rule(int baseRiskScore) throws Exception {
        var constructor = DetectionRule.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        DetectionRule r = constructor.newInstance();
        setField(r, "id", RULE_ID);
        setField(r, "baseRiskScore", baseRiskScore);
        return r;
    }
    private void setField(Object target, String name, Object value) throws Exception {
        Field f = DetectionRule.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private SecurityEvent event(String username) {
        return new SecurityEvent(NOW, EventType.LOGIN_FAILED, "AUTH_SERVICE", IP, null, username,
                com.sentinelx.common.model.Severity.MEDIUM, null);
    }

    @Test
    void noPriorThreats_noSensitiveUser_returnsBaseScoreOnly() throws Exception {
        when(threatRepository.countByRule_IdAndSourceIpAndDetectedAtAfter(eq(RULE_ID), eq(IP), any()))
                .thenReturn(0L);
        when(sensitiveAccounts.contains(any())).thenReturn(false);

        assertEquals(75, service.calculate(rule(75), event("bob")));
    }

    @Test
    void frequencyComponent_scalesWithPriorThreatCount() throws Exception {
        when(threatRepository.countByRule_IdAndSourceIpAndDetectedAtAfter(eq(RULE_ID), eq(IP), any()))
                .thenReturn(2L); // 2 * 5 = +10
        when(sensitiveAccounts.contains(any())).thenReturn(false);

        assertEquals(85, service.calculate(rule(75), event("bob")));
    }

    @Test
    void frequencyComponent_isCappedAt20() throws Exception {
        when(threatRepository.countByRule_IdAndSourceIpAndDetectedAtAfter(eq(RULE_ID), eq(IP), any()))
                .thenReturn(100L); // 100*5=500, phải bị cap ở 20
        when(sensitiveAccounts.contains(any())).thenReturn(false);

        assertEquals(95, service.calculate(rule(75), event("bob"))); // 75 + 20
    }

    @Test
    void targetSensitivity_addsFixedPoints() throws Exception {
        when(threatRepository.countByRule_IdAndSourceIpAndDetectedAtAfter(eq(RULE_ID), eq(IP), any()))
                .thenReturn(0L);
        when(sensitiveAccounts.contains(eq("admin"))).thenReturn(true);

        assertEquals(90, service.calculate(rule(75), event("admin"))); // 75 + 15
    }

    @Test
    void totalScore_isClampedAt100() throws Exception {
        when(threatRepository.countByRule_IdAndSourceIpAndDetectedAtAfter(eq(RULE_ID), eq(IP), any()))
                .thenReturn(100L);
        when(sensitiveAccounts.contains(any())).thenReturn(true);

        assertEquals(100, service.calculate(rule(90), event("admin"))); // 90+20+15=125 -> clamp 100
    }
}