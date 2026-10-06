package com.sentinelx.messaging;

import java.util.UUID;

/**
 * Sự kiện NỘI BỘ của Spring (không phải message RabbitMQ), do SecurityEventService phát ra ngay
 * sau khi lưu event, TRONG transaction đang mở. EventDetectionPublisher lắng nghe ở pha
 * AFTER_COMMIT để đảm bảo chỉ gửi message lên RabbitMQ khi transaction đã commit thật.
 */
public record EventIngestedEvent(UUID eventId) {
}