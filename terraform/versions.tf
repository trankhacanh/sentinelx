terraform {
  required_version = ">= 1.9"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.70"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  # Chưa cấu hình backend thật -- tên bucket S3 phải DUY NHẤT TOÀN CẦU, không thể đoán trước.
  # Khi deploy thật, bỏ comment và điền tên bucket/table riêng:
  #
  # backend "s3" {
  #   bucket         = "your-unique-bucket-name-sentinelx-tfstate"
  #   key            = "sentinelx/terraform.tfstate"
  #   region         = "ap-southeast-1"
  #   dynamodb_table = "sentinelx-tf-lock"
  #   encrypt        = true
  # }
}

provider "aws" {
  region = var.aws_region
}