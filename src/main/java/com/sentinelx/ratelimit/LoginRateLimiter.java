package com.sentinelx.ratelimit;

import com.sentinelx.common.exception.TooManyRequestsException;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Fixed-window rate limiter: INCR tăng bộ đếm, EXPIRE (chỉ áp dụng lần đầu tạo key, dùng cờ NX
 * tương đương "chỉ set TTL nếu key mới") đặt cửa sổ window phút tính từ request ĐẦU TIÊN của IP
 * đó. Đơn giản, đủ dùng để chặn brute-force vào endpoint login (xem quyết định kiến trúc 2a).
 */
@Component
public class LoginRateLimiter {

    private static final String KEY_PREFIX = "ratelimit:login:";

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;

    public LoginRateLimiter(StringRedisTemplate redisTemplate, RateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    /** Ném TooManyRequestsException nếu IP này đã vượt ngưỡng trong cửa sổ hiện tại. */
    public void checkAndIncrement(String sourceIp) {
        String key = KEY_PREFIX + sourceIp;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1L) {
            // Chỉ lần đầu tiên tạo key mới mới đặt TTL -- các lần INCR sau giữ nguyên TTL gốc,
            // đúng ngữ nghĩa "cửa sổ cố định tính từ request đầu tiên".
            redisTemplate.expire(key, properties.login().window());
        }

        int maxAttempts = properties.login().maxAttempts();
        if (count != null && count > maxAttempts) {
            Long ttlSeconds = redisTemplate.getExpire(key);
            Duration retryAfter = (ttlSeconds != null && ttlSeconds > 0)
                    ? Duration.ofSeconds(ttlSeconds) : properties.login().window();
            throw new TooManyRequestsException(
                    "Too many login attempts. Please try again later.", retryAfter);
        }
    }
}