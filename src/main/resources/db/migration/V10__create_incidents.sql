CREATE TABLE incidents (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    severity     VARCHAR(10)  NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    assigned_to  UUID         REFERENCES users (id),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    resolved_at  TIMESTAMPTZ,
    CONSTRAINT chk_incidents_severity
        CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_incidents_status
        CHECK (status IN ('OPEN', 'INVESTIGATING', 'CONTAINED', 'RESOLVED', 'CLOSED'))
);

CREATE TABLE incident_alerts (
    incident_id UUID NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    alert_id    UUID NOT NULL REFERENCES alerts (id),
    PRIMARY KEY (incident_id, alert_id)
);

CREATE TABLE incident_notes (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id UUID        NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    author_id   UUID        NOT NULL REFERENCES users (id),
    content     TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_incidents_status ON incidents (status);
CREATE INDEX idx_incidents_created_at ON incidents (created_at DESC);
CREATE INDEX idx_incident_alerts_alert_id ON incident_alerts (alert_id);
CREATE INDEX idx_incident_notes_incident_id ON incident_notes (incident_id, created_at);