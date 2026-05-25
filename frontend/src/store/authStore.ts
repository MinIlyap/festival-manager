import { create } from 'zustand';
import type { User, UserRole } from '../types';

function decodeRole(token: string): UserRole | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return (payload.role as UserRole) ?? null;
  } catch {
    return null;
  }
}

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: User | null;
  role: UserRole | null;

  setTokens: (accessToken: string, refreshToken: string) => void;
  setUser: (user: User) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>(() => {
  const storedAccess = localStorage.getItem('accessToken');
  const storedRefresh = localStorage.getItem('refreshToken');
  return {
    accessToken: storedAccess,
    refreshToken: storedRefresh,
    user: null,
    role: storedAccess ? decodeRole(storedAccess) : null,

    setTokens: (accessToken, refreshToken) => {
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      useAuthStore.setState({ accessToken, refreshToken, role: decodeRole(accessToken) });
    },

    setUser: (user) => useAuthStore.setState({ user }),

    logout: () => {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      useAuthStore.setState({ accessToken: null, refreshToken: null, user: null, role: null });
    },
  };
});
