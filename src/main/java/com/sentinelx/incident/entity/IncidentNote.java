package com.sentinelx.incident.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** Bất biến: ghi chú điều tra là bằng chứng theo thời gian, không có API sửa/xóa. */
@Entity
@Immutable
@Table(name = "incident_notes")
public class IncidentNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "incident_id", nullable = false, updatable = false)
    private Incident incident;

    @Column(name = "author_id", nullable = false, updatable = false)
    private UUID authorId;

    @Column(columnDefinition = "TEXT", nullable = false, updatable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected IncidentNote() {
        // JPA
    }

    public IncidentNote(Incident incident, UUID authorId, String content) {
        this.incident = incident;
        this.authorId = authorId;
        this.content = content;
    }

    public UUID getId() { return id; }
    public UUID getAuthorId() { return authorId; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}