import api from './axios';
import type { FestivalEvent, Stand, EventRegistration, Reward, Page } from '../types';

// ── Raw backend shapes ──────────────────────────────────────────────────────

interface RawFestivalStandResponse {
  id: number;
  festivalLocationId: number;
  name: string;
  type: 'COMMERCIAL' | 'DEMONSTRATION';
  description?: string;
  companyId: number;
  organizerCompanyId?: number;
  dateStart: string;
}

interface RawFestivalEventResponse {
  festivalEventId: number;
  festivalLocationId: number;
  eventName: string;
  type: string;
  description?: string;
  dateStart: string;
  timeStart?: string;
  approximateDuration?: number;
  isCommand: boolean;
  maxNumberPeople?: number;
  maxPeopleCommand?: number;
  organizerCompanyId?: number;
  companyBrandName?: string;
  createdAt: string;
}

interface RawUserFestivalEventsResponse {
  festivalLocationId: number;
  events: RawFestivalEventResponse[];
}

// ── Mappers ─────────────────────────────────────────────────────────────────

function mapEvent(e: RawFestivalEventResponse): FestivalEvent {
  return {
    id: String(e.festivalEventId),
    festivalId: String(e.festivalLocationId),
    festivalName: '',
    name: e.eventName,
    type: e.type as FestivalEvent['type'],
    description: e.description,
    format: e.isCommand ? 'TEAM' : 'SOLO',
    maxParticipants: e.maxNumberPeople,
    teamSize: e.maxPeopleCommand,
    startDate: e.dateStart,
    startTime: e.timeStart,
    approximateDuration: e.approximateDuration,
    endDate: e.dateStart,
    organizerCompanyId: e.organizerCompanyId,
    companyName: e.companyBrandName,
    rewards: [],
  };
}

// ── Payload types ────────────────────────────────────────────────────────────

interface EventFilters {
  name?: string;
  cityId?: string;
  dateFrom?: string;
  dateTo?: string;
  type?: string;
  companyId?: string;
  page?: number;
  size?: number;
}

interface CreateEventPayload {
  festivalId: string;
  eventId: number;
  type: string;
  description?: string;
  format: 'SOLO' | 'TEAM';
  maxParticipants?: number;
  teamSize?: number;
  startDate: string;
  startTime: string;
  approximateDuration?: number;
  organizerCompanyId?: number;
}

interface UpdateEventPayload {
  type: string;
  description?: string;
  format: 'SOLO' | 'TEAM';
  maxParticipants?: number;
  teamSize?: number;
  startDate: string;
  startTime: string;
  approximateDuration?: number;
  organizerCompanyId?: number;
}

export interface EventReward {
  eventRewardId: number;
  rewardId: number;
  rewardName: string;
  description?: string;
  place?: number;
  pointNumber?: number;
}

export interface StandProduct {
  productInstanceId: number;
  productId: number;
  productName: string;
  description?: string;
  quantity: number;
  price: number;
}

interface CreateStandPayload {
  festivalId: string;
  standId: number;
  type: 'COMMERCIAL' | 'DEMONSTRATION';
  description?: string;
  startDate: string;
  organizerCompanyId?: number;
}

interface CreateRewardPayload {
  eventId: string;
  name: string;
  description?: string;
  place: number;
  photoUrl?: string;
}

// ── API ──────────────────────────────────────────────────────────────────────
// Proxy: /api/events/* → eventservice /events/*
//        /api/qr/events/* → eventservice /qr/events/*

