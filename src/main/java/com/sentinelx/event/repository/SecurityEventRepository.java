package com.sentinelx.event.repository;

import com.sentinelx.event.entity.EventType;
import com.sentinelx.event.entity.SecurityEvent;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;

public interface SecurityEventRepository
        extends JpaRepository<SecurityEvent, UUID>, JpaSpecificationExecutor<SecurityEvent> {

    /**
     * Đếm event cùng loại, cùng IP nguồn, trong khoảng thời gian [from, to] (bao gồm hai đầu).
     * Dùng index idx_security_events_source_ip_time, không quét toàn bảng.
     */
    long countByEventTypeAndSourceIpAndTimestampBetween(
            EventType eventType, String sourceIp, Instant from, Instant to);

    /**
     * Đếm số cổng đích KHÁC NHAU (distinct) mà một IP đã chạm tới trong khoảng thời gian, đọc
     * trực tiếp từ payload JSONB. Native query vì JPQL không có cú pháp chuẩn để truy vấn JSONB.
     * Event có payload null hoặc thiếu destinationPort tự động bị loại khỏi phép đếm.
     */
    @org.springframework.data.jpa.repository.Query(value = """
            SELECT COUNT(DISTINCT (payload ->> 'destinationPort'))
            FROM security_events
            WHERE event_type = 'PORT_SCAN'
              AND source_ip = :sourceIp
              AND event_time BETWEEN :from AND :to
              AND payload ->> 'destinationPort' IS NOT NULL
            """, nativeQuery = true)
    long countDistinctDestinationPorts(@Param("sourceIp") String sourceIp,
                                       @Param("from") Instant from, @Param("to") Instant to);


      /** "New IP" cho SuspiciousLoginDetector: IP này đã từng LOGIN_SUCCESS cho username này chưa, TRƯỚC thời điểm given. */
    boolean existsByEventTypeAndSourceIpAndUsernameAndTimestampBefore(
            EventType eventType, String sourceIp, String username, Instant before);

    /** "Multiple failed attempts" cho SuspiciousLoginDetector: đếm LOGIN_FAILED của username (không phân biệt IP) trong cửa sổ. */
    long countByEventTypeAndUsernameAndTimestampBetween(
            EventType eventType, String username, Instant from, Instant to);                                  
}