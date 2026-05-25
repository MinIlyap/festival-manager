import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { MapPin, Calendar, Trophy, Store, Ticket, AlertCircle } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Tabs from '../../components/ui/Tabs';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import { festivalApi } from '../../api/festival';
import { eventApi } from '../../api/event';
import { userApi } from '../../api/user';
import { useAuthStore } from '../../store/authStore';
import type { TicketType } from '../../types';
import styles from './FestivalPage.module.css';

function formatDate(d: string) {
  return new Date(d).toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric' });
}

function TicketTypeCard({ tt, onBuy }: { tt: TicketType; onBuy: (id: string) => void }) {
  return (
    <Card padding="md">
      <div className={styles.ttHeader}>
        <div>
          <p className={styles.ttName}>{tt.name}</p>
          {tt.description && <p className={styles.ttDesc}>{tt.description}</p>}
        </div>
        <div className={styles.ttPrice}>
          {tt.price === 0 ? 'Бесплатно' : `${tt.price.toLocaleString('ru-RU')} ₽`}
        </div>
      </div>
      <div className={styles.ttFooter}>
        <span className={styles.ttRemaining}>Осталось: {tt.remaining}</span>
        <Button size="sm" disabled={tt.remaining === 0} onClick={() => onBuy(tt.id)}>
          {tt.remaining === 0 ? 'Распродано' : 'Купить'}
        </Button>
      </div>
    </Card>
  );
}

