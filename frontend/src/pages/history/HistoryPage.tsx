import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Trophy, Calendar, Award } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Tabs from '../../components/ui/Tabs';
import Card from '../../components/ui/Card';
import EmptyState from '../../components/ui/EmptyState';
import Spinner from '../../components/ui/Spinner';
import { festivalApi } from '../../api/festival';
import { eventApi } from '../../api/event';
import styles from './HistoryPage.module.css';

export default function HistoryPage() {
  const [tab, setTab] = useState('festivals');

  const { data: tickets, isLoading: tLoad } = useQuery({
    queryKey: ['my-tickets-history'],
    queryFn: festivalApi.myTickets,
    enabled: tab === 'festivals',
  });

  const { data: regs, isLoading: rLoad } = useQuery({
    queryKey: ['my-regs-history'],
    queryFn: eventApi.myRegistrations,
    enabled: tab === 'events',
  });

  const pastTickets = (tickets ?? []).filter(t => t.status !== 'ACTIVE');
  const pastRegs    = (regs ?? []).filter(r => r.status === 'COMPLETED');

  return (
    <Layout>
      <h1 className={styles.title}>История</h1>
      <Tabs
        tabs={[
          { key: 'festivals', label: '🎪 Посещённые фестивали' },
          { key: 'events',    label: '🎮 Мероприятия' },
          { key: 'awards',    label: '🏆 Награды' },
        ]}
        activeKey={tab}
        onChange={setTab}
      />
      <div className={styles.content}>
        {tab === 'festivals' && (
          tLoad ? <Spinner centered /> :
          !pastTickets.length ? <EmptyState icon={Trophy} message="Нет посещённых фестивалей" /> :
          <div className={styles.list}>
            {pastTickets.map(t => (
              <Card key={t.id} padding="md">
                <div className={styles.histItem}>
                  <Trophy size={20} style={{ color: 'var(--accent-primary)' }} />
                  <div>
                    <p className={styles.histTitle}>{t.festivalName}</p>
                    <p className={styles.histSub}>{t.ticketTypeName} · {new Date(t.startDate).toLocaleDateString('ru-RU')}</p>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
        {tab === 'events' && (
          rLoad ? <Spinner centered /> :
          !pastRegs.length ? <EmptyState icon={Calendar} message="Нет завершённых мероприятий" /> :
          <div className={styles.list}>
            {pastRegs.map(r => (
              <Card key={r.id} padding="md">
                <div className={styles.histItem}>
                  <Calendar size={20} style={{ color: 'var(--accent-secondary)' }} />
                  <div>
                    <p className={styles.histTitle}>{r.eventName}</p>
                    <p className={styles.histSub}>{r.festivalName} · {new Date(r.eventDate).toLocaleDateString('ru-RU')}</p>
                    {r.teamName && <p className={styles.histSub}>Команда: {r.teamName}</p>}
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
        {tab === 'awards' && (
          <EmptyState icon={Award} message="Нет наград" description="Участвуй в мероприятиях и побеждай!" />
        )}
      </div>
    </Layout>
  );
}
