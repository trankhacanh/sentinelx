package com.sentinelx.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = b64("0123456789abcdef0123456789abcdef");
    private static final String OTHER_SECRET = b64("fedcba9876543210fedcba9876543210");

    private static String b64(String raw) {
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static JwtService service(String secret, Duration ttl, String issuer) {
        return new JwtService(new JwtProperties(secret, ttl, issuer));
    }

    @Test
    void generatedToken_roundTripsUserId() {
        JwtService jwt = service(SECRET, Duration.ofMinutes(5), "sentinelx");
        UUID id = UUID.randomUUID();

        assertEquals(id, jwt.parseUserId(jwt.generateToken(id)));
    }

    @Test
    void expiredToken_isRejected() {
        JwtService jwt = service(SECRET, Duration.ofSeconds(-10), "sentinelx");
        String token = jwt.generateToken(UUID.randomUUID());

        assertThrows(JwtException.class, () -> jwt.parseUserId(token));
    }

    @Test
    void tokenSignedWithAnotherKey_isRejected() {
        String forged = service(OTHER_SECRET, Duration.ofMinutes(5), "sentinelx")
                .generateToken(UUID.randomUUID());

        assertThrows(JwtException.class,
                () -> service(SECRET, Duration.ofMinutes(5), "sentinelx").parseUserId(forged));
    }

    @Test
    void tokenFromDifferentIssuer_isRejected() {
        String token = service(SECRET, Duration.ofMinutes(5), "someone-else")
                .generateToken(UUID.randomUUID());

        assertThrows(JwtException.class,
                () -> service(SECRET, Duration.ofMinutes(5), "sentinelx").parseUserId(token));
    }

    @Test
    void garbageToken_isRejected() {
        JwtService jwt = service(SECRET, Duration.ofMinutes(5), "sentinelx");

        assertThrows(JwtException.class, () -> jwt.parseUserId("not.a.jwt"));
    }

    @Test
    void shortSecret_failsFast() {
        assertThrows(IllegalStateException.class,
                () -> service(b64("too-short-secret"), Duration.ofMinutes(5), "sentinelx"));
    }
}