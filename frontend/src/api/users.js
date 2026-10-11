import client from './client';

/** Chỉ ADMIN được phép gọi -- xem quyết định kiến trúc 2b ở Bước 10.5. */
export function listAllUsers(params = {}) {
  return client.get('/api/admin/users', { params });
}