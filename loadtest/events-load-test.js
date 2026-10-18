import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ADMIN_USERNAME = __ENV.ADMIN_USERNAME || 'admin';
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD;

// Hai giai đoạn tăng dần: 10 req/s (baseline) -> 100 req/s (mục tiêu đặc tả mục 28).
// Không thử 1000 req/s ở đây -- mục tiêu là XÁC NHẬN kiến trúc đúng hướng trên máy cá nhân,
// không phải benchmark server production (xem quyết định kiến trúc 2e).
export const options = {
  scenarios: {
    baseline_10rps: {
      executor: 'constant-arrival-rate',
      rate: 10,
      timeUnit: '1s',
      duration: '30s',
      preAllocatedVUs: 20,
      maxVUs: 50,
    },
    target_100rps: {
      executor: 'constant-arrival-rate',
      rate: 100,
      timeUnit: '1s',
      duration: '30s',
      preAllocatedVUs: 50,
      maxVUs: 150,
      startTime: '35s', // chạy sau khi baseline kết thúc
    },
  },
  thresholds: {
    // Vì detection chạy BẤT ĐỒNG BỘ qua RabbitMQ (Phase 8), request ghi event phải trả lời
    // NHANH -- không bị chặn bởi toàn bộ pipeline detection. Ngưỡng p95 < 500ms xác nhận điều
    // đó; nếu vi phạm, đó là dấu hiệu rõ ràng kiến trúc bất đồng bộ KHÔNG phát huy tác dụng.
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'], // dưới 1% request lỗi
  },
};

// setup() chạy MỘT LẦN, TRƯỚC khi các virtual user bắt đầu -- đăng nhập một lần, dùng lại token
// cho mọi VU trong suốt bài test, đúng cách một client thật hoạt động (xem quyết định 2c).
export function setup() {
  if (!ADMIN_PASSWORD) {
    throw new Error('ADMIN_PASSWORD env var is required. Run with: k6 run -e ADMIN_PASSWORD=... events-load-test.js');
  }

  const loginRes = http.post(
      `${BASE_URL}/api/auth/login`,
      JSON.stringify({ username: ADMIN_USERNAME, password: ADMIN_PASSWORD }),
      { headers: { 'Content-Type': 'application/json' } }
  );

  check(loginRes, { 'login succeeded': (r) => r.status === 200 });

  const token = loginRes.json('data.accessToken');
  return { token };
}

export default function (data) {
  const sourceIp = `203.0.113.${Math.floor(Math.random() * 254) + 1}`;

  const payload = JSON.stringify({
    timestamp: new Date().toISOString(),
    eventType: 'LOGIN_FAILED',
    source: 'AUTH_SERVICE',
    sourceIp: sourceIp,
    username: `loadtest_user_${__VU}`,
    severity: 'MEDIUM',
  });

  const res = http.post(`${BASE_URL}/api/events`, payload, {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${data.token}`,
    },
  });

  check(res, {
    'status is 201': (r) => r.status === 201,
  });
}