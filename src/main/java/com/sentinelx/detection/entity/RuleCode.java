package com.sentinelx.detection.entity;

/** Khóa nối giữa bảng detection_rules và logic Java. Thêm rule mới = thêm 1 dòng enum + 1 migration. */
public enum RuleCode {
    BRUTE_FORCE_LOGIN,
    PORT_SCAN,
    SUSPICIOUS_LOGIN,
    SQL_INJECTION_PATTERN,
    HIGH_REQUEST_RATE
}