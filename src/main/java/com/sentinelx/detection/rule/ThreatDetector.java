package com.sentinelx.detection.rule;

import com.sentinelx.detection.entity.RuleCode;
import com.sentinelx.event.entity.SecurityEvent;

/**
 * Mỗi rule implement interface này. DetectionRuleEngine lặp qua tất cả bean loại này
 * (Spring tự động tiêm danh sách) — thêm rule mới không cần sửa engine (Open/Closed Principle).
 */
public interface ThreatDetector {

    /** Dùng để engine tra DetectionRule tương ứng (enabled, severity, base risk) trong DB. */
    RuleCode ruleCode();

    /**
     * Kiểm tra event này có khớp điều kiện của rule không.
     * Trả về true nếu một Threat MỚI nên được tạo (đã tự kiểm tra trùng lặp bên trong, xem 2d).
     * KHÔNG tự lưu Threat — chỉ trả về quyết định; DetectionRuleEngine chịu trách nhiệm lưu.
     */
    boolean detect(SecurityEvent event);
}