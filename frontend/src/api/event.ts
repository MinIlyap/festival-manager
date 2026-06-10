import api from './axios';
import type { FestivalEvent, Stand, EventRegistration, Reward, Page } from '../types';

interface RawFestivalStandResponse {
  id: number;
  festival_location_id: number;
  name: string;
  type: 'COMMERCIAL' | 'DEMONSTRATION';
  description?: string;
  company_id: number;
  organizer_company_id?: number;
  date_start: string;
}

interface RawFestivalEventResponse {
  festival_event_id: number;
  festival_location_id: number;
  event_name: string;
  type: string;
  description?: string;
  date_start: string;
  time_start?: string;
  approximate_duration?: number;
  is_command: boolean;
  max_number_people?: number;
  max_people_command?: number;
  organizer_company_id?: number;
  company_brand_name?: string;
  created_at: string;
}

interface RawUserFestivalEventsResponse {
  festival_location_id: number;
  events: RawFestivalEventResponse[];
}

function mapEvent(e: RawFestivalEventResponse): FestivalEvent {
  return {
    id: String(e.festival_event_id),
    festivalId: String(e.festival_location_id),
    festivalName: '',
    name: e.event_name,
    type: e.type as FestivalEvent['type'],
    description: e.description,
    format: e.is_command ? 'TEAM' : 'SOLO',
    maxParticipants: e.max_number_people,
    teamSize: e.max_people_command,
    startDate: e.date_start,
    startTime: e.time_start,
    approximateDuration: e.approximate_duration,
    endDate: e.date_start,
    organizerCompanyId: e.organizer_company_id,
    companyName: e.company_brand_name,
    rewards: [],
  };
}

function mapStand(s: RawFestivalStandResponse): Stand {
  return {
    id: String(s.id),
    festivalId: String(s.festival_location_id),
    festivalName: '',
    name: s.name,
    type: s.type,
    description: s.description,
    startDate: s.date_start,
    organizerCompanyId: s.organizer_company_id,
  };
}

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

function mapEventReward(r: any): EventReward {
  return {
    eventRewardId: r.event_reward_id,
    rewardId: r.reward_id,
    rewardName: r.reward_name,
    description: r.description,
    place: r.place,
    pointNumber: r.point_number,
  };
}

function mapStandProduct(p: any): StandProduct {
  return {
    productInstanceId: p.product_instance_id,
    productId: p.product_id,
    productName: p.product_name,
    description: p.description,
    quantity: p.quantity,
    price: p.price,
  };
}

