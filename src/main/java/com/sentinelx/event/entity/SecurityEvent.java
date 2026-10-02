package com.sentinelx.event.entity;

import com.sentinelx.common.model.Severity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Bất biến: event là bằng chứng, chỉ được ghi một lần. Không có setter. */
@Entity
@Immutable
@Table(name = "security_events")
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_time", nullable = false, updatable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30, updatable = false)
    private EventType eventType;

    @Column(nullable = false, length = 100, updatable = false)
    private String source;

    @Column(name = "source_ip", nullable = false, length = 45, updatable = false)
    private String sourceIp;

    @Column(name = "destination_ip", length = 45, updatable = false)
    private String destinationIp;

    @Column(length = 100, updatable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private Severity severity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", updatable = false)
    private Map<String, Object> payload;

    /** Cột sinh tự động trong DB, chỉ đọc để phục vụ tìm kiếm. */
    @Column(name = "search_text", insertable = false, updatable = false)
    private String searchText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SecurityEvent() {
        // JPA
    }

    public SecurityEvent(Instant timestamp, EventType eventType, String source, String sourceIp,
                         String destinationIp, String username, Severity severity,
                         Map<String, Object> payload) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.source = source;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.username = username;
        this.severity = severity;
        this.payload = payload;
    }

    public UUID getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public EventType getEventType() { return eventType; }
    public String getSource() { return source; }
    public String getSourceIp() { return sourceIp; }
    public String getDestinationIp() { return destinationIp; }
    public String getUsername() { return username; }
    public Severity getSeverity() { return severity; }
    public Map<String, Object> getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
}