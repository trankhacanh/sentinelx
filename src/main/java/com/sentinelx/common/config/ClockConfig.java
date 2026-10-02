package com.sentinelx.common.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    /**
     * Inject Clock thay vì gọi Instant.now() trực tiếp: test có thể "đóng băng" thời gian,
     * và các rule dựa trên cửa sổ thời gian ở Phase 4 sẽ kiểm thử được một cách xác định.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}