export default function FestivalPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const { accessToken } = useAuthStore();
  const [tab, setTab] = useState('overview');
  const [buyConfirm, setBuyConfirm] = useState<string | null>(null);
  const [blacklistModal, setBlacklistModal] = useState(false);
  const [blacklistInfo, setBlacklistInfo] = useState<{ reason?: string; until?: string } | null>(null);

  const { data: festival, isLoading } = useQuery({
    queryKey: ['festival', id],
    queryFn: () => festivalApi.getById(id!),
    enabled: !!id,
  });

  const { data: festivalEvents = [] } = useQuery({
    queryKey: ['festival-events', id],
    queryFn: () => eventApi.getEventsByFestival(id!),
    enabled: !!id && tab === 'events',
  });

  const { data: festivalStands = [] } = useQuery({
    queryKey: ['festival-stands', id],
    queryFn: () => eventApi.getStandsByFestival(id!),
    enabled: !!id && tab === 'stands',
  });

  const buyMutation = useMutation({
    mutationFn: (ticketTypeId: string) => festivalApi.buyTicket(ticketTypeId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival', id] });
      qc.invalidateQueries({ queryKey: ['my-tickets'] });
      setBuyConfirm(null);
      navigate('/tickets');
    },
  });

  const handleBuyClick = async (ticketTypeId: string) => {
    if (!accessToken) { navigate('/auth/login'); return; }
    try {
      const bl = await userApi.checkBlacklist();
      if (bl.inBlacklist) {
        setBlacklistInfo(bl);
        setBlacklistModal(true);
        return;
      }
    } catch { /* ignore */ }
    setBuyConfirm(ticketTypeId);
  };

  if (isLoading) return <Layout><Spinner centered /></Layout>;
  if (!festival) return <Layout><p style={{ color: 'var(--text-muted)' }}>Фестиваль не найден</p></Layout>;

  const eventTabs = [
    { key: 'overview',  label: 'Обзор' },
    { key: 'events',    label: 'Мероприятия' },
    { key: 'stands',    label: 'Стенды' },
    { key: 'tickets',   label: 'Билеты' },
  ];

  return (
    <Layout noFooter={false}>
      {/* Hero */}
      <div className={styles.hero}>
        {festival.coverUrl && (
          <div className={styles.heroBg}>
            <img src={festival.coverUrl} alt={festival.name} />
          </div>
        )}
        <div className={styles.heroOverlay} />
        <div className={styles.heroContent}>
          <Badge status="info">{festival.ageRating}+</Badge>
          <h1 className={styles.heroTitle}>{festival.name}</h1>
          <div className={styles.heroMeta}>
            <span><MapPin size={14} /> {festival.cityName}{festival.platformName ? ` · ${festival.platformName}` : ''}</span>
            <span><Calendar size={14} /> {formatDate(festival.startDate)} — {formatDate(festival.endDate)}</span>
          </div>
          {festival.companies.length > 0 && (
            <div className={styles.heroOrgs}>
              {festival.companies.map(c => (
                <div key={c.id} className={styles.orgChip}>
                  {c.logoUrl ? <img src={c.logoUrl} alt={c.fullName} className={styles.orgLogo} /> : null}
                  <span>{c.fullName}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      <Tabs tabs={eventTabs} activeKey={tab} onChange={setTab} className={styles.pageTabs} />

      <div className={styles.tabContent}>
        {tab === 'overview' && (
          <div className={styles.overview}>
            {festival.description && <p className={styles.desc}>{festival.description}</p>}
            {festival.platformName && (
              <Card padding="md">
                <p className={styles.overviewLabel}>Площадка</p>
                <p>{festival.platformName}</p>
              </Card>
            )}
          </div>
        )}

        {tab === 'events' && (
          <div>
            <h2 className={styles.sectionTitle}>Мероприятия</h2>
            {festivalEvents.length === 0 ? (
              <div className={styles.emptySection}>
                <Trophy size={32} style={{ color: 'var(--text-muted)' }} />
                <p style={{ color: 'var(--text-muted)' }}>Мероприятия не добавлены</p>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                {festivalEvents.map(ev => (
                  <Card key={ev.id} padding="md" hoverable onClick={() => navigate(`/events/${ev.id}`)}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12 }}>
                      <div>
                        <p style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, fontSize: 16 }}>{ev.name}</p>
                        {ev.description && <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>{ev.description}</p>}
                      </div>
                      <div style={{ display: 'flex', gap: 6, flexShrink: 0 }}>
                        <Badge status="info">{ev.type}</Badge>
                        <Badge status={ev.format === 'TEAM' ? 'pending' : 'info'}>{ev.format === 'TEAM' ? 'Командное' : 'Соло'}</Badge>
                      </div>
                    </div>
                  </Card>
                ))}
              </div>
            )}
          </div>
        )}

        {tab === 'stands' && (
          <div>
            <h2 className={styles.sectionTitle}>Стенды</h2>
            {festivalStands.length === 0 ? (
              <div className={styles.emptySection}>
                <Store size={32} style={{ color: 'var(--text-muted)' }} />
                <p style={{ color: 'var(--text-muted)' }}>Стенды не добавлены</p>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                {festivalStands.map(st => (
                  <Card key={st.id} padding="md">
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12 }}>
                      <div>
                        <p style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, fontSize: 16 }}>{st.name}</p>
                        {st.description && <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>{st.description}</p>}
                      </div>
                      <Badge status="info">{st.type === 'TRADE' ? 'Торговый' : 'Демо'}</Badge>
                    </div>
                  </Card>
                ))}
              </div>
            )}
          </div>
        )}

        {tab === 'tickets' && (
          <div>
            <h2 className={styles.sectionTitle}>Типы билетов</h2>
            {festival.ticketTypes.length === 0
              ? <p style={{ color: 'var(--text-muted)' }}>Билеты пока не добавлены</p>
              : (
                <div className={styles.ticketsGrid}>
                  {festival.ticketTypes.map(tt => (
                    <TicketTypeCard key={tt.id} tt={tt} onBuy={handleBuyClick} />
                  ))}
                </div>
              )
            }
          </div>
        )}
      </div>

      {/* Buy confirm modal */}
      <Modal open={!!buyConfirm} onClose={() => setBuyConfirm(null)} title="Подтверждение покупки" size="sm">
        <div className={styles.confirmModal}>
          <p>Вы уверены, что хотите купить билет?</p>
          <div className={styles.confirmActions}>
            <Button variant="secondary" onClick={() => setBuyConfirm(null)}>Отмена</Button>
            <Button loading={buyMutation.isPending} onClick={() => buyConfirm && buyMutation.mutate(buyConfirm)}>
              Купить
            </Button>
          </div>
        </div>
      </Modal>

      {/* Blacklist modal */}
      <Modal open={blacklistModal} onClose={() => setBlacklistModal(false)} title="Доступ ограничен" size="sm">
        <div className={styles.confirmModal}>
          <AlertCircle size={32} style={{ color: 'var(--accent-danger)', margin: '0 auto' }} />
          <p style={{ textAlign: 'center' }}>
            Вы в чёрном списке
            {blacklistInfo?.until ? ` до ${formatDate(blacklistInfo.until)}` : ''}.
          </p>
          {blacklistInfo?.reason && <p style={{ color: 'var(--text-muted)', textAlign: 'center', fontSize: 13 }}>Причина: {blacklistInfo.reason}</p>}
          <Button fullWidth onClick={() => setBlacklistModal(false)}>Понятно</Button>
        </div>
      </Modal>
    </Layout>
  );
}
