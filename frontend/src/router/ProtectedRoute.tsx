import { Navigate, Outlet } from 'react-router-dom';
import type { UserRole } from '../types';
import { useAuthStore } from '../store/authStore';

interface ProtectedRouteProps {
  roles?: UserRole[];
}

export default function ProtectedRoute({ roles }: ProtectedRouteProps) {
  const { accessToken, role } = useAuthStore();

  if (!accessToken) return <Navigate to="/auth/login" replace />;

  if (roles && role && !roles.includes(role)) return <Navigate to="/home" replace />;

  return <Outlet />;
}
