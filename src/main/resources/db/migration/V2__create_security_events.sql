CREATE TABLE security_events (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    event_time     TIMESTAMPTZ  NOT NULL,          -- thời điểm sự kiện xảy ra (do nguồn gửi)
    event_type     VARCHAR(30)  NOT NULL,
    source         VARCHAR(100) NOT NULL,
    source_ip      VARCHAR(45)  NOT NULL,          -- 45 ký tự đủ cho IPv6 dạng đầy đủ + IPv4-mapped
    destination_ip VARCHAR(45),
    username       VARCHAR(100),
    severity       VARCHAR(10)  NOT NULL,
    payload        JSONB,
    -- Cột sinh tự động, chỉ phục vụ tìm kiếm. Hibernate không ghi cột này.
    search_text    TEXT GENERATED ALWAYS AS (
                       lower(source || ' ' || source_ip || ' '
                             || coalesce(destination_ip, '') || ' ' || coalesce(username, ''))
                   ) STORED,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_security_events_severity
        CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_security_events_username_lowercase
        CHECK (username IS NULL OR username = lower(username))
);

-- Danh sách mới nhất, lọc theo khoảng thời gian
CREATE INDEX idx_security_events_event_time
    ON security_events (event_time DESC);

-- Detection (Phase 4): "các event của IP này trong N giây gần nhất"
CREATE INDEX idx_security_events_source_ip_time
    ON security_events (source_ip, event_time DESC);

-- Lọc theo loại event
CREATE INDEX idx_security_events_type_time
    ON security_events (event_type, event_time DESC);

-- Partial index: nhiều event không có username nên không cần lập chỉ mục cho chúng
CREATE INDEX idx_security_events_username_time
    ON security_events (username, event_time DESC)
    WHERE username IS NOT NULL;

-- Tìm kiếm chứa chuỗi con (LIKE '%...%') nhờ pg_trgm bật ở V0_1
CREATE INDEX idx_security_events_search_trgm
    ON security_events USING GIN (search_text gin_trgm_ops);