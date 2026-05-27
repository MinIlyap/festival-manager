import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ChevronLeft, Trophy, Clock, Users, Building2, Award, Calendar } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import EmptyState from '../../components/ui/EmptyState';
import { eventApi } from '../../api/event';
import { festivalApi } from '../../api/festival';
import type { FestivalEvent } from '../../types';
import styles from './CuratorEventsPage.module.css';

const TYPE_LABEL: Record<string, string> = {
  TOURNAMENT:    'Турнир',
  QUEST:         'Квест',
  MASTER_CLASS:  'Мастер-класс',
  MASTERCLASS:   'Мастер-класс',
  DEMONSTRATION: 'Демонстрация',
  DEMO:          'Демонстрация',
};

function fmtTime(t?: string) { return t ? t.substring(0, 5) : ''; }

function fmtDate(iso: string) {
  return new Date(iso + 'T00:00:00').toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
}

function EventCard({ ev, onDistribute }: { ev: FestivalEvent; onDistribute: () => void }) {
  const [imgErr, setImgErr] = useState(false);

  return (
    <div className={styles.card}>
      <div className={styles.cardPhoto}>
        {!imgErr ? (
          <img src={`/api/files/FESTIVAL_EVENT/${ev.id}`} alt={ev.name} onError={() => setImgErr(true)} />
        ) : (
          <div className={styles.cardPhotoFallback}><Trophy size={28} /></div>
        )}
      </div>
      <div className={styles.cardBody}>
        <div className={styles.cardBadges}>
          <Badge status="info">{TYPE_LABEL[ev.type] ?? ev.type}</Badge>
          <Badge status={ev.format === 'TEAM' ? 'pending' : 'active'}>
            {ev.format === 'TEAM'
              ? `Командное${ev.teamSize ? ` · до ${ev.teamSize}` : ''}`
              : 'Одиночное'}
          </Badge>
        </div>
        <p className={styles.cardName}>{ev.name}</p>
        {ev.description && <p className={styles.cardDesc}>{ev.description}</p>}
        <div className={styles.cardMeta}>
          {ev.startDate && <span><Calendar size={11} />{fmtDate(ev.startDate)}</span>}
          {ev.startTime && <span><Clock size={11} />{fmtTime(ev.startTime)}</span>}
          {ev.approximateDuration != null && <span>~{ev.approximateDuration} мин</span>}
          {ev.maxParticipants ? <span><Users size={11} />до {ev.maxParticipants}</span> : null}
          {ev.companyName && <span><Building2 size={11} />{ev.companyName}</span>}
        </div>
      </div>
      <div className={styles.cardAction}>
        <Button variant="primary" size="sm" onClick={onDistribute}>
          <Award size={14} /> Распределить награды
        </Button>
      </div>
    </div>
  );
}

export default function CuratorEventsPage() {
  const { festivalId } = useParams<{ festivalId: string }>();
  const navigate = useNavigate();

  const { data: festivalInfo } = useQuery({
    queryKey: ['festival-basic', festivalId],
    queryFn: () => festivalApi.getFestivalBasicInfo(festivalId!),
    enabled: !!festivalId,
  });

  const { data: events = [], isLoading } = useQuery({
    queryKey: ['festival-events', festivalId],
    queryFn: () => eventApi.getEventsByFestival(festivalId!),
    enabled: !!festivalId,
  });

  if (isLoading) return <Layout><Spinner centered /></Layout>;

  return (
    <Layout>
      <div className={styles.page}>
        <button className={styles.back} onClick={() => navigate('/curator/festivals')}>
          <ChevronLeft size={18} /> Назад к фестивалям
        </button>

        <h1 className={styles.title}>{festivalInfo?.name ?? 'Мероприятия'}</h1>
        <p className={styles.subtitle}>Выберите мероприятие для распределения наград</p>

        {!events.length ? (
          <EmptyState
            icon={Trophy}
            message="Нет мероприятий"
            description="На этом фестивале пока нет мероприятий"
          />
        ) : (
          <div className={styles.list}>
            {events.map(ev => (
              <EventCard
                key={ev.id}
                ev={ev}
                onDistribute={() => navigate(`/curator/events/${ev.id}/distribute`)}
              />
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
}
