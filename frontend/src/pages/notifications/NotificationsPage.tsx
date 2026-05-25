import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { Bell, CheckCircle, XCircle, Users, Check, AlertCircle } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import EmptyState from '../../components/ui/EmptyState';
import { notificationApi } from '../../api/notification';
import { useNotificationStore } from '../../store/notificationStore';
import { eventApi } from '../../api/event';
import type { Notification, NotificationType } from '../../types';
import styles from './NotificationsPage.module.css';

const iconMap: Record<NotificationType, React.ElementType> = {
  INVITING_TEAM: Users,
  APPROVING_COMPANY: CheckCircle,
  REJECTING_COMPANY: XCircle,
  APPROVING_FESTIVAL: CheckCircle,
  REJECTING_FESTIVAL: XCircle,
  RESUBMITTING_APPLICATION: AlertCircle,
};

const titleMap: Record<NotificationType, string> = {
  INVITING_TEAM: 'Приглашение в команду',
  APPROVING_COMPANY: 'Компания зарегистрирована',
  REJECTING_COMPANY: 'Регистрация компании отклонена',
  APPROVING_FESTIVAL: 'Фестиваль одобрен',
  REJECTING_FESTIVAL: 'Фестиваль отклонён',
  RESUBMITTING_APPLICATION: 'Повторная заявка на рассмотрение',
};

function renderMessage(n: Notification): React.ReactNode {
  const d = n.data;
  switch (n.type) {
    case 'APPROVING_FESTIVAL':
      return (
        <>
          Ваша заявка на создание фестиваля{' '}
          {d?.festivalLocationId ? (
            <Link to={`/festival/${d.festivalLocationId}`} className={styles.link}>
              «{d.festivalName || `#${d.festivalLocationId}`}»
            </Link>
          ) : null}{' '}
          была одобрена
        </>
      );

    case 'REJECTING_FESTIVAL':
      return (
        <>
          Ваша заявка на создание фестиваля{' '}
          {d?.festivalLocationId ? (
            <Link to={`/festival/${d.festivalLocationId}`} className={styles.link}>
              «{d.festivalName || `#${d.festivalLocationId}`}»
            </Link>
          ) : null}{' '}
          была отклонена{d?.comment ? `. Причина: ${d.comment}` : ''}
        </>
      );

    case 'APPROVING_COMPANY':
      return (
        <>
          Ваша компания успешно зарегистрирована в системе
          {d?.companyEmail ? `. Доступы отправлены на ${d.companyEmail}` : ''}
        </>
      );

    case 'REJECTING_COMPANY':
      return (
        <>
          Заявка на регистрацию компании отклонена
          {d?.comment ? `. Причина: ${d.comment}` : ''}
        </>
      );

    case 'INVITING_TEAM':
      return (
        <>
          Вас пригласили в команду «{d?.commandName ?? '—'}» на мероприятие{' '}
          {d?.eventId ? (
            <Link to={`/events/${d.eventId}`} className={styles.link}>
              «{d.eventName ?? `#${d.eventId}`}»
            </Link>
          ) : (
            `«${d?.eventName ?? '—'}»`
          )}
        </>
      );

    case 'RESUBMITTING_APPLICATION':
      return (
        <>
          Заявка
          {d?.applicationId ? (
            <>
              {' '}
              <Link to={`/admin/applications/${d.applicationId}`} className={styles.link}>
                №{d.applicationId}
              </Link>
            </>
          ) : null}
          , которую вы ранее рассматривали, была повторно подана на рассмотрение
        </>
      );

    default:
      return 'Новое уведомление';
  }
}

export default function NotificationsPage() {
  const qc = useQueryClient();
  const reset = useNotificationStore(s => s.reset);

  const { data, isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => notificationApi.list({ page: 0, size: 50 }),
  });

  const markAllMutation = useMutation({
    mutationFn: notificationApi.markAllRead,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['notifications'] });
      reset();
    },
  });

  const markReadMutation = useMutation({
    mutationFn: (id: string) => notificationApi.markRead(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['notifications'] }),
  });

  const acceptMutation = useMutation({
    mutationFn: (inviteId: string) => eventApi.acceptInvite(inviteId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['notifications'] }),
  });

  const rejectMutation = useMutation({
    mutationFn: (inviteId: string) => eventApi.rejectInvite(inviteId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['notifications'] }),
  });

  const notifications = data?.content ?? [];
  const unread = notifications.filter(n => !n.read).length;

  if (isLoading) return <Layout><Spinner centered /></Layout>;

  return (
    <Layout>
      <div className={styles.header}>
        <h1 className={styles.title}>
          Уведомления {unread > 0 && <span className={styles.unreadCount}>{unread}</span>}
        </h1>
        {unread > 0 && (
          <Button variant="ghost" size="sm" onClick={() => markAllMutation.mutate()} loading={markAllMutation.isPending}>
            <Check size={14} /> Отметить все как прочитанные
          </Button>
        )}
      </div>

      {!notifications.length ? (
        <EmptyState icon={Bell} message="Нет уведомлений" description="Здесь будут появляться важные события" />
      ) : (
        <div className={styles.list}>
          {notifications.map(n => {
            const Icon = iconMap[n.type] ?? Bell;
            const typeClass = n.type.toLowerCase().replace(/_/g, '-');
            return (
              <div
                key={n.id}
                className={[styles.item, !n.read ? styles.unread : ''].join(' ')}
                onClick={() => { if (!n.read) markReadMutation.mutate(n.id); }}
                style={{ cursor: !n.read ? 'pointer' : 'default' }}
              >
                <div className={[styles.iconWrap, styles[typeClass]].join(' ')}>
                  <Icon size={18} />
                </div>
                <div className={styles.itemContent}>
                  <p className={styles.itemTitle}>{titleMap[n.type] ?? 'Уведомление'}</p>
                  <p className={styles.itemMsg}>{renderMessage(n)}</p>
                  <p className={styles.itemTime}>{new Date(n.createdAt).toLocaleString('ru-RU')}</p>
                  {n.type === 'INVITING_TEAM' && n.data?.commandId && (
                    <div className={styles.itemActions}>
                      <Button size="sm" onClick={e => { e.stopPropagation(); acceptMutation.mutate(String(n.data!.commandId)); }}>
                        Принять
                      </Button>
                      <Button size="sm" variant="danger" onClick={e => { e.stopPropagation(); rejectMutation.mutate(String(n.data!.commandId)); }}>
                        Отклонить
                      </Button>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </Layout>
  );
}
