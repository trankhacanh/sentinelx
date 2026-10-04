INSERT INTO detection_rules (rule_code, name, description, threat_type, severity, base_risk_score)
VALUES (
    'SUSPICIOUS_LOGIN',
    'Suspicious Login Detection',
    'Phat hien LOGIN_SUCCESS voi >=2 tin hieu dang ngo: IP moi, gio bat thuong, nhieu lan dang nhap that bai gan day, tai khoan nhay cam',
    'SUSPICIOUS_AUTHENTICATION',
    'MEDIUM',
    55
);