import api from './axios';
import type { Notification, NotificationData, NotificationType, Page } from '../types';

interface RawNotificationData {
  company_id?: number;
  company_name?: string;
  company_email?: string;
  festival_location_id?: number;
  festival_name?: string;
  application_id?: number;
  comment?: string;
  event_id?: number;
  event_name?: string;
  command_id?: number;
  command_name?: string;
  related_user_id?: number;
}

interface RawNotification {
  notification_id: number;
  notification_type: string;
  is_read: boolean;
  created_at: string;
  data?: RawNotificationData;
}

function mapData(d: RawNotificationData): NotificationData {
  return {
    companyId: d.company_id,
    companyName: d.company_name,
    companyEmail: d.company_email,
    festivalLocationId: d.festival_location_id,
    festivalName: d.festival_name,
    applicationId: d.application_id,
    comment: d.comment,
    eventId: d.event_id,
    eventName: d.event_name,
    commandId: d.command_id,
    commandName: d.command_name,
    relatedUserId: d.related_user_id,
  };
}

function mapNotification(r: RawNotification): Notification {
  return {
    id: String(r.notification_id),
    type: r.notification_type as NotificationType,
    read: r.is_read,
    createdAt: r.created_at,
    data: r.data ? mapData(r.data) : undefined,
  };
}

interface RawPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  last: boolean;
}

export const notificationApi = {
  list: (params?: { page?: number; size?: number; isRead?: boolean }) =>
    api.get<RawPage<RawNotification>>('/notification', { params }).then(r => ({
      content: r.data.content.map(mapNotification),
      totalElements: r.data.totalElements,
      totalPages: r.data.totalPages,
      number: r.data.number,
      size: r.data.size,
      last: r.data.last,
    } as Page<Notification>)),

  markAllRead: () =>
    api.post('/notification/read-all').then(r => r.data),

  markRead: (id: string) =>
    api.post(`/notification/read/${id}`).then(r => r.data),

  unreadCount: () =>
    api.get<number>('/notification/unread-count').then(r => r.data),
};
