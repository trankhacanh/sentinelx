package com.sentinelx;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelx.auth.security.JwtProperties;
import com.sentinelx.auth.security.JwtService;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Kiểm chứng các hàng rào bảo vệ của đặc tả mục 30 trên TOÀN BỘ pipeline HTTP thật (filter chain
 * thật, không mock), bổ sung cho các unit test đã có (JwtServiceTest test riêng tầng JWT thuần).
 */
class SecurityIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${sentinelx.security.jwt.secret}")
    private String jwtSecret;

    @Value("${sentinelx.security.jwt.issuer}")
    private String jwtIssuer;

    @Test
    void unauthenticatedRequest_toProtectedEndpoint_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", org.hamcrest.Matchers.is("UNAUTHORIZED")));
    }

    @Test
    void invalidJwt_malformedToken_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer this.is.not.a.valid.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwt_wrongSignature_returns401() throws Exception {
        // Token hợp lệ về CẤU TRÚC nhưng ký bằng secret KHÁC -> chữ ký không khớp khi xác minh.
        JwtProperties forgedProperties = new JwtProperties(
                "ZGlmZmVyZW50LXNlY3JldC1rZXktdGhhdC1pcy1hbHNvLTMyLWJ5dGVzLWxvbmc=",
                Duration.ofMinutes(60), jwtIssuer);
        JwtService forgedJwtService = new JwtService(forgedProperties);
        String forgedToken = forgedJwtService.generateToken(UUID.randomUUID());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + forgedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredJwt_returns401() throws Exception {
        // expiration ÂM -> token được sinh ra đã hết hạn ngay lập tức, không cần chờ thật.
        JwtProperties expiredProperties = new JwtProperties(jwtSecret, Duration.ofSeconds(-10), jwtIssuer);
        JwtService expiredJwtService = new JwtService(expiredProperties);
        String expiredToken = expiredJwtService.generateToken(UUID.randomUUID());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthorizedRole_viewerCannotIngestEvent() throws Exception {
        registerAndLogin("itest_viewer1", "itest_viewer1@example.com");
        String viewerToken = loginAndGetToken("itest_viewer1", "StrongPass123");

        Map<String, Object> eventBody = Map.of(
                "timestamp", java.time.Instant.now().toString(),
                "eventType", "LOGIN_FAILED",
                "source", "AUTH_SERVICE",
                "sourceIp", "203.0.113.10",
                "severity", "MEDIUM");

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventBody)))
                .andExpect(status().isForbidden());
    }

        @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "tester", roles = {"ADMIN"})
    void invalidInput_malformedIp_returns400NotServerError() throws Exception {
        Map<String, Object> badBody = Map.of(
                "timestamp", java.time.Instant.now().toString(),
                "eventType", "LOGIN_FAILED",
                "source", "AUTH_SERVICE",
                "sourceIp", "999.1.1.1",
                "severity", "MEDIUM");

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", org.hamcrest.Matchers.is("VALIDATION_ERROR")));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "tester", roles = {"ADMIN"})
    void invalidInput_missingRequiredField_returns400() throws Exception {
        Map<String, Object> badBody = Map.of(
                "eventType", "LOGIN_FAILED",
                "source", "AUTH_SERVICE",
                "sourceIp", "10.0.0.1",
                "severity", "MEDIUM");

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rateLimiting_sixthLoginAttempt_returns429() throws Exception {
        Map<String, String> loginBody = Map.of("username", "nonexistent_user", "password", "wrong");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginBody)));
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isTooManyRequests());
    }

    private void registerAndLogin(String username, String email) throws Exception {
        Map<String, String> registerBody = Map.of(
                "username", username, "email", email,
                "password", "StrongPass123", "fullName", "Test User");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody)));
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        Map<String, String> loginBody = Map.of("username", username, "password", password);
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("accessToken").asText();
    }
}