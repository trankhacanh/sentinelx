package com.sentinelx.event.repository;

import com.sentinelx.event.entity.EventType;
import com.sentinelx.event.entity.SecurityEvent;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SecurityEventRepository
        extends JpaRepository<SecurityEvent, UUID>, JpaSpecificationExecutor<SecurityEvent> {

    /**
     * Đếm event cùng loại, cùng IP nguồn, trong khoảng thời gian [from, to] (bao gồm hai đầu).
     * Dùng index idx_security_events_source_ip_time, không quét toàn bảng.
     */
    long countByEventTypeAndSourceIpAndTimestampBetween(
            EventType eventType, String sourceIp, Instant from, Instant to);
}