export const eventApi = {
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

  getEventsByFestival: (festivalLocationId: string) =>
    api.get<RawFestivalEventResponse[]>(`/events/festival/${festivalLocationId}`)
      .then(r => r.data.map(mapEvent)),

  getEventById: (id: string) =>
    Promise.all([
      api.get<RawFestivalEventResponse>(`/events/${id}`),
      api.get<any[]>(`/events/rewards/by-event/${id}`).catch(() => ({ data: [] as any[] })),
    ]).then(([eventRes, rewardsRes]) => {
      const event = mapEvent(eventRes.data);
      event.rewards = (rewardsRes.data ?? []).map((r: any) => ({
        id: String(r.event_reward_id),
        name: r.reward_name,
        description: r.description,
        place: r.place ?? 0,
        photoUrl: `/api/files/EVENT_REWARD/${r.event_reward_id}`,
        eventId: id,
      }));
      return event;
    }),

  createEvent: (data: CreateEventPayload) =>
    api.post<number>('/events', {
      event_id: data.eventId,
      festival_location_id: Number(data.festivalId),
      type: data.type,
      description: data.description,
      date_start: data.startDate,
      time_start: data.startTime || '00:00',
      approximate_duration: data.approximateDuration,
      is_command: data.format === 'TEAM',
      max_number_people: data.maxParticipants ?? 0,
      max_people_command: data.teamSize,
      organizer_company_id: data.organizerCompanyId ?? null,
    }).then(r => ({ id: String(r.data) } as FestivalEvent)),

  updateEvent: (festivalEventId: string, data: UpdateEventPayload) =>
    api.put(`/events/${festivalEventId}`, {
      type: data.type,
      description: data.description,
      date_start: data.startDate,
      time_start: data.startTime || '00:00',
      approximate_duration: data.approximateDuration,
      is_command: data.format === 'TEAM',
      max_number_people: data.maxParticipants ?? 0,
      max_people_command: data.teamSize,
      organizer_company_id: data.organizerCompanyId ?? null,
    }).then(r => r.data),

  deleteEvent: (id: string) =>
    api.delete(`/events/${id}`).then(r => r.data),

  getStandsByFestival: (festivalLocationId: string) =>
    api.get<RawFestivalStandResponse[]>(`/events/stands/festival/${festivalLocationId}`)
      .then(r => r.data.map(mapStand)),

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
      content: r.data.content.map(mapStand),
      totalElements: r.data.totalElements,
      totalPages: r.data.totalPages,
      number: r.data.number,
      size: r.data.size,
      last: r.data.last,
    } as Page<Stand>)),

  createStand: (data: CreateStandPayload) =>
    api.post<number>('/events/stands', {
      stand_id: data.standId,
      festival_location_id: Number(data.festivalId),
      type: data.type,
      description: data.description,
      date_start: data.startDate,
      organizer_company_id: data.organizerCompanyId ?? null,
    }).then(r => ({ id: String(r.data) } as Stand)),

  updateStand: (festivalStandId: string, data: { type: 'COMMERCIAL' | 'DEMONSTRATION'; description?: string; startDate: string; organizerCompanyId?: number }) =>
    api.put(`/events/stands/${festivalStandId}`, {
      type: data.type,
      description: data.description,
      date_start: data.startDate,
      organizer_company_id: data.organizerCompanyId ?? null,
    }).then(r => r.data),

  deleteStand: (id: string) =>
    api.delete(`/events/stands/${id}`).then(r => r.data),

  addProduct: (standId: string, product: { productId: number; description?: string; price: number; quantity: number }) =>
    api.post(`/events/stands/${standId}/products`, {
      product_id: product.productId,
      description: product.description,
      quantity: product.quantity,
      price: product.price,
    }).then(r => r.data),

  getProductsByStand: (standId: string) =>
    api.get<any[]>(`/events/stands/${standId}/products`)
      .then(r => r.data.map(mapStandProduct)),

  deleteProduct: (standId: string, productInstanceId: number) =>
    api.delete(`/events/stands/${standId}/products/${productInstanceId}`).then(r => r.data),

  updateProduct: (standId: string, productInstanceId: number, data: { productId: number; description?: string; price: number; quantity: number }) =>
    api.put(`/events/stands/${standId}/products/${productInstanceId}`, {
      product_id: data.productId,
      description: data.description,
      quantity: data.quantity,
      price: data.price,
    }).then(r => r.data),

  createReward: (data: CreateRewardPayload) =>
    api.post<number>(`/events/rewards/${data.eventId}`, {
      reward_id: 0,
      description: data.description,
      place: data.place,
    }).then(r => ({ id: String(r.data) } as Reward)),

  distributeReward: (rewardId: string, userId: string) =>
    api.post(`/events/rewards/${rewardId}/distribute/users`, {
      user_ids: [Number(userId)],
    }).then(r => r.data),

  register: (eventId: string) =>
    api.post(`/events/registrations/${eventId}`).then(r => ({ id: eventId } as EventRegistration)),

  myRegistrations: () =>
    api.get<RawUserFestivalEventsResponse[]>('/events/my/attended').then(r => {
      const result: EventRegistration[] = [];
      (r.data ?? []).forEach(group => {
        (group.events ?? []).forEach(ev => {
          result.push({
            id: String(ev.festival_event_id),
            festivalId: String(group.festival_location_id),
            festivalName: '',
            eventId: String(ev.festival_event_id),
            eventName: ev.event_name,
            eventDate: ev.date_start,
            format: ev.is_command ? 'TEAM' : 'SOLO',
            status: 'ACTIVE',
          });
        });
      });
      return result;
    }),

  createTeam: (festivalEventId: string, teamName: string) =>
    api.post('/events/commands/create', null, {
      params: { festivalEventId: Number(festivalEventId), name: teamName },
    }).then(r => r.data),

  inviteToTeam: (commandId: string, targetUserId: number) =>
    api.post(`/events/commands/${commandId}/invite`, null, {
      params: { targetUserId },
    }).then(r => r.data),

  acceptInvite: (commandId: string) =>
    api.post(`/events/commands/${commandId}/join`).then(r => r.data),

  rejectInvite: (_commandId: string) => Promise.resolve(),

  leaveTeam: (commandId: string) =>
    api.post(`/events/commands/${commandId}/leave`).then(r => r.data),

  disbandTeam: (commandId: string) =>
    api.delete(`/events/commands/${commandId}`).then(r => r.data),

  getEventParticipants: (eventId: string) =>
    api.get<number[]>(`/events/${eventId}/info/participants`).then(r => r.data),

  getEventCommands: (eventId: string) =>
    api.get<any[]>(`/events/${eventId}/info/commands`).then(r =>
      r.data.map((c: any) => ({
        commandId: c.command_id,
        commandName: c.command_name,
        creatorId: c.creator_id,
        memberUserIds: c.member_user_ids,
      }))
    ),

  getEventParticipantsWithoutCommand: (eventId: string) =>
    api.get<number[]>(`/events/${eventId}/info/participants/without-command`).then(r => r.data),

  verifyEventQr: (token: string, festivalEventId = '0') =>
    api.post(`/qr/events/festival-event/${festivalEventId}/verify`, { token }).then(r => r.data),

  generateEventQr: (festivalEventId: string) =>
    api.post(`/qr/events/festival-event/${festivalEventId}/generate`).then(r => r.data),

  myCompanyEvents: () =>
    api.get<RawFestivalEventResponse[]>('/events/my/created').then(r => r.data.map(mapEvent)),

  myRewards: () =>
    api.get<any[]>('/events/my/rewards').then(r =>
      r.data.map((group: any) => ({
        festivalLocationId: group.festival_location_id,
        rewards: (group.rewards ?? []).map((rw: any) => ({
          eventRewardId: rw.event_reward_id,
          eventName: rw.event_name,
          rewardName: rw.reward_name,
          description: rw.description,
          place: rw.place,
          pointNumber: rw.point_number,
        })),
      }))
    ),

  searchUserByNickname: (nickname: string) =>
    api.get(`/user/users/search`, { params: { nickname } }).then(r => r.data),

  uploadEventPhoto: async (eventId: string, file: File) => {
    const bytes = await file.arrayBuffer();
    await api.post(`/files/FESTIVAL_EVENT/${eventId}`, bytes, {
      headers: { 'Content-Type': 'application/octet-stream' },
    });
    return `/api/files/FESTIVAL_EVENT/${eventId}`;
  },

  deleteEventPhoto: (eventId: string) =>
    api.delete(`/files/FESTIVAL_EVENT/${eventId}`).then(r => r.data),

  getEventPhotoUrl: (eventId: string) => `/api/files/FESTIVAL_EVENT/${eventId}`,

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

  getRewardsByEvent: (festivalEventId: string) =>
    api.get<any[]>(`/events/rewards/by-event/${festivalEventId}`)
      .then(r => r.data.map(mapEventReward)),

  addRewardToEvent: (festivalEventId: string, data: { rewardId: number; description?: string; place?: number; pointNumber?: number }) =>
    api.post<number>(`/events/rewards/${festivalEventId}`, {
      reward_id: data.rewardId,
      description: data.description,
      place: data.place,
      point_number: data.pointNumber,
    }).then(r => r.data),

  deleteReward: (eventRewardId: string) =>
    api.delete(`/events/rewards/${eventRewardId}`).then(r => r.data),

  distributeRewardToUsers: (eventRewardId: string, userIds: number[]) =>
    api.post(`/events/rewards/${eventRewardId}/distribute/users`, { user_ids: userIds }).then(r => r.data),

  distributeRewardToCommand: (eventRewardId: string, commandId: number) =>
    api.post(`/events/rewards/${eventRewardId}/distribute/commands/${commandId}`).then(r => r.data),

  uploadStandPhoto: async (standId: string, file: File) => {
    const bytes = await file.arrayBuffer();
    await api.post(`/files/FESTIVAL_STAND/${standId}`, bytes, {
      headers: { 'Content-Type': 'application/octet-stream' },
    });
    return `/api/files/FESTIVAL_STAND/${standId}`;
  },

  deleteStandPhoto: (standId: string) =>
    api.delete(`/files/FESTIVAL_STAND/${standId}`).then(r => r.data),

  uploadProductPhoto: async (productInstanceId: number, file: File) => {
    const bytes = await file.arrayBuffer();
    await api.post(`/files/PRODUCT_INSTANCE/${productInstanceId}`, bytes, {
      headers: { 'Content-Type': 'application/octet-stream' },
    });
    return `/api/files/PRODUCT_INSTANCE/${productInstanceId}`;
  },

  deleteProductPhoto: (productInstanceId: number) =>
    api.delete(`/files/PRODUCT_INSTANCE/${productInstanceId}`).then(r => r.data),

  uploadRewardPhoto: async (rewardId: string, file: File) => {
    const bytes = await file.arrayBuffer();
    await api.post(`/files/EVENT_REWARD/${rewardId}`, bytes, {
      headers: { 'Content-Type': 'application/octet-stream' },
    });
    return `/api/files/EVENT_REWARD/${rewardId}`;
  },

  deleteRewardPhoto: (rewardId: string) =>
    api.delete(`/files/EVENT_REWARD/${rewardId}`).then(r => r.data),
};
