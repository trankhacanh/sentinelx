package com.sentinelx.event.service;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "sentinelx.events")
public record EventIngestionProperties(
        @DefaultValue("5m") Duration maxFutureSkew,
        @DefaultValue("8192") int maxPayloadBytes) {
}