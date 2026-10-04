INSERT INTO detection_rules (rule_code, name, description, threat_type, severity, base_risk_score)
VALUES (
    'PORT_SCAN',
    'Port Scan Detection',
    'Phat hien >=20 cong dich khac nhau tu cung source IP trong vong 30 giay',
    'PORT_SCAN',
    'HIGH',
    70
);