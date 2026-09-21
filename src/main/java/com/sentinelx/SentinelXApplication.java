package com.sentinelx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SentinelXApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentinelXApplication.class, args);
    }
}