import api from './axios';
import type { City, Platform } from '../types';

export const locationApi = {
  getCities: (name?: string) =>
    api.get<{ content: Array<{ id: number; name: string }> }>(
      '/location/city/search',
      { params: { name: name ?? '', size: 10 } }
    ).then(r => r.data.content.map(c => ({ id: String(c.id), name: c.name }) as City)),

  getPlatforms: (cityId?: string) =>
    api.get<Platform[]>('/location/platforms', { params: { cityId } }).then(r => r.data),
};
