package com.sentinelx.ratelimit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sentinelx.common.exception.TooManyRequestsException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class LoginRateLimiterTest {

    private static final String IP = "203.0.113.5";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        rateLimiter = new LoginRateLimiter(redisTemplate,
                new RateLimitProperties(new RateLimitProperties.Login(5, Duration.ofMinutes(15))));
    }

    @Test
    void firstRequest_setsExpiryAndPasses() {
        when(valueOperations.increment("ratelimit:login:" + IP)).thenReturn(1L);

        assertDoesNotThrow(() -> rateLimiter.checkAndIncrement(IP));

        verify(redisTemplate).expire(eq("ratelimit:login:" + IP), eq(Duration.ofMinutes(15)));
    }

    @Test
    void subsequentRequestWithinLimit_doesNotResetExpiry() {
        when(valueOperations.increment("ratelimit:login:" + IP)).thenReturn(3L);

        assertDoesNotThrow(() -> rateLimiter.checkAndIncrement(IP));

        verify(redisTemplate, org.mockito.Mockito.never()).expire(any(), any());
    }

    @Test
    void exceedingThreshold_throws() {
        when(valueOperations.increment("ratelimit:login:" + IP)).thenReturn(6L);
        when(redisTemplate.getExpire("ratelimit:login:" + IP)).thenReturn(600L);

        TooManyRequestsException ex = assertThrows(TooManyRequestsException.class,
                () -> rateLimiter.checkAndIncrement(IP));

        assertDoesNotThrow(() -> ex.getRetryAfter());
    }

    @Test
    void atExactThreshold_doesNotThrow() {
        when(valueOperations.increment("ratelimit:login:" + IP)).thenReturn(5L);

        assertDoesNotThrow(() -> rateLimiter.checkAndIncrement(IP));
    }
}