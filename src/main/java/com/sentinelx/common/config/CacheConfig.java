package com.sentinelx.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.sentinelx.detection.entity.DetectionRule;
import java.time.Duration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Bật Spring Cache abstraction, provider là Redis (spring.cache.type=redis).
 *
 * QUAN TRỌNG: mặc định, RedisCacheManager dùng JdkSerializationRedisSerializer cho cache value --
 * yêu cầu object phải implement java.io.Serializable. Entity JPA (như DetectionRule) không, và
 * KHÔNG NÊN bị ép implement Serializable chỉ để phục vụ cache (trộn lẫn mối quan tâm JPA/caching).
 * Bean RedisCacheConfiguration dưới đây ghi đè serializer mặc định bằng
 * GenericJackson2JsonRedisSerializer (cùng cơ chế JSON đã dùng cho RabbitMQ ở Phase 8) --
 * Spring Boot tự động áp dụng cấu hình này cho MỌI cache tạo qua @Cacheable, không cần khai báo
 * riêng từng cache name.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration(ObjectMapper objectMapper) {
        ObjectMapper cacheObjectMapper = objectMapper.copy();
        cacheObjectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(DetectionRule.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(60))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(cacheObjectMapper)));
    }
}