import api from './axios';
import type { City } from '../types';

export interface PlatformInstanceResponse {
  id: number;
  platform_name: string;
  city: { id: number; name: string };
  street: string;
  house: string;
}

export const locationApi = {
  getCities: (name?: string) =>
    api.get<{ content: Array<{ id: number; name: string }> }>(
      '/location/city/search',
      { params: { name: name ?? '', size: 10 } }
    ).then(r => r.data.content.map(c => ({ id: String(c.id), name: c.name }) as City)),

  searchPlatformInstances: (name = '', cityId?: string) =>
    api.get<{ content: PlatformInstanceResponse[] }>('/location/platform-instance/search', {
      params: {
        platformName: name,
        cityId: cityId ? Number(cityId) : undefined,
        size: 10,
      },
    }).then(r => r.data.content),

  createPlatformInstance: (data: { platformName: string; cityId: number; street: string; house: string }) =>
    api.post<PlatformInstanceResponse>('/location/platform-instance', {
      platform_name: data.platformName,
      city_id: data.cityId,
      street: data.street,
      house: data.house,
    }).then(r => r.data),
};
