import api from './axios';
import type { Festival, Ticket, Page } from '../types';

export interface AgeLimit {
  id: number;
  ageLimitValue: string;
}

export interface TicketTypeOption {
  id: number;
  name: string;
}

// ── Raw backend shapes ──────────────────────────────────────────────────────

interface RawFestivalResponse {
  festival_location_id: number;
  name: string;
  description?: string;
  date_start: string;
  time_start?: string;
  date_end: string;
  time_end?: string;
  platform_instance_id?: number;
  is_visible?: boolean;
  age_rating?: number;
}

interface RawTicketResponse {
  ticket_id: number;
  festival_name: string;
  ticket_type: string;
  quantity: number;
  final_price: string | number;
  expiry_date: string;
}

interface RawTicketTypeInstanceResponse {
  id: number;
  name: string;
  description?: string;
  price: number;
}

interface RawUserTicketsResponse {
  valid_tickets: RawTicketResponse[];
  expired_tickets: RawTicketResponse[];
}

// ── Mappers ─────────────────────────────────────────────────────────────────

function mapFestival(f: RawFestivalResponse): Festival {
  return {
    id: String(f.festival_location_id),
    name: f.name,
    description: f.description,
    cityId: '',
    cityName: '',
    startDate: f.date_start,
    endDate: f.date_end,
    ageRating: f.age_rating ?? 0,
    status: f.is_visible ? 'APPROVED' : 'PENDING',
    companies: [],
    ticketTypes: [],
  };
}

function mapTicket(t: RawTicketResponse, status: 'ACTIVE' | 'EXPIRED'): Ticket {
  return {
    id: String(t.ticket_id),
    festivalId: '',
    festivalName: t.festival_name,
    ticketTypeName: t.ticket_type,
    price: Number(t.final_price) || 0,
    startDate: '',
    endDate: t.expiry_date,
    number: `TKT-${t.ticket_id}`,
    status,
  };
}

// ── Payload types ────────────────────────────────────────────────────────────

interface FestivalFilters {
  name?: string;
  cityId?: string;
  dateFrom?: string;
  dateTo?: string;
  page?: number;
  size?: number;
  status?: string;
}

interface CreateFestivalPayload {
  name: string;
  description: string;
  platformId: string;
  startDate: string;
  startTime: string;
  endDate: string;
  endTime: string;
  ageLimitId: number;
  maxPeople: number;
  organizerTins?: string[];
  curatorEmails?: string[];
  ticketTypes?: Array<{ ticketTypeId: number; description: string; price: number }>;
}

interface CreateTicketTypePayload {
  ticketTypeId?: number;
  festivalLocationId?: string;
  description?: string;
  price?: number;
}

// ── API ──────────────────────────────────────────────────────────────────────

