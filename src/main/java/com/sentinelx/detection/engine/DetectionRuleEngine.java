package com.sentinelx.detection.engine;

import com.sentinelx.detection.entity.DetectionRule;
import com.sentinelx.detection.entity.Threat;
import com.sentinelx.detection.repository.DetectionRuleRepository;
import com.sentinelx.detection.repository.ThreatRepository;
import com.sentinelx.detection.rule.ThreatDetector;
import com.sentinelx.event.entity.SecurityEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.sentinelx.common.model.Severity;
import com.sentinelx.detection.service.RiskScoreService;
import com.sentinelx.alert.service.AlertService;


/**
 * Nhận Event -> Nạp rule đang bật -> Đánh giá từng detector -> Tạo Threat -> Tính Risk.
 */
@Component
public class DetectionRuleEngine {

    private static final Logger log = LoggerFactory.getLogger(DetectionRuleEngine.class);

    private final List<ThreatDetector> detectors;
    private final DetectionRuleRepository ruleRepository;
    private final ThreatRepository threatRepository;
    private final RiskScoreService riskScoreService;
    private final AlertService alertService;


      public DetectionRuleEngine(List<ThreatDetector> detectors, DetectionRuleRepository ruleRepository,
                               ThreatRepository threatRepository, RiskScoreService riskScoreService,
                               AlertService alertService) {
        this.detectors = detectors;
        this.ruleRepository = ruleRepository;
        this.threatRepository = threatRepository;
        this.riskScoreService = riskScoreService;
        this.alertService = alertService;
    }

    /**
     * CHẠY CÙNG TRANSACTION với SecurityEventService.ingest() (propagation mặc định REQUIRED,
     * cùng connection). Bắt buộc phải vậy: Threat tham chiếu event_id qua foreign key, và event
     * đó chỉ tồn tại trong transaction hiện tại (chưa commit). Hai transaction riêng (connection
     * khác nhau) sẽ không "nhìn thấy" nhau do isolation level READ COMMITTED của PostgreSQL —
     * cả khi ĐỌC (đếm sai, đã gặp) lẫn khi GHI (vi phạm FK, vừa gặp).
     *
     * Đánh đổi: nếu một detector ném exception KHÔNG được bắt, toàn bộ transaction (bao gồm cả
     * việc lưu event) sẽ rollback theo. Ta chấp nhận đánh đổi này và giảm thiểu rủi ro bằng cách
     * bọc try/catch quanh TỪNG detector bên dưới — một detector lỗi chỉ bị bỏ qua (log lại),
     * không ảnh hưởng detector khác hay việc lưu event.
     */
    @Transactional
    public void evaluate(SecurityEvent event) {
        for (ThreatDetector detector : detectors) {
            try {
                evaluateOne(detector, event);
            } catch (Exception ex) {
                log.error("Detector {} failed for event {}: {}",
                        detector.getClass().getSimpleName(), event.getId(), ex.getMessage(), ex);
            }
        }
    }

    private void evaluateOne(ThreatDetector detector, SecurityEvent event) {
        DetectionRule rule = ruleRepository.findByRuleCode(detector.ruleCode()).orElse(null);
        if (rule == null) {
            log.warn("No DetectionRule row found for rule code {}. Did you add the Flyway seed?",
                    detector.ruleCode());
            return;
        }
        if (!rule.isEnabled()) {
            return;
        }
                if (!detector.detect(event)) {
            return;
        }

        int riskScore = riskScoreService.calculate(rule, event);
        Severity severity = Severity.fromScore(riskScore);

        Threat threat = new Threat(
                event.getId(),
                rule,
                rule.getThreatType(),
                severity,
                riskScore,
                event.getSourceIp(),
                "Rule '" + rule.getName() + "' triggered by event " + event.getId()
                        + " (risk score " + riskScore + ")");

        threatRepository.save(threat);
        log.info("Threat created: type={} sourceIp={} riskScore={} severity={} ruleCode={}",
                threat.getThreatType(), threat.getSourceIp(), threat.getRiskScore(), severity, rule.getRuleCode());
            
        Threat savedThreat = threatRepository.save(threat);
        alertService.createForThreat(savedThreat);

        log.info("Threat created: type={} sourceIp={} riskScore={} severity={} ruleCode={}",
                savedThreat.getThreatType(), savedThreat.getSourceIp(), savedThreat.getRiskScore(),
                severity, rule.getRuleCode());
    }
}