package com.sentinelx.ratelimit;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "sentinelx.rate-limit")
public record RateLimitProperties(@NestedConfigurationProperty Login login) {

    public record Login(int maxAttempts, Duration window) {
    }
}