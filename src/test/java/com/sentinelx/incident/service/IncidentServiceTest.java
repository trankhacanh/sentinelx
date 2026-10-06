package com.sentinelx.incident.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sentinelx.alert.entity.Alert;
import com.sentinelx.alert.repository.AlertRepository;
import com.sentinelx.common.exception.ResourceNotFoundException;
import com.sentinelx.common.model.Severity;
import com.sentinelx.detection.entity.DetectionRule;
import com.sentinelx.detection.entity.Threat;
import com.sentinelx.detection.entity.ThreatType;
import com.sentinelx.incident.dto.CreateIncidentRequest;
import com.sentinelx.incident.entity.Incident;
import com.sentinelx.incident.entity.IncidentStatus;
import com.sentinelx.incident.repository.IncidentNoteRepository;
import com.sentinelx.incident.repository.IncidentRepository;
import com.sentinelx.user.service.AssignmentValidator;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentNoteRepository noteRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AssignmentValidator assignmentValidator;

    private IncidentService service;

    @BeforeEach
    void setUp() {
        service = new IncidentService(incidentRepository, noteRepository, alertRepository, assignmentValidator);
    }

    private Incident incidentWithId(Incident incident, UUID id) throws Exception {
        Field f = Incident.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(incident, id);
        return incident;
    }

    private Alert alert(UUID id) throws Exception {
        var threatConstructor = Threat.class.getDeclaredConstructor(
                UUID.class, DetectionRule.class, ThreatType.class, Severity.class, int.class, String.class,
                String.class);
        threatConstructor.setAccessible(true);
        Threat t = threatConstructor.newInstance(UUID.randomUUID(), null, ThreatType.BRUTE_FORCE, Severity.HIGH,
                75, "10.0.0.1", "desc");
        Alert a = new Alert(t, "title");
        Field f = Alert.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(a, id);
        return a;
    }

    @Test
    void create_withoutAlerts_startsAsOpen() {
        when(incidentRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = service.create(new CreateIncidentRequest("Title", "Desc", Severity.HIGH, null));

        assertEquals(IncidentStatus.OPEN, response.status());
        assertTrue(response.alerts().isEmpty());
    }

           @Test
    void create_withAlertIds_linksThem() throws Exception {
        UUID alertId = UUID.randomUUID();
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert(alertId)));
        when(incidentRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = service.create(
                new CreateIncidentRequest("Title", "Desc", Severity.HIGH, Set.of(alertId)));

        assertEquals(1, response.alerts().size());
    }

    @Test
    void create_withUnknownAlertId_throwsNotFound() {
        UUID alertId = UUID.randomUUID();
        when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(
                new CreateIncidentRequest("Title", "Desc", Severity.HIGH, Set.of(alertId))));
    }

    @Test
    void updateStatus_toResolved_setsResolvedAt() throws Exception {
        Incident incident = incidentWithId(new Incident("Title", "Desc", Severity.MEDIUM), UUID.randomUUID());
        when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
        when(noteRepository.findByIncident_IdOrderByCreatedAtAsc(incident.getId())).thenReturn(List.of());

        var response = service.updateStatus(incident.getId(), IncidentStatus.RESOLVED);

        assertEquals(IncidentStatus.RESOLVED, response.status());
        assertNotNull(response.resolvedAt());
    }

    @Test
    void updateStatus_toInvestigating_doesNotSetResolvedAt() throws Exception {
        Incident incident = incidentWithId(new Incident("Title", "Desc", Severity.MEDIUM), UUID.randomUUID());
        when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
        when(noteRepository.findByIncident_IdOrderByCreatedAtAsc(incident.getId())).thenReturn(List.of());

        var response = service.updateStatus(incident.getId(), IncidentStatus.INVESTIGATING);

        assertNull(response.resolvedAt());
    }

    @Test
    void assign_delegatesToAssignmentValidator() throws Exception {
        Incident incident = incidentWithId(new Incident("Title", "Desc", Severity.MEDIUM), UUID.randomUUID());
        UUID analystId = UUID.randomUUID();
        when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
        when(noteRepository.findByIncident_IdOrderByCreatedAtAsc(incident.getId())).thenReturn(List.of());

        var response = service.assign(incident.getId(), analystId);

        verify(assignmentValidator).validateAssignable(analystId);
        assertEquals(analystId, response.assignedTo());
    }

    @Test
    void addNote_savesNoteLinkedToIncident() throws Exception {
        Incident incident = incidentWithId(new Incident("Title", "Desc", Severity.MEDIUM), UUID.randomUUID());
        UUID authorId = UUID.randomUUID();
        when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
        when(noteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = service.addNote(incident.getId(), authorId, "Investigating now");

        assertEquals(authorId, response.authorId());
        assertEquals("Investigating now", response.content());
    }

    @Test
    void getById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(incidentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(id));
    }
}