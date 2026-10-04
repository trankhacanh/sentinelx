CREATE TABLE threats (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id    UUID         NOT NULL REFERENCES security_events (id),
    rule_id     UUID         NOT NULL REFERENCES detection_rules (id),
    threat_type VARCHAR(40)  NOT NULL,
    severity    VARCHAR(10)  NOT NULL,
    risk_score  INTEGER      NOT NULL,
    source_ip   VARCHAR(45)  NOT NULL,
    description TEXT,
    detected_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_threats_severity
        CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_threats_risk_range
        CHECK (risk_score BETWEEN 0 AND 100)
);

-- Dashboard/alert sẽ lọc theo IP và thời gian thường xuyên
CREATE INDEX idx_threats_source_ip_time ON threats (source_ip, detected_at DESC);
CREATE INDEX idx_threats_detected_at ON threats (detected_at DESC);
CREATE INDEX idx_threats_rule_id ON threats (rule_id);