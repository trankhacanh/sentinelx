CREATE TABLE roles (
    id   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    full_name     VARCHAR(100),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- Phòng thủ nhiều lớp: DB cũng từ chối dữ liệu chưa chuẩn hóa
    CONSTRAINT chk_users_username_lowercase CHECK (username = lower(username)),
    CONSTRAINT chk_users_email_lowercase    CHECK (email = lower(email))
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles (id),
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);

-- Tập role cố định của hệ thống, phải khớp enum RoleName
INSERT INTO roles (name) VALUES
    ('ADMIN'), ('SOC_ANALYST'), ('SECURITY_ENGINEER'), ('VIEWER');