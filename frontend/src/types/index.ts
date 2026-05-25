export type UserRole = 'USER' | 'CURATOR' | 'COMPANY' | 'ADMIN';

export interface User {
  id: string;
  nickname: string;
  birthDate?: string;
  cityId?: string;
  phoneNumber?: string;
  firstName?: string;
  lastName?: string;
  middleName?: string;
  passportSeries?: string;
  passportNumber?: string;
  // populated from other sources (auth/file services)
  email?: string;
  avatarUrl?: string;
  role?: UserRole;
  cityName?: string;
}

export interface Company {
  id: string;
  fullName: string;
  brandName?: string;
  tin: string;
  email: string;
  cityId?: string;
  cityName?: string;
  description?: string;
  website?: string;
  logoUrl?: string;
  active: boolean;
}

export interface City {
  id: string;
  name: string;
}

export interface Platform {
  id: string;
  name: string;
  address: string;
  cityId: string;
}

export interface TicketType {
  id: string;
  name: string;
  price: number;
  description?: string;
  quantity: number;
  remaining: number;
}

export interface Festival {
  id: string;
  name: string;
  description?: string;
  cityId: string;
  cityName: string;
  platformId?: string;
  platformName?: string;
  startDate: string;
  endDate: string;
  ageRating: number;
  coverUrl?: string;
  status: 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED';
  companies: Company[];
  ticketTypes: TicketType[];
}

export interface Reward {
  id: string;
  name: string;
  description?: string;
  place: number;
  photoUrl?: string;
  eventId: string;
}

export interface FestivalEvent {
  id: string;
  festivalId: string;
  festivalName: string;
  name: string;
  type: 'TOURNAMENT' | 'QUEST' | 'MASTERCLASS' | 'DEMO';
  description?: string;
  format: 'SOLO' | 'TEAM';
  maxParticipants?: number;
  teamSize?: number;
  startDate: string;
  endDate: string;
  photoUrl?: string;
  companyName?: string;
  rewards: Reward[];
}

export interface Stand {
  id: string;
  festivalId: string;
  festivalName: string;
  eventId?: string;
  eventName?: string;
  name: string;
  type: 'TRADE' | 'DEMO';
  description?: string;
  photoUrl?: string;
  companyName?: string;
  products?: StandProduct[];
}

export interface StandProduct {
  id: string;
  name: string;
  description?: string;
  price: number;
  quantity: number;
  photoUrl?: string;
}

export interface Ticket {
  id: string;
  festivalId: string;
  festivalName: string;
  ticketTypeName: string;
  price: number;
  startDate: string;
  endDate: string;
  number: string;
  status: 'ACTIVE' | 'EXPIRED' | 'USED';
  qrToken?: string;
}

export interface EventRegistration {
  id: string;
  festivalId: string;
  festivalName: string;
  eventId: string;
  eventName: string;
  eventDate: string;
  format: 'SOLO' | 'TEAM';
  teamName?: string;
  status: 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
  qrToken?: string;
}

export interface ApplicationData {
  id: string;
  comment?: string;
  companyId?: string;
  festivalLocationId?: string;
}

export interface Application {
  id: string;
  applicationType: 'COMPANY_APPLICATION' | 'FESTIVAL_APPLICATION';
  applicationStatus: 'PENDING' | 'APPROVED' | 'REJECTED';
  senderId?: string;
  agentId?: string;
  createdAt: string;
  updatedAt: string;
  data?: ApplicationData;
}

export type NotificationType =
  | 'INVITING_TEAM'
  | 'APPROVING_COMPANY'
  | 'REJECTING_COMPANY'
  | 'APPROVING_FESTIVAL'
  | 'REJECTING_FESTIVAL'
  | 'RESUBMITTING_APPLICATION';

export interface NotificationData {
  companyId?: number;
  companyName?: string;
  companyEmail?: string;
  festivalLocationId?: number;
  festivalName?: string;
  applicationId?: number;
  comment?: string;
  eventId?: number;
  eventName?: string;
  commandId?: number;
  commandName?: string;
  relatedUserId?: number;
}

export interface Notification {
  id: string;
  type: NotificationType;
  read: boolean;
  createdAt: string;
  data?: NotificationData;
}

export interface BlacklistEntry {
  inBlacklist: boolean;
  reason?: string;
  until?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  last: boolean;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken?: string;
}
