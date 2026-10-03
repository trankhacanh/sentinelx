package com.sentinelx.detection.entity;

import com.sentinelx.common.model.Severity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

/** Bất biến như SecurityEvent: threat là kết quả phát hiện, không bị sửa sau khi tạo. */
@Entity
@Immutable
@Table(name = "threats")
public class Threat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @ManyToOne
    @JoinColumn(name = "rule_id", nullable = false, updatable = false)
    private DetectionRule rule;

    @Enumerated(EnumType.STRING)
    @Column(name = "threat_type", nullable = false, length = 40, updatable = false)
    private ThreatType threatType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private Severity severity;

    @Column(name = "risk_score", nullable = false, updatable = false)
    private int riskScore;

    @Column(name = "source_ip", nullable = false, length = 45, updatable = false)
    private String sourceIp;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String description;

    @CreationTimestamp
    @Column(name = "detected_at", nullable = false, updatable = false)
    private Instant detectedAt;

    protected Threat() {
        // JPA
    }

    public Threat(UUID eventId, DetectionRule rule, ThreatType threatType, Severity severity,
                 int riskScore, String sourceIp, String description) {
        this.eventId = eventId;
        this.rule = rule;
        this.threatType = threatType;
        this.severity = severity;
        this.riskScore = riskScore;
        this.sourceIp = sourceIp;
        this.description = description;
    }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public DetectionRule getRule() { return rule; }
    public ThreatType getThreatType() { return threatType; }
    public Severity getSeverity() { return severity; }
    public int getRiskScore() { return riskScore; }
    public String getSourceIp() { return sourceIp; }
    public String getDescription() { return description; }
    public Instant getDetectedAt() { return detectedAt; }
}