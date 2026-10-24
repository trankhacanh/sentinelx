# SentinelX — Terraform AWS Infrastructure

Hạ tầng as-code cho SentinelX trên AWS: VPC 2-AZ, RDS PostgreSQL, ElastiCache Redis,
Amazon MQ (RabbitMQ), ECS Fargate (backend + frontend), ALB.

**Đây là infrastructure-as-code CHƯA ĐƯỢC APPLY lên tài khoản AWS thật** — viết để chứng minh
năng lực thiết kế hạ tầng cho đồ án, chạy `terraform plan` để xác nhận cú pháp hợp lệ.

## Trước khi deploy thật (nếu muốn)

1. Cấu hình AWS credentials: `aws configure`
2. Tạo S3 bucket + DynamoDB table cho Terraform state, bỏ comment backend trong `versions.tf`
3. `terraform init`
4. Build + đẩy image lên ECR (2 repository sẽ được Terraform tạo ở `ecs.tf`, nhưng cần đẩy image
   TRƯỚC KHI chạy `ecs.tf` phần service, vì image phải tồn tại để ECS task khởi động được —
   đây là "con gà quả trứng" cố hữu của Terraform + ECR, xử lý bằng cách build/push image thủ
   công một lần đầu, các lần sau GitHub Actions có thể tự động hóa):
5. `cp terraform.tfvars.example terraform.tfvars`, điền giá trị image URL thật
6. `terraform plan` — xem lại kế hoạch trước khi áp dụng
7. `terraform apply`

## Ước tính chi phí (region ap-southeast-1, chạy liên tục)

| Resource | Loại | Ước tính/tháng |
|---|---|---|
| RDS db.t4g.micro | Single-AZ | ~$15 |
| ElastiCache cache.t4g.micro | 1 node | ~$12 |
| Amazon MQ mq.t3.micro | Single instance | ~$25 |
| ECS Fargate | 2 backend + 2 frontend task, nhỏ | ~$30 |
| ALB | | ~$20 |
| NAT Gateway | | ~$35 |
| **Tổng** | | **~$137/tháng** |

Đây là lý do dự án này **chỉ viết sẵn, không apply** — chi phí không cần thiết cho một đồ án
cá nhân nếu không có nhu cầu demo online liên tục.

## Dọn dẹp (nếu đã apply)
