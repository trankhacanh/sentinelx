INSERT INTO detection_rules (rule_code, name, description, threat_type, severity, base_risk_score)
VALUES (
    'HIGH_REQUEST_RATE',
    'High Request Rate Detection',
    'Phat hien >=100 HTTP_REQUEST tu cung source IP trong vong 10 giay',
    'HIGH_REQUEST_RATE',
    'MEDIUM',
    50
);