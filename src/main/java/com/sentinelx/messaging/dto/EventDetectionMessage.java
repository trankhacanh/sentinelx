package com.sentinelx.messaging.dto;

import java.util.UUID;

/** Message thực sự đi qua RabbitMQ. Chỉ chứa id; consumer luôn đọc lại dữ liệu mới nhất từ DB. */
public record EventDetectionMessage(UUID eventId) {
}