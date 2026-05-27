import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useQueries, useMutation } from '@tanstack/react-query';
import { QrCode, Award, LayoutList, Trophy, Clock, Users, Building2, Calendar } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import EmptyState from '../../components/ui/EmptyState';
import Spinner from '../../components/ui/Spinner';
import QRScanner from '../../components/QRScanner';
import { eventApi } from '../../api/event';
import { festivalApi } from '../../api/festival';
import type { FestivalEvent } from '../../types';
import styles from './CompanyEventsPage.module.css';

const TYPE_LABEL: Record<string, string> = {
  TOURNAMENT:    'Турнир',
  QUEST:         'Квест',
  MASTER_CLASS:  'Мастер-класс',
  MASTERCLASS:   'Мастер-класс',
  DEMONSTRATION: 'Демонстрация',
  DEMO:          'Демонстрация',
};

function fmtDate(iso: string) {
  return new Date(iso + 'T00:00:00').toLocaleDateString('ru-RU', { day: 'numeric', month: 'short', year: 'numeric' });
}

function fmtTime(t?: string) { return t ? t.substring(0, 5) : ''; }

function EventCard({ ev, onDistribute }: { ev: FestivalEvent & { festivalName: string }; onDistribute: () => void }) {
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
        {ev.festivalName && <p className={styles.cardFestival}>{ev.festivalName}</p>}
        {ev.description && <p className={styles.cardDesc}>{ev.description}</p>}
        <div className={styles.cardMeta}>
          {ev.startDate && <span><Calendar size={11} />{fmtDate(ev.startDate)}</span>}
          {ev.startTime && <span><Clock size={11} />{fmtTime(ev.startTime)}</span>}
          {ev.approximateDuration != null && <span>~{ev.approximateDuration} мин</span>}
          {ev.maxParticipants ? <span><Users size={11} />до {ev.maxParticipants}</span> : null}
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

export default function CompanyEventsPage() {
  const navigate = useNavigate();
  const [scanModal, setScanModal] = useState(false);
  const [scanResult, setScanResult] = useState<{ valid: boolean; message: string } | null>(null);

  const { data: allEvents = [], isLoading: eventsLoading } = useQuery({
    queryKey: ['company-events'],
    queryFn: eventApi.myCompanyEvents,
  });

  const verifyMutation = useMutation({
    mutationFn: (token: string) => eventApi.verifyEventQr(token),
    onSuccess: () => setScanResult({ valid: true, message: '✅ Регистрация действительна' }),
    onError: () => setScanResult({ valid: false, message: '❌ Недействительный QR-код' }),
  });

  // Fetch visibility status for each unique festival ID referenced by company's events
  const uniqueFestivalIds = useMemo(
    () => [...new Set(allEvents.map(ev => ev.festivalId))],
    [allEvents],
  );

  const festivalInfoQueries = useQueries({
    queries: uniqueFestivalIds.map(festivalId => ({
      queryKey: ['festival-info', festivalId],
      queryFn: () => festivalApi.getFestivalInfo(festivalId),
      enabled: !eventsLoading,
    })),
  });

  const festivalInfoMap = useMemo(() => {
    const map: Record<string, { name: string; isVisible: boolean }> = {};
    festivalInfoQueries.forEach(q => {
      if (q.data) map[q.data.id] = q.data;
    });
    return map;
  }, [festivalInfoQueries]);

  // Keep only events that belong to published (visible) festivals
  const events = useMemo(
    () => allEvents
      .filter(ev => festivalInfoMap[ev.festivalId]?.isVisible === true)
      .map(ev => ({ ...ev, festivalName: festivalInfoMap[ev.festivalId]?.name ?? '' })),
    [allEvents, festivalInfoMap],
  );

  const festivalsLoading = uniqueFestivalIds.length > 0 && festivalInfoQueries.some(q => q.isLoading);
  const isLoading = eventsLoading || festivalsLoading;

  if (isLoading) return <Layout><Spinner centered /></Layout>;

  return (
    <Layout>
      <div className={styles.pageHeader}>
        <h1 className={styles.title}>Мои мероприятия</h1>
        <Button variant="secondary" onClick={() => setScanModal(true)}>
          <QrCode size={16} /> Сканер QR
        </Button>
      </div>

      {!events.length ? (
        <EmptyState
          icon={LayoutList}
          message="Нет мероприятий"
          description="Мероприятия появятся здесь после публикации фестиваля"
        />
      ) : (
        <div className={styles.list}>
          {events.map(ev => (
            <EventCard
              key={ev.id}
              ev={ev}
              onDistribute={() => navigate(`/company/events/${ev.id}/distribute`)}
            />
          ))}
        </div>
      )}

      {/* QR Scanner modal */}
      <Modal
        open={scanModal}
        onClose={() => { setScanModal(false); setScanResult(null); }}
        title="Сканер QR"
        size="md"
      >
        {scanResult ? (
          <div style={{ textAlign: 'center', padding: '24px 0' }}>
            <p style={{ fontSize: 20, fontWeight: 700 }}>{scanResult.message}</p>
            <Button style={{ marginTop: 16 }} onClick={() => setScanResult(null)}>Сканировать ещё</Button>
          </div>
        ) : (
          <QRScanner onScan={text => verifyMutation.mutate(text)} />
        )}
      </Modal>
    </Layout>
  );
}
