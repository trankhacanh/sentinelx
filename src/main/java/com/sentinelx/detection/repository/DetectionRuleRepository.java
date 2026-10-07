package com.sentinelx.detection.repository;

import com.sentinelx.detection.entity.DetectionRule;
import com.sentinelx.detection.entity.RuleCode;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetectionRuleRepository extends JpaRepository<DetectionRule, UUID> {

    /**
     * Mỗi event đi qua DetectionRuleEngine đều gọi method này (1 lần cho mỗi trong 5 detector).
     * detection_rules gần như tĩnh (chỉ đổi khi SECURITY_ENGINEER bật/tắt rule qua API) -- cache
     * giảm tải DB đáng kể ở throughput cao. Evict tường minh khi rule bị sửa (xem
     * DetectionRuleController), TTL 60s trong application.yml chỉ là lưới an toàn dự phòng.
     */
    @Cacheable(cacheNames = "detectionRules", key = "#ruleCode")
    Optional<DetectionRule> findByRuleCode(RuleCode ruleCode);
}