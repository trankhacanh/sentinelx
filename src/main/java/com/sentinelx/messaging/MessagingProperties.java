package com.sentinelx.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sentinelx.messaging")
public record MessagingProperties(String exchange, String deadLetterExchange, String routingKey,
                                  String queue, String deadLetterQueue) {
}