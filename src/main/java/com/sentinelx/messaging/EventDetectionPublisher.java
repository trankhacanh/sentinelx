package com.sentinelx.messaging;

import com.sentinelx.messaging.dto.EventDetectionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EventDetectionPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventDetectionPublisher.class);

    private final AmqpTemplate amqpTemplate;
    private final MessagingProperties properties;

    public EventDetectionPublisher(AmqpTemplate amqpTemplate, MessagingProperties properties) {
        this.amqpTemplate = amqpTemplate;
        this.properties = properties;
    }

    /**
     * AFTER_COMMIT: chỉ chạy nếu transaction ghi SecurityEvent đã COMMIT THÀNH CÔNG. Nếu
     * transaction rollback (lỗi bất kỳ sau khi event được lưu), listener này KHÔNG BAO GIỜ được
     * gọi -> không bao giờ gửi message cho một event không tồn tại trong DB.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEventIngested(EventIngestedEvent event) {
        try {
            amqpTemplate.convertAndSend(properties.exchange(), properties.routingKey(),
                    new EventDetectionMessage(event.eventId()));
        } catch (Exception ex) {
            // Lỗi publish không được ném ngược lên HTTP response -- transaction ĐÃ commit, event
            // ĐÃ an toàn trong DB. Mất một lượt detection do lỗi hạ tầng hiếm gặp là đánh đổi
            // chấp nhận được ở MVP; Phase 13 (Observability) sẽ thêm metric theo dõi việc này.
            log.error("Failed to publish detection message for event {}: {}", event.eventId(), ex.getMessage(), ex);
        }
    }
}