export const eventApi = {
  // GET /events/search → Page<FestivalEventResponse>
  listEvents: (filters?: EventFilters) =>
    api.get<{ content: RawFestivalEventResponse[]; totalElements: number; totalPages: number; number: number; size: number; last: boolean }>(
      '/events/search',
      {
        params: {
          name: filters?.name || undefined,
          cityId: filters?.cityId ? Number(filters.cityId) : undefined,
          startDate: filters?.dateFrom || undefined,
          endDate: filters?.dateTo || undefined,
          type: filters?.type || undefined,
          page: filters?.page ?? 0,
          size: filters?.size ?? 12,
        },
      }
    ).then(r => ({
      content: r.data.content.map(mapEvent),
      totalElements: r.data.totalElements,
      totalPages: r.data.totalPages,
      number: r.data.number,
      size: r.data.size,
      last: r.data.last,
    } as Page<FestivalEvent>)),

  // GET /events/festival/{festivalLocationId} — events by festival location
  getEventsByFestival: (festivalLocationId: string) =>
    api.get<RawFestivalEventResponse[]>(`/events/festival/${festivalLocationId}`)
      .then(r => r.data.map(mapEvent)),

  getEventById: (id: string) =>
    Promise.all([
      api.get<RawFestivalEventResponse>(`/events/${id}`),
      api.get<EventReward[]>(`/events/rewards/by-event/${id}`).catch(() => ({ data: [] as EventReward[] })),
    ]).then(([eventRes, rewardsRes]) => {
      const event = mapEvent(eventRes.data);
      event.rewards = (rewardsRes.data ?? []).map(r => ({
        id: String(r.eventRewardId),
        name: r.rewardName,
        description: r.description,
        place: r.place ?? 0,
        photoUrl: `/api/files/EVENT_REWARD/${r.eventRewardId}`,
        eventId: id,
      }));
      return event;
    }),

  // POST /events — COMPANY creates festival event
  createEvent: (data: CreateEventPayload) =>
    api.post<number>('/events', {
      eventId: data.eventId,
      festivalLocationId: Number(data.festivalId),
      type: data.type,
      description: data.description,
      dateStart: data.startDate,
      timeStart: data.startTime || '00:00',
      approximateDuration: data.approximateDuration,
      isCommand: data.format === 'TEAM',
      maxNumberPeople: data.maxParticipants ?? 0,
      maxPeopleCommand: data.teamSize,
      organizerCompanyId: data.organizerCompanyId ?? null,
    }).then(r => ({ id: String(r.data) } as FestivalEvent)),

  updateEvent: (festivalEventId: string, data: UpdateEventPayload) =>
    api.put(`/events/${festivalEventId}`, {
      type: data.type,
      description: data.description,
      dateStart: data.startDate,
      timeStart: data.startTime || '00:00',
      approximateDuration: data.approximateDuration,
      isCommand: data.format === 'TEAM',
      maxNumberPeople: data.maxParticipants ?? 0,
      maxPeopleCommand: data.teamSize,
      organizerCompanyId: data.organizerCompanyId ?? null,
    }).then(r => r.data),

  // DELETE /events/{festivalEventId}
  deleteEvent: (id: string) =>
    api.delete(`/events/${id}`).then(r => r.data),

  // GET /events/stands/festival/{festivalLocationId}
  getStandsByFestival: (festivalLocationId: string) =>
    api.get<RawFestivalStandResponse[]>(`/events/stands/festival/${festivalLocationId}`)
      .then(r => r.data.map(s => ({
        id: String(s.id),
        festivalId: String(s.festivalLocationId),
        festivalName: '',
        name: s.name,
        type: s.type,
        description: s.description,
        startDate: s.dateStart,
        organizerCompanyId: s.organizerCompanyId,
      } as Stand))),

  // GET /events/stands/search
  listStands: (filters?: EventFilters) =>
    api.get<{ content: RawFestivalStandResponse[]; totalElements: number; totalPages: number; number: number; size: number; last: boolean }>(
      '/events/stands/search',
      {
        params: {
          name: filters?.name || undefined,
          cityId: filters?.cityId ? Number(filters.cityId) : undefined,
          dateStart: filters?.dateFrom || undefined,
          type: filters?.type || undefined,
          page: filters?.page ?? 0,
          size: filters?.size ?? 12,
        },
      }
    ).then(r => ({
      content: r.data.content.map(s => ({
        id: String(s.id),
        festivalId: String(s.festivalLocationId),
        festivalName: '',
        name: s.name,
        type: s.type,
        description: s.description,
        startDate: s.dateStart,
        organizerCompanyId: s.organizerCompanyId,
      } as Stand)),
      totalElements: r.data.totalElements,
      totalPages: r.data.totalPages,
      number: r.data.number,
      size: r.data.size,
      last: r.data.last,
    } as Page<Stand>)),

  // POST /events/stands
  createStand: (data: CreateStandPayload) =>
    api.post<number>('/events/stands', {
      standId: data.standId,
      festivalLocationId: Number(data.festivalId),
      type: data.type,
      description: data.description,
      dateStart: data.startDate,
      organizerCompanyId: data.organizerCompanyId ?? null,
    }).then(r => ({ id: String(r.data) } as Stand)),

  updateStand: (festivalStandId: string, data: { type: 'COMMERCIAL' | 'DEMONSTRATION'; description?: string; startDate: string; organizerCompanyId?: number }) =>
    api.put(`/events/stands/${festivalStandId}`, {
      type: data.type,
      description: data.description,
      dateStart: data.startDate,
      organizerCompanyId: data.organizerCompanyId ?? null,
    }).then(r => r.data),

  // DELETE /events/stands/{festivalStandId}
  deleteStand: (id: string) =>
    api.delete(`/events/stands/${id}`).then(r => r.data),

  // POST /events/stands/{festivalStandId}/products
  addProduct: (standId: string, product: { productId: number; description?: string; price: number; quantity: number }) =>
    api.post(`/events/stands/${standId}/products`, {
      productId: product.productId,
      description: product.description,
      quantity: product.quantity,
      price: product.price,
    }).then(r => r.data),

  // GET /events/stands/{festivalStandId}/products
  getProductsByStand: (standId: string) =>
    api.get<StandProduct[]>(`/events/stands/${standId}/products`).then(r => r.data),

  // DELETE /events/stands/{festivalStandId}/products/{productInstanceId}
  deleteProduct: (standId: string, productInstanceId: number) =>
    api.delete(`/events/stands/${standId}/products/${productInstanceId}`).then(r => r.data),

  // PUT /events/stands/{festivalStandId}/products/{productInstanceId}
  updateProduct: (standId: string, productInstanceId: number, data: { productId: number; description?: string; price: number; quantity: number }) =>
    api.put(`/events/stands/${standId}/products/${productInstanceId}`, {
      productId: data.productId,
      description: data.description,
      quantity: data.quantity,
      price: data.price,
    }).then(r => r.data),

  // POST /events/rewards/{festivalEventId}
  createReward: (data: CreateRewardPayload) =>
    api.post<number>(`/events/rewards/${data.eventId}`, {
      rewardId: 0,
      description: data.description,
      place: data.place,
    }).then(r => ({ id: String(r.data) } as Reward)),

  // POST /events/rewards/{eventRewardId}/distribute/users
  distributeReward: (rewardId: string, userId: string) =>
    api.post(`/events/rewards/${rewardId}/distribute/users`, {
      userIds: [Number(userId)],
    }).then(r => r.data),

  // POST /events/registrations/{festivalEventId}
  register: (eventId: string) =>
    api.post(`/events/registrations/${eventId}`).then(r => ({ id: eventId } as EventRegistration)),

  // GET /events/my/attended → UserFestivalEventsResponse[] → flat EventRegistration[]
  myRegistrations: () =>
    api.get<RawUserFestivalEventsResponse[]>('/events/my/attended').then(r => {
      const result: EventRegistration[] = [];
      (r.data ?? []).forEach(group => {
        (group.events ?? []).forEach(ev => {
          result.push({
            id: String(ev.festivalEventId),
            festivalId: String(group.festivalLocationId),
            festivalName: '',
            eventId: String(ev.festivalEventId),
            eventName: ev.eventName,
            eventDate: ev.dateStart,
            format: ev.isCommand ? 'TEAM' : 'SOLO',
            status: 'ACTIVE',
          });
        });
      });
      return result;
    }),

  // POST /events/commands/create?festivalEventId=xxx&name=xxx
  createTeam: (festivalEventId: string, teamName: string) =>
    api.post('/events/commands/create', null, {
      params: { festivalEventId: Number(festivalEventId), name: teamName },
    }).then(r => r.data),

  // POST /events/commands/{commandId}/invite?targetUserId=xxx
  inviteToTeam: (commandId: string, targetUserId: string) =>
    api.post(`/events/commands/${commandId}/invite`, null, {
      params: { targetUserId: Number(targetUserId) },
    }).then(r => r.data),

  acceptInvite: (_inviteId: string) => Promise.resolve(),
  rejectInvite: (_inviteId: string) => Promise.resolve(),

  // POST /qr/events/festival-event/{festivalEventId}/verify — COMPANY verifies event QR
  verifyEventQr: (token: string, festivalEventId = '0') =>
    api.post(`/qr/events/festival-event/${festivalEventId}/verify`, { token }).then(r => r.data),

  // POST /qr/events/festival-event/{festivalEventId}/generate — USER generates event QR
  generateEventQr: (festivalEventId: string) =>
    api.post(`/qr/events/festival-event/${festivalEventId}/generate`).then(r => r.data),

  // GET /events/my/created — COMPANY's own events
  myCompanyEvents: () =>
    api.get<RawFestivalEventResponse[]>('/events/my/created').then(r => r.data.map(mapEvent)),

  // GET /user/users/search?nickname=xxx — used for invite flow
  searchUserByNickname: (nickname: string) =>
    api.get(`/user/users/search`, { params: { nickname } }).then(r => r.data),

  // ── Photo management ─────────────────────────────────────────────────────────
  // POST /files/FESTIVAL_EVENT/{eventId}
  uploadEventPhoto: async (eventId: string, file: File) => {
    const bytes = await file.arrayBuffer();
    await api.post(`/files/FESTIVAL_EVENT/${eventId}`, bytes, {
      headers: { 'Content-Type': 'application/octet-stream' },
    });
    return `/api/files/FESTIVAL_EVENT/${eventId}`;
  },

  // DELETE /files/FESTIVAL_EVENT/{eventId}
  deleteEventPhoto: (eventId: string) =>
    api.delete(`/files/FESTIVAL_EVENT/${eventId}`).then(r => r.data),

  // GET URL for event photo
  getEventPhotoUrl: (eventId: string) => `/api/files/FESTIVAL_EVENT/${eventId}`,

  // ── Dictionary management ────────────────────────────────────────────────────
  getDictionaryEvents: () =>
    api.get<Array<{ id: number; name: string }>>('/events/dictionaries/events').then(r => r.data),

  createDictionaryEvent: (name: string) =>
    api.post<number>('/events/dictionaries/events', { name }).then(r => r.data),

  getDictionaryStands: () =>
    api.get<Array<{ id: number; name: string }>>('/events/dictionaries/stands').then(r => r.data),

  createDictionaryStand: (name: string) =>
    api.post<number>('/events/dictionaries/stands', { name }).then(r => r.data),

  getDictionaryRewards: () =>
    api.get<Array<{ id: number; name: string }>>('/events/dictionaries/rewards').then(r => r.data),

  createDictionaryReward: (name: string) =>
    api.post<number>('/events/dictionaries/rewards', { name }).then(r => r.data),

  getDictionaryProducts: () =>
    api.get<Array<{ id: number; name: string }>>('/events/dictionaries/products').then(r => r.data),

  createDictionaryProduct: (name: string) =>
    api.post<number>('/events/dictionaries/products', { name }).then(r => r.data),

  // ── Event rewards ────────────────────────────────────────────────────────────
  getRewardsByEvent: (festivalEventId: string) =>
    api.get<EventReward[]>(`/events/rewards/by-event/${festivalEventId}`).then(r => r.data),

  addRewardToEvent: (festivalEventId: string, data: { rewardId: number; description?: string; place?: number; pointNumber?: number }) =>
    api.post<number>(`/events/rewards/${festivalEventId}`, data).then(r => r.data),

  deleteReward: (eventRewardId: string) =>
    api.delete(`/events/rewards/${eventRewardId}`).then(r => r.data),

  distributeRewardToUsers: (eventRewardId: string, userIds: number[]) =>
    api.post(`/events/rewards/${eventRewardId}/distribute/users`, { userIds }).then(r => r.data),

  // POST /files/FESTIVAL_STAND/{standId}
  uploadStandPhoto: async (standId: string, file: File) => {
    const bytes = await file.arrayBuffer();
    await api.post(`/files/FESTIVAL_STAND/${standId}`, bytes, {
      headers: { 'Content-Type': 'application/octet-stream' },
    });
    return `/api/files/FESTIVAL_STAND/${standId}`;
  },

  // DELETE /files/FESTIVAL_STAND/{standId}
  deleteStandPhoto: (standId: string) =>
    api.delete(`/files/FESTIVAL_STAND/${standId}`).then(r => r.data),

  // POST /files/PRODUCT_INSTANCE/{productInstanceId}
  uploadProductPhoto: async (productInstanceId: number, file: File) => {
    const bytes = await file.arrayBuffer();
    await api.post(`/files/PRODUCT_INSTANCE/${productInstanceId}`, bytes, {
      headers: { 'Content-Type': 'application/octet-stream' },
    });
    return `/api/files/PRODUCT_INSTANCE/${productInstanceId}`;
  },

  // DELETE /files/PRODUCT_INSTANCE/{productInstanceId}
  deleteProductPhoto: (productInstanceId: number) =>
    api.delete(`/files/PRODUCT_INSTANCE/${productInstanceId}`).then(r => r.data),

  // POST /files/EVENT_REWARD/{rewardId}
  uploadRewardPhoto: async (rewardId: string, file: File) => {
    const bytes = await file.arrayBuffer();
    await api.post(`/files/EVENT_REWARD/${rewardId}`, bytes, {
      headers: { 'Content-Type': 'application/octet-stream' },
    });
    return `/api/files/EVENT_REWARD/${rewardId}`;
  },

  // DELETE /files/EVENT_REWARD/{rewardId}
  deleteRewardPhoto: (rewardId: string) =>
    api.delete(`/files/EVENT_REWARD/${rewardId}`).then(r => r.data),
};
