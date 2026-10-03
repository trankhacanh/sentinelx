CREATE TABLE detection_rules (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_code       VARCHAR(50)  NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    threat_type     VARCHAR(40)  NOT NULL,
    severity        VARCHAR(10)  NOT NULL,
    base_risk_score INTEGER      NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_detection_rules_severity
        CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_detection_rules_risk_range
        CHECK (base_risk_score BETWEEN 0 AND 100)
);

-- Chỉ seed rule đầu tiên theo đúng thứ tự Phase 4 yêu cầu (bắt đầu bằng BRUTE_FORCE_LOGIN)
INSERT INTO detection_rules (rule_code, name, description, threat_type, severity, base_risk_score)
VALUES (
    'BRUTE_FORCE_LOGIN',
    'Brute Force Login Detection',
    'Phat hien >=5 LOGIN_FAILED tu cung source IP trong vong 60 giay',
    'BRUTE_FORCE',
    'HIGH',
    75
);