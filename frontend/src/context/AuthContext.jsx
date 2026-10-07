import { createContext, useContext, useEffect, useState } from 'react';
import { getCurrentUser, login as loginApi } from '../api/auth';

const AuthContext = createContext(null);

const TOKEN_KEY = 'sentinelx_token';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  // "loading" phân biệt "chưa biết đã đăng nhập chưa" (đang kiểm tra token cũ lúc tải trang)
  // với "chắc chắn chưa đăng nhập" -> tránh ProtectedRoute đá về /login một cách giả (false negative)
  // ngay khi người dùng F5 lại trang dù token vẫn còn hợp lệ.
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) {
      setLoading(false);
      return;
    }
    getCurrentUser()
      .then((res) => setUser(res.data.data))
      .catch(() => localStorage.removeItem(TOKEN_KEY))
      .finally(() => setLoading(false));
  }, []);

  async function login(username, password) {
    const res = await loginApi(username, password);
    const { accessToken } = res.data.data;
    localStorage.setItem(TOKEN_KEY, accessToken);
    const profile = await getCurrentUser();
    setUser(profile.data.data);
    return profile.data.data;
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY);
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}