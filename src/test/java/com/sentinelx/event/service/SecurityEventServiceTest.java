package com.sentinelx.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelx.common.exception.BadRequestException;
import com.sentinelx.common.model.Severity;
import com.sentinelx.event.dto.CreateSecurityEventRequest;
import com.sentinelx.event.dto.EventFilter;
import com.sentinelx.event.entity.EventType;
import com.sentinelx.event.entity.SecurityEvent;
import com.sentinelx.event.repository.SecurityEventRepository;
import com.sentinelx.messaging.EventIngestedEvent;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class SecurityEventServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-22T10:00:00Z");

    @Mock
    private SecurityEventRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SecurityEventService service;

    @BeforeEach
    void setUp() {
        service = new SecurityEventService(repository, new ObjectMapper(),
                Clock.fixed(NOW, ZoneOffset.UTC),
                new EventIngestionProperties(Duration.ofMinutes(5), 100),
                eventPublisher);
    }

    private CreateSecurityEventRequest request(Instant timestamp, String sourceIp, String username,
                                               Map<String, Object> payload) {
        return new CreateSecurityEventRequest(timestamp, EventType.LOGIN_FAILED, "AUTH_SERVICE",
                sourceIp, null, username, Severity.MEDIUM, payload);
    }

    @Test
    void ingest_normalizesAndSavesEvent() {
        when(repository.save(any(SecurityEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        service.ingest(request(NOW, "::1", "  Admin ", null));

        ArgumentCaptor<SecurityEvent> captor = ArgumentCaptor.forClass(SecurityEvent.class);
        verify(repository).save(captor.capture());
        assertEquals("0:0:0:0:0:0:0:1", captor.getValue().getSourceIp());
        assertEquals("admin", captor.getValue().getUsername());
        assertEquals(NOW, captor.getValue().getTimestamp());
    }

    @Test
    void ingest_publishesEventIngestedEvent_afterSaving() {
        when(repository.save(any(SecurityEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        service.ingest(request(NOW, "10.0.0.1", null, null));

        ArgumentCaptor<EventIngestedEvent> captor = ArgumentCaptor.forClass(EventIngestedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(EventIngestedEvent.class, captor.getValue().getClass());
    }

    @Test
    void ingest_allowsSmallClockSkew() {
        when(repository.save(any(SecurityEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        service.ingest(request(NOW.plusSeconds(60), "10.0.0.1", null, null));

        verify(repository).save(any(SecurityEvent.class));
    }

    @Test
    void ingest_rejectsTimestampTooFarInFuture() {
        assertThrows(BadRequestException.class,
                () -> service.ingest(request(NOW.plus(Duration.ofMinutes(10)), "10.0.0.1", null, null)));

        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void ingest_rejectsOversizedPayload() {
        Map<String, Object> huge = Map.of("data", "x".repeat(500));

        assertThrows(BadRequestException.class,
                () -> service.ingest(request(NOW, "10.0.0.1", null, huge)));

        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void list_rejectsInvertedTimeRange() {
        EventFilter filter = new EventFilter(null, null, null, null, NOW, NOW.minusSeconds(1));

        assertThrows(BadRequestException.class, () -> service.list(filter, 0, 20));
    }
}