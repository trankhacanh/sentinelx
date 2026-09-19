package com.sentinelx.system;

import com.sentinelx.common.response.ApiResponse;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final JdbcTemplate jdbcTemplate;
    private final String applicationName;

    // Constructor injection: dependency rõ ràng, field final, dễ mock khi test.
    public HealthController(JdbcTemplate jdbcTemplate,
                            @Value("${spring.application.name}") String applicationName) {
        this.jdbcTemplate = jdbcTemplate;
        this.applicationName = applicationName;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<HealthResponse>> health() {
        boolean dbUp = isDatabaseUp();
        HealthResponse body = new HealthResponse(
                dbUp ? "UP" : "DEGRADED",
                applicationName,
                dbUp ? "UP" : "DOWN",
                Instant.now());

        if (dbUp) {
            return ResponseEntity.ok(ApiResponse.ok(body));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiResponse<>(false, "Database unavailable", body, Instant.now()));
    }

    private boolean isDatabaseUp() {
        try {
            Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Integer.valueOf(1).equals(one);
        } catch (Exception ex) {
            log.warn("Database health check failed: {}", ex.getClass().getSimpleName());
            return false;
        }
    }
}