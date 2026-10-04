INSERT INTO detection_rules (rule_code, name, description, threat_type, severity, base_risk_score)
VALUES (
    'SQL_INJECTION_PATTERN',
    'SQL Injection Pattern Detection',
    'Phat hien request path/query chua dau hieu cu phap SQL injection pho bien (UNION SELECT, OR 1=1, comment SQL, DROP TABLE...)',
    'SQL_INJECTION',
    'CRITICAL',
    90
);