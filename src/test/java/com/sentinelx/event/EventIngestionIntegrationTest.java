package com.sentinelx.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelx.IntegrationTestBase;
import com.sentinelx.detection.engine.DetectionRuleEngine;
import com.sentinelx.detection.repository.ThreatRepository;
import com.sentinelx.event.repository.SecurityEventRepository;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Vì RabbitMQ không chạy thật trong môi trường test (xem IntegrationTestBase), ta kiểm chứng
 * pipeline detection bằng cách: (1) gọi API thật để INSERT event qua đúng luồng nghiệp vụ
 * (validation, normalize IP...), rồi (2) gọi trực tiếp DetectionRuleEngine.evaluate() -- đúng
 * logic mà EventDetectionConsumer sẽ gọi khi nhận message thật, chỉ bỏ qua lớp truyền tải AMQP.
 */
class EventIngestionIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SecurityEventRepository eventRepository;
    @Autowired private ThreatRepository threatRepository;
    @Autowired private DetectionRuleEngine detectionRuleEngine;

    @Test
    @WithMockUser(username = "tester", roles = {"ADMIN"})
    void fiveFailedLogins_fromSameIp_createsExactlyOneBruteForceThreat() throws Exception {
        String sourceIp = "198.51.100.199";

        for (int i = 0; i < 5; i++) {
            Map<String, Object> body = Map.of(
                    "timestamp", Instant.now().toString(),
                    "eventType", "LOGIN_FAILED",
                    "source", "AUTH_SERVICE",
                    "sourceIp", sourceIp,
                    "username", "victim",
                    "severity", "MEDIUM");

            String response = mockMvc.perform(post("/api/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String eventId = objectMapper.readTree(response).path("data").path("id").asText();
            var event = eventRepository.findById(java.util.UUID.fromString(eventId)).orElseThrow();
            detectionRuleEngine.evaluate(event);
        }

        long threatCount = threatRepository.findAll().stream()
                .filter(t -> t.getSourceIp().equals(sourceIp))
                .count();

        assertThat(threatCount).isEqualTo(1);
    }
}