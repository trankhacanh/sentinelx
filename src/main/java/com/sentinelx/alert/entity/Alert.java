package com.sentinelx.alert.entity;

import com.sentinelx.detection.entity.Threat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Quan hệ 1-1 với Threat: mỗi Threat sinh đúng 1 Alert (ràng buộc UNIQUE ở DB, xem V9).
    @OneToOne
    @JoinColumn(name = "threat_id", nullable = false, unique = true, updatable = false)
    private Threat threat;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Alert() {
        // JPA
    }

    public Alert(Threat threat, String title) {
        this.threat = threat;
        this.title = title;
        this.status = AlertStatus.OPEN;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateStatus(AlertStatus status) {
        this.status = status;
    }

    /** null nghĩa là hủy gán (unassign). */
    public void assignTo(UUID userId) {
        this.assignedTo = userId;
    }

    public UUID getId() { return id; }
    public Threat getThreat() { return threat; }
    public String getTitle() { return title; }
    public AlertStatus getStatus() { return status; }
    public UUID getAssignedTo() { return assignedTo; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}