import api from './axios';
import type { Application, Page } from '../types';

export const applicationApi = {
  submitFestivalApplication: (festivalId: string) =>
    api.post<Application>('/application/festival-applications', { festivalId }).then(r => r.data),

  myApplications: () =>
    api.get<Page<Application>>('/application/my', { params: { size: 100 } }).then(r => r.data.content),

  adminList: (params?: { type?: string; status?: string; page?: number; size?: number }) =>
    api.get<Page<Application>>('/application/admin/all', { params }).then(r => r.data),

  getById: (id: string) =>
    api.get<Application>(`/application/admin/${id}`).then(r => r.data),

  approve: (id: string) =>
    api.post(`/application/admin/${id}/review`, { status: 'APPROVED' }).then(r => r.data),

  reject: (id: string, reason: string) =>
    api.post(`/application/admin/${id}/review`, { status: 'REJECTED', comment: reason }).then(r => r.data),

  adminHistory: (params?: { page?: number; size?: number }) =>
    api.get<Page<Application>>('/application/admin/my-history', { params })
      .then(r => r.data.content),
};
