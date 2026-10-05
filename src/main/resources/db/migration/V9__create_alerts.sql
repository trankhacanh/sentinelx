CREATE TABLE alerts (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    threat_id   UUID         NOT NULL UNIQUE REFERENCES threats (id),
    title       VARCHAR(200) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    assigned_to UUID         REFERENCES users (id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_alerts_status
        CHECK (status IN ('OPEN', 'ACKNOWLEDGED', 'IN_PROGRESS', 'RESOLVED', 'FALSE_POSITIVE'))
);

CREATE INDEX idx_alerts_status ON alerts (status);
CREATE INDEX idx_alerts_created_at ON alerts (created_at DESC);
-- Partial index: phần lớn alert ban đầu chưa được gán, không cần lập chỉ mục cho NULL
CREATE INDEX idx_alerts_assigned_to ON alerts (assigned_to) WHERE assigned_to IS NOT NULL;