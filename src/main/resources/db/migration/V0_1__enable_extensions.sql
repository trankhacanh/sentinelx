-- pg_trgm: hỗ trợ tìm kiếm ILIKE '%keyword%' nhanh (dùng cho event search ở Phase 3).
-- Là "trusted extension" từ PostgreSQL 13 nên owner của DB có thể tạo mà không cần superuser.
CREATE EXTENSION IF NOT EXISTS pg_trgm;