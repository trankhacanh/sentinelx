package com.sentinelx.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sentinelx.common.model.Severity;
import com.sentinelx.detection.engine.DetectionRuleEngine;
import com.sentinelx.event.entity.EventType;
import com.sentinelx.event.entity.SecurityEvent;
import com.sentinelx.event.repository.SecurityEventRepository;
import com.sentinelx.messaging.dto.EventDetectionMessage;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventDetectionConsumerTest {

    @Mock
    private SecurityEventRepository eventRepository;

    @Mock
    private DetectionRuleEngine detectionRuleEngine;

    private EventDetectionConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new EventDetectionConsumer(eventRepository, detectionRuleEngine);
    }

    @Test
    void onMessage_eventExists_callsDetectionEngine() {
        UUID eventId = UUID.randomUUID();
        SecurityEvent event = new SecurityEvent(Instant.parse("2026-10-06T00:00:00Z"), EventType.LOGIN_FAILED,
                "AUTH_SERVICE", "10.0.0.1", null, "admin", Severity.MEDIUM, null);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        consumer.onMessage(new EventDetectionMessage(eventId));

        verify(detectionRuleEngine).evaluate(event);
    }

    @Test
    void onMessage_eventMissing_skipsWithoutError() {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        consumer.onMessage(new EventDetectionMessage(eventId));

        verify(detectionRuleEngine, never()).evaluate(any());
    }
}