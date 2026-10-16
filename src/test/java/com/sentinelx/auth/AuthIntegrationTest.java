package com.sentinelx.auth;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelx.IntegrationTestBase;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Kiểm chứng end-to-end qua MockMvc + Postgres thật: register -> login -> JWT hợp lệ cho phép
 * truy cập /api/users/me. Đây là luồng không thể kiểm chứng đầy đủ bằng unit test thuần Mockito,
 * vì nó đi qua BCryptPasswordEncoder, JwtAuthenticationFilter, và CustomUserDetailsService thật.
 */
class AuthIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerThenLogin_grantsAccessToProtectedEndpoint() throws Exception {
        Map<String, String> registerBody = Map.of(
                "username", "itest_user1",
                "email", "itest_user1@example.com",
                "password", "StrongPass123",
                "fullName", "Integration Test User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.roles[0]", is("VIEWER")));

        Map<String, String> loginBody = Map.of("username", "itest_user1", "password", "StrongPass123");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username", is("itest_user1")));
    }

    @Test
    void login_withWrongPassword_returns401WithGenericMessage() throws Exception {
        Map<String, String> registerBody = Map.of(
                "username", "itest_user2",
                "email", "itest_user2@example.com",
                "password", "StrongPass123",
                "fullName", "User Two");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody)));

        Map<String, String> loginBody = Map.of("username", "itest_user2", "password", "WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid username or password")));
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}