import api from './axios';
import type { User, BlacklistEntry } from '../types';

interface UpdateUserPayload {
  firstName?: string;
  lastName?: string;
  middleName?: string;
  cityId?: string;
  phoneNumber?: string;
  passportSeries?: string;
  passportNumber?: string;
}

export const userApi = {
  getMe: () =>
    api.get<User>('/user/me').then(r => ({
      ...r.data,
      avatarUrl: `/api/files/avatar/${r.data.id}`,
    })),

  updateMe: (data: UpdateUserPayload) =>
    api.patch<string>('/user/me', data).then(r => r.data),

  checkBlacklist: () =>
    api.get<BlacklistEntry>('/user/me/blacklist-check').then(r => r.data),

  addToBlacklist: (userId: string, reason: string, days: number) =>
    api.post<string>(`/user/blacklist/${userId}`, { reason, days }).then(r => r.data),

  uploadAvatar: async (file: File) => {
    const bytes = await file.arrayBuffer();
    await api.post<void>('/files/avatar', bytes, {
      headers: { 'Content-Type': 'application/octet-stream' },
    });
  },

  deleteAvatar: () =>
    api.delete('/files/avatar').then(r => r.data),

  searchByNickname: (nickname: string) =>
    api.get<{ id: number; nickname: string }[]>('/user/users/search', { params: { nickname } }).then(r => r.data),

  getUserById: (id: number) =>
    api.get<{ id: number; nickname: string }>(`/user/users/${id}`).then(r => r.data),
};
