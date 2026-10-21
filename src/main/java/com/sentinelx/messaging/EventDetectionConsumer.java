package com.sentinelx.messaging;

import com.sentinelx.detection.engine.DetectionRuleEngine;
import com.sentinelx.event.repository.SecurityEventRepository;
import com.sentinelx.messaging.dto.EventDetectionMessage;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EventDetectionConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventDetectionConsumer.class);

    private final SecurityEventRepository eventRepository;
    private final DetectionRuleEngine detectionRuleEngine;
    private final MeterRegistry meterRegistry;

    public EventDetectionConsumer(SecurityEventRepository eventRepository,
                                  DetectionRuleEngine detectionRuleEngine, MeterRegistry meterRegistry) {
        this.eventRepository = eventRepository;
        this.detectionRuleEngine = detectionRuleEngine;
        this.meterRegistry = meterRegistry;
    }

    @RabbitListener(queues = "${sentinelx.messaging.queue}")
    public void onMessage(EventDetectionMessage message) {
        eventRepository.findById(message.eventId()).ifPresentOrElse(
                event -> {
                    // Detection latency: thời gian từ lúc event được GHI vào DB (createdAt) tới
                    // lúc consumer THỰC SỰ bắt đầu xử lý nó -- phản ánh đúng "thời gian chờ trong
                    // hàng đợi + overhead AMQP" (xem quyết định kiến trúc 2b).
                    Duration latency = Duration.between(event.getCreatedAt(), Instant.now());
                    meterRegistry.timer("sentinelx.detection.latency").record(latency);

                    detectionRuleEngine.evaluate(event);
                },
                () -> log.warn("Received detection message for unknown event {} - skipping. "
                        + "This should not normally happen since messages are only published "
                        + "after the event's transaction commits.", message.eventId())
        );
    }
}