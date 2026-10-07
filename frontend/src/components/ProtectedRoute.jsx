import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Chỉ chặn hiển thị ở phía client (UX). Mọi bảo vệ thật sự nằm ở backend (@PreAuthorize) -
 * route này không phải lớp bảo mật, chỉ tránh hiển thị giao diện cho người chưa đăng nhập.
 */
export default function ProtectedRoute() {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="flex h-screen items-center justify-center text-gray-400">Loading...</div>;
  }
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  return <Outlet />;
}