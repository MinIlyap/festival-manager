import api from './axios';
import type { Company } from '../types';

interface UpdateCompanyPayload {
  fullName: string;
  brandName: string;
  website?: string;
}

interface RawCompanyResponse {
  id: number;
  tin: string;
  full_name: string;
  brand_name: string;
  email: string;
  website?: string;
  is_active: boolean;
}

function mapCompany(r: RawCompanyResponse): Company {
  return {
    id: String(r.id),
    fullName: r.full_name,
    brandName: r.brand_name,
    tin: r.tin,
    email: r.email,
    website: r.website,
    active: r.is_active,
  };
}

export const companyApi = {
  getMe: () =>
    api.get<RawCompanyResponse>('/company/me').then(r => ({
      ...mapCompany(r.data),
      logoUrl: `/api/files/COMPANY_LOGO/${r.data.id}`,
    })),
  updateMe: (data: UpdateCompanyPayload) =>
    api.put<RawCompanyResponse>('/company/me', {
      full_name: data.fullName,
      brand_name: data.brandName,
      website: data.website,
    }).then(r => mapCompany(r.data)),
  findByTin: (tin: string) => api.get<Company>(`/company/search?tin=${tin}`).then(r => r.data),

  getById: (id: string) =>
    api.get<RawCompanyResponse>(`/company/${id}`).then(r => mapCompany(r.data)),
  uploadLogo: async (companyId: string, file: File) => {
    const bytes = await file.arrayBuffer();
    await api.post(`/files/COMPANY_LOGO/${companyId}`, bytes, {
      headers: { 'Content-Type': 'application/octet-stream' },
    });
    return { url: `/api/files/COMPANY_LOGO/${companyId}` };
  },

  deleteLogo: (companyId: string) =>
    api.delete(`/files/COMPANY_LOGO/${companyId}`).then(r => r.data),

  getLogoUrl: (companyId: string) => `/api/files/COMPANY_LOGO/${companyId}`,
};