export const festivalApi = {
  // GET /festival/search → List<FestivalResponse>
  list: (filters?: FestivalFilters) =>
    api.get<RawFestivalResponse[]>('/festival/search', {
      params: {
        name: filters?.name || undefined,
        cityId: filters?.cityId ? Number(filters.cityId) : undefined,
        startDate: filters?.dateFrom || undefined,
        endDate: filters?.dateTo || undefined,
      },
    }).then(r => ({
      content: r.data.map(mapFestival),
      totalElements: r.data.length,
      totalPages: 1,
      number: 0,
      size: r.data.length,
      last: true,
    } as Page<Festival>)),

  // GET /festival/{id} + GET /ticket/instances/by-festival/{id} in parallel
  getById: (id: string) =>
    Promise.all([
      api.get<RawFestivalResponse>(`/festival/${id}`),
      api.get<RawTicketTypeInstanceResponse[]>(`/ticket/instances/by-festival/${id}`).catch(() => ({ data: [] as RawTicketTypeInstanceResponse[] })),
    ]).then(([festivalRes, ticketsRes]) => {
      const festival = mapFestival(festivalRes.data);
      festival.ticketTypes = (ticketsRes.data ?? []).map(t => ({
        id: String(t.id),
        name: t.name,
        description: t.description,
        price: Number(t.price) || 0,
        quantity: 0,
        remaining: 0,
      }));
      return festival;
    }),

  // GET /festival/age-limits — public, no auth required
  getAgeLimits: () =>
    api.get<AgeLimit[]>('/festival/age-limits').then(r => r.data),

  // GET /ticket/types — returns all global ticket types
  getTicketTypes: () =>
    api.get<TicketTypeOption[]>('/ticket/types').then(r => r.data),

  // POST /festival/apply — COMPANY creates festival application
  create: (data: CreateFestivalPayload) =>
    api.post<number>('/festival/apply', {
      festival_name: data.name,
      description: data.description,
      platform_instance_id: Number(data.platformId),
      date_start: data.startDate,
      time_start: data.startTime,
      date_end: data.endDate,
      time_end: data.endTime,
      age_limit_id: data.ageLimitId,
      max_people_number: data.maxPeople,
      organizer_tins: data.organizerTins?.length ? data.organizerTins : undefined,
      curator_emails: data.curatorEmails?.length ? data.curatorEmails : undefined,
      ticket_types: data.ticketTypes?.length ? data.ticketTypes.map(t => ({
        ticket_type_id: t.ticketTypeId,
        description: t.description,
        price: t.price,
      })) : undefined,
    }).then(r => ({ id: String(r.data) } as Festival)),

  update: (_id: string, _data: Partial<CreateFestivalPayload>) =>
    Promise.resolve({} as Festival),

  addCurator: (_festivalId: string, _email: string) => Promise.resolve(),
  removeCurator: (_festivalId: string, _userId: string) => Promise.resolve(),
  addCompany: (_festivalId: string, _tin: string) => Promise.resolve(),
  removeCompany: (_festivalId: string, _companyId: string) => Promise.resolve(),

  // POST /ticket/instances
  createTicketType: (data: CreateTicketTypePayload) =>
    api.post('/ticket/instances', {
      ticketTypeId: data.ticketTypeId,
      festivalLocationId: data.festivalLocationId ? Number(data.festivalLocationId) : undefined,
      description: data.description,
      price: data.price,
    }).then(r => r.data),

  // PUT /ticket/instances/{id}
  updateTicketType: (id: string, data: Partial<CreateTicketTypePayload>) =>
    api.put(`/ticket/instances/${id}`, {
      description: data.description,
      price: data.price,
    }).then(r => r.data),

  // DELETE /ticket/instances/{id}
  deleteTicketType: (id: string) =>
    api.delete(`/ticket/instances/${id}`).then(r => r.data),

  // POST /ticket/purchase
  buyTicket: (ticketTypeInstanceId: string) =>
    api.post<number>('/ticket/purchase', {
      ticket_type_instance_id: Number(ticketTypeInstanceId),
      quantity: 1,
    }).then(r => ({ id: String(r.data) } as Ticket)),

  // GET /ticket/my → UserTicketsResponse → Ticket[]
  myTickets: () =>
    api.get<RawUserTicketsResponse>('/ticket/my').then(r => {
      const valid = (r.data.valid_tickets ?? []).map(t => mapTicket(t, 'ACTIVE'));
      const expired = (r.data.expired_tickets ?? []).map(t => mapTicket(t, 'EXPIRED'));
      return [...valid, ...expired];
    }),

  // GET /festival/my — COMPANY's festivals
  myCompanyFestivals: () =>
    api.get<RawFestivalResponse[]>('/festival/my').then(r => r.data.map(mapFestival)),

  // GET /festival/curator/my — CURATOR's festivals
  myCuratorFestivals: () =>
    api.get<RawFestivalResponse[]>('/festival/curator/my').then(r => r.data.map(mapFestival)),

  // POST /qr/festivals/location/{locationId}/verify — CURATOR verifies ticket QR
  verifyTicketQr: (token: string, locationId = '0') =>
    api.post(`/qr/festivals/location/${locationId}/verify`, { token }).then(r => r.data),

  // POST /qr/festivals/ticket/{ticketId}/generate — USER generates ticket QR
  generateTicketQr: (ticketId: string) =>
    api.post(`/qr/festivals/ticket/${ticketId}/generate`).then(r => r.data),

  // POST /files/FESTIVAL_LOCATION/{festivalId}
  uploadPhoto: async (festivalId: string, file: File) => {
    const bytes = await file.arrayBuffer();
    await api.post(`/files/FESTIVAL_LOCATION/${festivalId}`, bytes, {
      headers: { 'Content-Type': 'application/octet-stream' },
    });
    return { url: `/api/files/FESTIVAL_LOCATION/${festivalId}` };
  },

  // DELETE /files/FESTIVAL_LOCATION/{festivalId}
  deletePhoto: (festivalId: string) =>
    api.delete(`/files/FESTIVAL_LOCATION/${festivalId}`).then(r => r.data),

  uploadCover: async (file: File) => {
    return { url: '' };
  },
};
