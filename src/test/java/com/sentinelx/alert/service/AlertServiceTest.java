package com.sentinelx.alert.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.sentinelx.alert.entity.Alert;
import com.sentinelx.alert.entity.AlertStatus;
import com.sentinelx.alert.repository.AlertRepository;
import com.sentinelx.common.exception.BadRequestException;
import com.sentinelx.common.exception.ResourceNotFoundException;
import com.sentinelx.common.model.Severity;
import com.sentinelx.detection.entity.DetectionRule;
import com.sentinelx.detection.entity.Threat;
import com.sentinelx.detection.entity.ThreatType;
import com.sentinelx.user.entity.Role;
import com.sentinelx.user.entity.RoleName;
import com.sentinelx.user.entity.User;
import com.sentinelx.user.repository.UserRepository;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private UserRepository userRepository;

    private AlertService service;

    @BeforeEach
    void setUp() {
        service = new AlertService(alertRepository, userRepository);
    }

    private Threat threat(ThreatType type, String ip) throws Exception {
        var constructor = Threat.class.getDeclaredConstructor(
                UUID.class, DetectionRule.class, ThreatType.class, Severity.class, int.class, String.class,
                String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(UUID.randomUUID(), null, type, Severity.HIGH, 75, ip, "desc");
    }

    private Alert alertWithId(Alert alert, UUID id) throws Exception {
        Field f = Alert.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(alert, id);
        return alert;
    }

    private User userWithRole(RoleName roleName) throws Exception {
        var roleConstructor = Role.class.getDeclaredConstructor();
        roleConstructor.setAccessible(true);
        Role role = roleConstructor.newInstance();
        Field nameField = Role.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(role, roleName);

        return new User("someone", "someone@example.com", "hash", "Someone", Set.of(role));
    }

    @Test
    void createForThreat_buildsHumanReadableTitle() throws Exception {
        Threat t = threat(ThreatType.BRUTE_FORCE, "10.0.0.1");
        when(alertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Alert alert = service.createForThreat(t);

        assertEquals("Brute force login attempts from 10.0.0.1", alert.getTitle());
        assertEquals(AlertStatus.OPEN, alert.getStatus());
    }

    @Test
    void getById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(alertRepository.findById(id)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(id));
    }

    @Test
    void assign_toNull_unassigns() throws Exception {
        Alert alert = alertWithId(new Alert(threat(ThreatType.PORT_SCAN, "10.0.0.2"), "title"), UUID.randomUUID());
        when(alertRepository.findById(alert.getId())).thenReturn(java.util.Optional.of(alert));

        var response = service.assign(alert.getId(), null);

        assertNull(response.assignedTo());
    }

    @Test
    void assign_toViewerOnlyUser_throwsBadRequest() throws Exception {
        Alert alert = alertWithId(new Alert(threat(ThreatType.PORT_SCAN, "10.0.0.2"), "title"), UUID.randomUUID());
        UUID viewerId = UUID.randomUUID();
        when(alertRepository.findById(alert.getId())).thenReturn(java.util.Optional.of(alert));
        when(userRepository.findById(viewerId)).thenReturn(java.util.Optional.of(userWithRole(RoleName.VIEWER)));

        assertThrows(BadRequestException.class, () -> service.assign(alert.getId(), viewerId));
    }

    @Test
    void assign_toSocAnalyst_succeeds() throws Exception {
        Alert alert = alertWithId(new Alert(threat(ThreatType.PORT_SCAN, "10.0.0.2"), "title"), UUID.randomUUID());
        UUID analystId = UUID.randomUUID();
        when(alertRepository.findById(alert.getId())).thenReturn(java.util.Optional.of(alert));
        when(userRepository.findById(analystId))
                .thenReturn(java.util.Optional.of(userWithRole(RoleName.SOC_ANALYST)));

        var response = service.assign(alert.getId(), analystId);

        assertEquals(analystId, response.assignedTo());
    }

    @Test
    void updateStatus_changesStatus() throws Exception {
        Alert alert = alertWithId(new Alert(threat(ThreatType.SQL_INJECTION, "10.0.0.3"), "title"), UUID.randomUUID());
        when(alertRepository.findById(alert.getId())).thenReturn(java.util.Optional.of(alert));

        var response = service.updateStatus(alert.getId(), AlertStatus.RESOLVED);

        assertEquals(AlertStatus.RESOLVED, response.status());
    }
}