package com.sentinelx.incident.entity;

import com.sentinelx.alert.entity.Alert;
import com.sentinelx.common.model.Severity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    // LAZY (mặc định của @ManyToMany): chấp nhận N+1 nhỏ khi list nhiều incident cùng lúc,
    // ở quy mô portfolio project chưa cần tối ưu (xem đặc tả mục 39: "Measure before optimizing").
    @ManyToMany
    @JoinTable(name = "incident_alerts",
            joinColumns = @JoinColumn(name = "incident_id"),
            inverseJoinColumns = @JoinColumn(name = "alert_id"))
    private Set<Alert> alerts = new HashSet<>();

    protected Incident() {
        // JPA
    }

    public Incident(String title, String description, Severity severity) {
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = IncidentStatus.OPEN;
    }

    public void updateDetails(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public void updateStatus(IncidentStatus status) {
        this.status = status;
        boolean isTerminal = status == IncidentStatus.RESOLVED || status == IncidentStatus.CLOSED;
        if (isTerminal && this.resolvedAt == null) {
            this.resolvedAt = Instant.now();
        }
    }

    public void assignTo(UUID userId) {
        this.assignedTo = userId;
    }

    public void linkAlert(Alert alert) {
        this.alerts.add(alert);
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Severity getSeverity() { return severity; }
    public IncidentStatus getStatus() { return status; }
    public UUID getAssignedTo() { return assignedTo; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public Set<Alert> getAlerts() { return alerts; }
}