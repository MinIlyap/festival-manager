import api from './axios';
import type { UserRole } from '../types';

interface LoginPayload { email: string; password: string; }

interface RegisterUserPayload {
  nickname: string;
  email: string;
  password: string;
  birth_date: string;
  city_id: number;
}

interface RegisterCompanyPayload {
  email: string;
  tin: string;
  full_name: string;
  brand_name: string;
  website?: string;
}

interface RawAuthResponse {
  access_token: string;
  refresh_token: string;
  role?: UserRole;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  role?: UserRole;
}

function normalize(raw: RawAuthResponse): AuthTokens {
  return {
    accessToken: raw.access_token,
    refreshToken: raw.refresh_token,
    role: raw.role,
  };
}

export const authApi = {
  login: (data: LoginPayload) =>
    api.post<RawAuthResponse>('/auth/login', data).then(r => normalize(r.data)),

  register: (data: RegisterUserPayload) =>
    api.post<RawAuthResponse>('/auth/register/user', data).then(r => normalize(r.data)),

  registerCompany: (data: RegisterCompanyPayload) =>
    api.post('/auth/register/company', data).then(r => r.data),

  refresh: (refreshToken: string) =>
    api.post<RawAuthResponse>('/auth/refresh', { refresh_token: refreshToken })
      .then(r => normalize(r.data)),

  logout: () => Promise.resolve(),

  getYandexAuthUrl: () =>
    Promise.resolve({ url: '/api/oauth2/authorization/yandex' }),

  changeEmail: (newEmail: string) =>
    api.put('/auth/email', { new_email: newEmail }).then(r => r.data),

  changePassword: (data: { oldPassword: string; newPassword: string }) =>
    api.put('/auth/password', { old_password: data.oldPassword, new_password: data.newPassword })
      .then(r => r.data),

  getMe: () =>
    api.get<{ email: string; accessType: string; userId: number | null; companyId: number | null }>('/auth/me')
      .then(r => r.data),
};
