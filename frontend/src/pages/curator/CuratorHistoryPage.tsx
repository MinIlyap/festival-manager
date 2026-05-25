import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { CalendarDays, Ticket, Award } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Tabs from '../../components/ui/Tabs';
import EmptyState from '../../components/ui/EmptyState';
import Spinner from '../../components/ui/Spinner';
import { festivalApi } from '../../api/festival';
import { eventApi } from '../../api/event';
import styles from '../history/HistoryPage.module.css';

export default function CuratorHistoryPage() {
  const [tab, setTab] = useState('festivals');

  const { data: tickets, isLoading: ticketsLoading } = useQuery({
    queryKey: ['my-tickets'],
    queryFn: festivalApi.myTickets,
    enabled: tab === 'festivals',
  });

  const { data: registrations, isLoading: regsLoading } = useQuery({
    queryKey: ['my-registrations'],
    queryFn: eventApi.myRegistrations,
    enabled: tab === 'events',
  });

  const isLoading = tab === 'festivals' ? ticketsLoading : regsLoading;

  const pastTickets = (tickets ?? []).filter(t => t.status !== 'ACTIVE');
  const pastRegs = (registrations ?? []).filter(r => r.status === 'COMPLETED');

  return (
    <Layout>
      <h1 className={styles.title}>История</h1>
      <Tabs
        tabs={[
          { key: 'festivals', label: 'Фестивали' },
          { key: 'events', label: 'Мероприятия' },
        ]}
        activeKey={tab}
        onChange={setTab}
      />
      <div style={{ marginTop: 'var(--space-lg)' }}>
        {isLoading ? (
          <Spinner centered />
        ) : tab === 'festivals' ? (
          !pastTickets.length ? (
            <EmptyState icon={CalendarDays} message="Нет посещённых фестивалей" />
          ) : (
            <div className={styles.list}>
              {pastTickets.map(t => (
                <Card key={t.id} padding="md">
                  <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    <Ticket size={20} style={{ color: 'var(--accent-primary)', flexShrink: 0 }} />
                    <div>
                      <p style={{ fontWeight: 700 }}>{t.festivalName}</p>
                      <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 2 }}>
                        {t.ticketTypeName} · {new Date(t.startDate).toLocaleDateString('ru-RU')}
                      </p>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          )
        ) : (
          !pastRegs.length ? (
            <EmptyState icon={Award} message="Нет посещённых мероприятий" />
          ) : (
            <div className={styles.list}>
              {pastRegs.map(r => (
                <Card key={r.id} padding="md">
                  <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    <CalendarDays size={20} style={{ color: 'var(--accent-secondary)', flexShrink: 0 }} />
                    <div>
                      <p style={{ fontWeight: 700 }}>{r.eventName}</p>
                      <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 2 }}>
                        {r.festivalName} · {new Date(r.eventDate).toLocaleDateString('ru-RU')}
                      </p>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          )
        )}
      </div>
    </Layout>
  );
}
