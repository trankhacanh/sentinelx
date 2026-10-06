package com.sentinelx.messaging;

import com.sentinelx.detection.engine.DetectionRuleEngine;
import com.sentinelx.event.repository.SecurityEventRepository;
import com.sentinelx.messaging.dto.EventDetectionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EventDetectionConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventDetectionConsumer.class);

    private final SecurityEventRepository eventRepository;
    private final DetectionRuleEngine detectionRuleEngine;

    public EventDetectionConsumer(SecurityEventRepository eventRepository,
                                  DetectionRuleEngine detectionRuleEngine) {
        this.eventRepository = eventRepository;
        this.detectionRuleEngine = detectionRuleEngine;
    }

    /**
     * Chạy trên thread riêng của RabbitMQ listener container, KHÔNG có transaction nào đang mở
     * sẵn. detectionRuleEngine.evaluate() tự mở transaction mới (propagation REQUIRED mặc định) —
     * vì event đã chắc chắn commit từ trước (EventDetectionPublisher chỉ gửi sau AFTER_COMMIT),
     * mọi query trong quá trình detect() đều thấy đúng dữ liệu, không còn vấn đề visibility đã
     * gặp ở Phase 4.
     */
    @RabbitListener(queues = "${sentinelx.messaging.queue}")
    public void onMessage(EventDetectionMessage message) {
        eventRepository.findById(message.eventId()).ifPresentOrElse(
                detectionRuleEngine::evaluate,
                () -> log.warn("Received detection message for unknown event {} - skipping. "
                        + "This should not normally happen since messages are only published "
                        + "after the event's transaction commits.", message.eventId())
        );
    }
}