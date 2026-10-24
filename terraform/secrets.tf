resource "random_password" "db_password" {
  length  = 32
  special = false # Tránh ký tự đặc biệt gây lỗi parse connection string
}

resource "random_password" "jwt_secret" {
  length  = 32
}

resource "random_password" "rabbitmq_password" {
  length  = 32
  special = false
}

# Mỗi secret một entry RIÊNG trong Secrets Manager -- đúng bài học Phase 11/14.2 (không dùng
# chung một secret cho nhiều service), và cho phép rotate từng cái độc lập sau này.
resource "aws_secretsmanager_secret" "db_password" {
  name = "${var.project_name}/db-password"
}
resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = random_password.db_password.result
}

resource "aws_secretsmanager_secret" "jwt_secret" {
  name = "${var.project_name}/jwt-secret"
}
resource "aws_secretsmanager_secret_version" "jwt_secret" {
  secret_id     = aws_secretsmanager_secret.jwt_secret.id
  secret_string = base64encode(random_password.jwt_secret.result)
}

resource "aws_secretsmanager_secret" "rabbitmq_password" {
  name = "${var.project_name}/rabbitmq-password"
}
resource "aws_secretsmanager_secret_version" "rabbitmq_password" {
  secret_id     = aws_secretsmanager_secret.rabbitmq_password.id
  secret_string = random_password.rabbitmq_password.result
}