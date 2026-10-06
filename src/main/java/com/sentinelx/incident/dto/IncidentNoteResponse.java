package com.sentinelx.incident.dto;

import com.sentinelx.incident.entity.IncidentNote;
import java.time.Instant;
import java.util.UUID;

public record IncidentNoteResponse(UUID id, UUID authorId, String content, Instant createdAt) {

    public static IncidentNoteResponse from(IncidentNote note) {
        return new IncidentNoteResponse(note.getId(), note.getAuthorId(), note.getContent(), note.getCreatedAt());
    }
}