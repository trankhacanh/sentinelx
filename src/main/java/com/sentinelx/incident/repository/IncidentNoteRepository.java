package com.sentinelx.incident.repository;

import com.sentinelx.incident.entity.IncidentNote;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentNoteRepository extends JpaRepository<IncidentNote, UUID> {

    List<IncidentNote> findByIncident_IdOrderByCreatedAtAsc(UUID incidentId);
}