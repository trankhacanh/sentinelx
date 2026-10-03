package com.sentinelx.detection.repository;

import com.sentinelx.detection.entity.DetectionRule;
import com.sentinelx.detection.entity.RuleCode;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetectionRuleRepository extends JpaRepository<DetectionRule, UUID> {

    Optional<DetectionRule> findByRuleCode(RuleCode ruleCode);
}