resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-db-subnet"
  subnet_ids = aws_subnet.private[*].id
}

resource "aws_db_instance" "postgres" {
  identifier     = "${var.project_name}-postgres"
  engine         = "postgres"
  engine_version = "16"
  instance_class = var.db_instance_class

  allocated_storage = 20
  storage_encrypted = true

  db_name  = "sentinelx"
  username = "sentinelx"
  password = random_password.db_password.result

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.data.id]

  # Demo/portfolio: không cần Multi-AZ (tăng chi phí gấp đôi). Bật multi_az = true cho production thật.
  multi_az = false

  backup_retention_period = 7
  skip_final_snapshot     = true # Demo only - production thật nên để false + final_snapshot_identifier

  tags = { Name = "${var.project_name}-postgres" }
}

resource "aws_elasticache_subnet_group" "main" {
  name       = "${var.project_name}-redis-subnet"
  subnet_ids = aws_subnet.private[*].id
}

resource "aws_elasticache_cluster" "redis" {
  cluster_id         = "${var.project_name}-redis"
  engine             = "redis"
  engine_version     = "7.1"
  node_type          = var.redis_node_type
  num_cache_nodes    = 1
  port               = 6379
  subnet_group_name  = aws_elasticache_subnet_group.main.name
  security_group_ids = [aws_security_group.data.id]
}

resource "aws_mq_broker" "rabbitmq" {
  broker_name        = "${var.project_name}-rabbitmq"
  engine_type        = "RabbitMQ"
  engine_version     = "3.13"
  host_instance_type = "mq.t3.micro"
  deployment_mode    = "SINGLE_INSTANCE" # Demo/portfolio - CLUSTER_MULTI_AZ cho production thật

  user {
    username = "sentinelx"
    password = random_password.rabbitmq_password.result
  }

  subnet_ids         = [aws_subnet.private[0].id]
  security_groups    = [aws_security_group.data.id]
  publicly_accessible = false
}