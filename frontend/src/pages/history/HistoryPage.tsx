import { useState } from 'react';
import { useQuery, useQueries } from '@tanstack/react-query';
import { Trophy, Calendar, Award } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Tabs from '../../components/ui/Tabs';
import Card from '../../components/ui/Card';
import EmptyState from '../../components/ui/EmptyState';
import Spinner from '../../components/ui/Spinner';
import { festivalApi } from '../../api/festival';
import { eventApi } from '../../api/event';
import styles from './HistoryPage.module.css';

const MEDAL: Record<number, string> = { 1: '🥇', 2: '🥈', 3: '🥉' };

function placeLabel(place?: number, pointNumber?: number): string | null {
  if (place) return `${MEDAL[place] ?? '🏅'} ${place} место`;
  if (pointNumber) return `${pointNumber} очков`;
  return null;
}

function AwardThumb({ eventRewardId, place }: { eventRewardId: number; place?: number }) {
  const [imgErr, setImgErr] = useState(false);
  const emoji = place ? (MEDAL[place] ?? '🏅') : '🏅';

  if (!imgErr) {
    return (
      <img
        src={`/api/files/EVENT_REWARD/${eventRewardId}`}
        alt=""
        onError={() => setImgErr(true)}
        className={styles.awardThumb}
      />
    );
  }
  return <span className={styles.awardEmoji}>{emoji}</span>;
}

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

  const { data: awardsRaw = [], isLoading: aLoad } = useQuery({
    queryKey: ['my-awards'],
    queryFn: eventApi.myRewards,
    enabled: tab === 'awards',
  });

  const uniqueFestivalIds = [...new Set(awardsRaw.map(g => g.festivalLocationId))];
  const festivalQueries = useQueries({
    queries: uniqueFestivalIds.map(id => ({
      queryKey: ['festival', String(id)],
      queryFn: () => festivalApi.getById(String(id)),
      enabled: tab === 'awards' && awardsRaw.length > 0,
    })),
  });
  const festivalNames: Record<number, string> = {};
  uniqueFestivalIds.forEach((id, i) => {
    const f = festivalQueries[i]?.data;
    if (f) festivalNames[id] = f.name;
  });

  const pastTickets = (tickets ?? []).filter(t => t.status !== 'ACTIVE');
  const pastRegs    = (regs ?? []).filter(r => r.status === 'COMPLETED');

  return (
    <Layout>
      <h1 className={styles.title}>История</h1>
      <Tabs
        tabs={[
          { key: 'festivals', label: 'Посещённые фестивали' },
          { key: 'events',    label: 'Мероприятия' },
          { key: 'awards',    label: 'Награды' },
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
          aLoad ? <Spinner centered /> :
          !awardsRaw.length ? (
            <EmptyState icon={Award} message="Нет наград" description="Участвуй в мероприятиях и побеждай!" />
          ) : (
            <div className={styles.awardGroups}>
              {awardsRaw.map(group => {
                const festName = festivalNames[group.festivalLocationId] ?? `Фестиваль #${group.festivalLocationId}`;
                return (
                  <div key={group.festivalLocationId} className={styles.awardGroup}>
                    <div className={styles.awardGroupHeader}>
                      <Trophy size={15} />
                      <span>{festName}</span>
                    </div>
                    <div className={styles.list}>
                      {group.rewards.map(a => {
                        const label = placeLabel(a.place, a.pointNumber);
                        return (
                          <Card key={a.eventRewardId} padding="md">
                            <div className={styles.histItem}>
                              <AwardThumb eventRewardId={a.eventRewardId} place={a.place} />
                              <div>
                                <p className={styles.histTitle}>{a.rewardName}</p>
                                <p className={styles.histSub}>{a.eventName}</p>
                                {label && (
                                  <p className={styles.histSub} style={{ color: 'var(--accent-primary)', fontWeight: 600 }}>
                                    {label}
                                  </p>
                                )}
                                {a.description && (
                                  <p className={styles.histSub} style={{ marginTop: 4 }}>{a.description}</p>
                                )}
                              </div>
                            </div>
                          </Card>
                        );
                      })}
                    </div>
                  </div>
                );
              })}
            </div>
          )
        )}
      </div>
    </Layout>
  );
}
