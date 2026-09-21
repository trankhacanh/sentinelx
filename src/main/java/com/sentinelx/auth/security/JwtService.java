package com.sentinelx.auth.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final int MIN_KEY_BYTES = 32; // 256 bit cho HS256

    private final SecretKey key;
    private final String issuer;
    private final long expirationSeconds;

    public JwtService(JwtProperties properties) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(properties.secret());
        } catch (RuntimeException ex) {
            throw new IllegalStateException("JWT_SECRET must be a valid Base64 string", ex);
        }
        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET must decode to at least " + MIN_KEY_BYTES + " bytes (256 bits)");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.issuer = properties.issuer();
        this.expirationSeconds = properties.expiration().toSeconds();
    }

    /** Token chỉ mang user id. Không chứa role, email hay thông tin nhạy cảm. */
    public String generateToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuer(issuer)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key)
                .compact();
    }

    /**
     * Xác minh chữ ký, thời hạn và issuer, rồi trả về user id.
     * Ném JwtException / IllegalArgumentException nếu token không hợp lệ.
     */
    public UUID parseUserId(String token) throws JwtException {
        String subject = Jwts.parser()
                .verifyWith(key)          // chỉ nhận thuật toán HMAC với key này, từ chối alg=none
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return UUID.fromString(subject);
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}