package com.sentinelx.auth.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sentinelx.security.jwt")
public record JwtProperties(String secret, Duration expiration, String issuer) {

    /** Không bao giờ in secret ra log. */
    @Override
    public String toString() {
        return "JwtProperties[expiration=" + expiration + ", issuer=" + issuer + "]";
    }
}