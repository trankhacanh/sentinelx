package com.sentinelx.common.model;

/**
 * Băng điểm theo đặc tả mục 11:
 * 0-29 LOW, 30-59 MEDIUM, 60-79 HIGH, 80-100 CRITICAL.
 */
public enum Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static Severity fromScore(int score) {
        if (score >= 80) {
            return CRITICAL;
        }
        if (score >= 60) {
            return HIGH;
        }
        if (score >= 30) {
            return MEDIUM;
        }
        return LOW;
    }
}