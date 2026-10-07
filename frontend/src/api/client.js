import axios from 'axios';

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Gắn token vào MỌI request, đọc trực tiếp từ localStorage mỗi lần (không cache trong closure)
// để luôn phản ánh đúng trạng thái đăng nhập hiện tại, kể cả sau khi AuthContext cập nhật.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('sentinelx_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 401 từ backend (token hết hạn/không hợp lệ) -> dọn dẹp và đưa về trang login.
// Đặt ở một nơi duy nhất thay vì xử lý rải rác ở từng page gọi API.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('sentinelx_token');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default client;