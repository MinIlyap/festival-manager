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
    api.get<any>('/user/me').then(r => {
      const d = r.data;
      return {
        id: d.id,
        nickname: d.nickname,
        birthDate: d.birth_date,
        cityId: d.city_id != null ? String(d.city_id) : undefined,
        cityName: d.city_name,
        phoneNumber: d.phone_number,
        firstName: d.first_name,
        lastName: d.last_name,
        middleName: d.middle_name,
        passportSeries: d.passport_series,
        passportNumber: d.passport_number,
        avatarUrl: `/api/files/avatar/${d.id}`,
      } as User;
    }),

  updateMe: (data: UpdateUserPayload) =>
    api.patch<string>('/user/me', {
      first_name: data.firstName,
      last_name: data.lastName,
      middle_name: data.middleName,
      city_id: data.cityId,
      phone_number: data.phoneNumber,
      passport_series: data.passportSeries,
      passport_number: data.passportNumber,
    }).then(r => r.data),

  checkBlacklist: () =>
    api.get<any>('/user/me/blacklist-check').then(r => ({
      inBlacklist: r.data.in_blacklist,
      reason: r.data.reason,
      until: r.data.until,
    } as BlacklistEntry)),

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
