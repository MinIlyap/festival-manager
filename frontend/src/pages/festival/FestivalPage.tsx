import { useState, useMemo, useEffect } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  MapPin, Calendar, Trophy, Store, Ticket, AlertCircle,
  Clock, Users, ChevronRight, CheckCircle, Building2, Check, X, Info,
} from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import { festivalApi } from '../../api/festival';
import { eventApi } from '../../api/event';
import { userApi } from '../../api/user';
import { applicationApi } from '../../api/application';
import { useAuthStore } from '../../store/authStore';
import type { FestivalEvent, Stand } from '../../types';
import styles from './FestivalPage.module.css';

// ── helpers ───────────────────────────────────────────────────────────────────

function getDays(start: string, end: string): string[] {
  const days: string[] = [];
  const [sy, sm, sd] = start.split('-').map(Number);
  const [ey, em, ed] = end.split('-').map(Number);
  const cur = new Date(Date.UTC(sy, sm - 1, sd));
  const endD = new Date(Date.UTC(ey, em - 1, ed));
  while (cur <= endD) {
    days.push(cur.toISOString().slice(0, 10));
    cur.setUTCDate(cur.getUTCDate() + 1);
  }
  return days;
}

const TYPE_LABEL: Record<string, string> = {
  TOURNAMENT:    'Турнир',
  QUEST:         'Квест',
  MASTER_CLASS:  'Мастер-класс',
  MASTERCLASS:   'Мастер-класс',
  DEMONSTRATION: 'Демонстрация',
  DEMO:          'Демонстрация',
};

function fmtDateLong(iso: string) {
  return new Date(iso + 'T00:00:00').toLocaleDateString('ru-RU', {
    day: 'numeric', month: 'long', year: 'numeric',
  });
}

function fmtDayFull(iso: string) {
  return new Date(iso + 'T00:00:00').toLocaleDateString('ru-RU', {
    weekday: 'short', day: 'numeric', month: 'short',
  });
}

function fmtDayShort(iso: string) {
  const d = new Date(iso + 'T00:00:00');
  return d.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' }).replace('.', '');
}

function fmtTime(t?: string) {
  return t ? t.substring(0, 5) : '';
}

function sortByTime<T extends { startTime?: string }>(items: T[]): T[] {
  return [...items].sort((a, b) => {
    if (!a.startTime) return 1;
    if (!b.startTime) return -1;
    return a.startTime.localeCompare(b.startTime);
  });
}

// ── Event card (vertical grid) ───────────────────────────────────────────────

function EventCard({ ev, onClick }: { ev: FestivalEvent; onClick: () => void }) {
  const [imgErr, setImgErr] = useState(false);
  return (
    <div className={styles.itemCard} onClick={onClick} role="button" tabIndex={0}
         onKeyDown={e => e.key === 'Enter' && onClick()}>
      <div className={styles.itemCardPhoto}>
        {!imgErr ? (
          <img
            src={`/api/files/FESTIVAL_EVENT/${ev.id}`}
            alt={ev.name}
            onError={() => setImgErr(true)}
          />
        ) : (
          <div className={styles.itemCardPhotoFallback}>
            <Trophy size={28} />
          </div>
        )}
      </div>
      <div className={styles.itemCardBody}>
        <div className={styles.itemCardBadges}>
          <Badge status="info">{TYPE_LABEL[ev.type] ?? ev.type}</Badge>
          <Badge status={ev.format === 'TEAM' ? 'pending' : 'active'}>
            {ev.format === 'TEAM'
              ? `Командное${ev.teamSize ? ` · до ${ev.teamSize}` : ''}`
              : 'Одиночное'}
          </Badge>
        </div>
        <p className={styles.itemCardName}>{ev.name}</p>
        {ev.description && <p className={styles.itemCardDesc}>{ev.description}</p>}
        <div className={styles.itemCardMeta}>
          {ev.startTime && <span><Clock size={11} />{fmtTime(ev.startTime)}</span>}
          {ev.approximateDuration && <span>~{ev.approximateDuration} мин</span>}
          {ev.maxParticipants ? <span><Users size={11} />до {ev.maxParticipants}</span> : null}
          {ev.companyName && <span><Building2 size={11} />{ev.companyName}</span>}
        </div>
      </div>
      <ChevronRight size={14} className={styles.itemCardArrow} />
    </div>
  );
}

// ── Stand card (vertical grid) ───────────────────────────────────────────────

function StandCard({ st, onClick }: { st: Stand; onClick: () => void }) {
  const [imgErr, setImgErr] = useState(false);
  return (
    <div className={styles.itemCard} onClick={onClick} role="button" tabIndex={0}
         onKeyDown={e => e.key === 'Enter' && onClick()}>
      <div className={styles.itemCardPhoto}>
        {!imgErr ? (
          <img
            src={`/api/files/FESTIVAL_STAND/${st.id}`}
            alt={st.name}
            onError={() => setImgErr(true)}
          />
        ) : (
          <div className={styles.itemCardPhotoFallback}>
            <Store size={28} />
          </div>
        )}
      </div>
      <div className={styles.itemCardBody}>
        <div className={styles.itemCardBadges}>
          <Badge status={st.type === 'COMMERCIAL' ? 'active' : 'info'}>
            {st.type === 'COMMERCIAL' ? 'Коммерческий' : 'Демонстрационный'}
          </Badge>
        </div>
        <p className={styles.itemCardName}>{st.name}</p>
        {st.companyName && <p className={styles.itemCardCompany}>{st.companyName}</p>}
        {st.description && <p className={styles.itemCardDesc}>{st.description}</p>}
      </div>
      <ChevronRight size={14} className={styles.itemCardArrow} />
    </div>
  );
}

// ── main page ─────────────────────────────────────────────────────────────────

export default function FestivalPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const qc = useQueryClient();
  const { accessToken, role } = useAuthStore();

  const adminReviewAppId = searchParams.get('adminReview');
  const isAdminReview = role === 'ADMIN' && !!adminReviewAppId;

  const [activeDay, setActiveDay] = useState<string>('');
  const [activeSection, setActiveSection] = useState<'events' | 'stands'>('events');
  const [buyModal, setBuyModal] = useState(false);
  const [selectedTicketId, setSelectedTicketId] = useState<string | null>(null);
  const [selectedDays, setSelectedDays] = useState<Set<string>>(new Set());
  const [buySuccess, setBuySuccess] = useState(false);
  const [hasCover, setHasCover] = useState(true);
  const [rejectModal, setRejectModal] = useState(false);
  const [rejectReason, setRejectReason] = useState('');

  const { data: festival, isLoading } = useQuery({
    queryKey: ['festival', id],
    queryFn: () => festivalApi.getById(id!),
    enabled: !!id,
  });

  const { data: festivalEvents = [], isLoading: eventsLoading } = useQuery({
    queryKey: ['festival-events', id],
    queryFn: () => eventApi.getEventsByFestival(id!),
    enabled: !!id && !!festival,
  });

  const { data: festivalStands = [] } = useQuery({
    queryKey: ['festival-stands', id],
    queryFn: () => eventApi.getStandsByFestival(id!),
    enabled: !!id && !!festival,
  });

  const { data: blacklistStatus } = useQuery({
    queryKey: ['my-blacklist'],
    queryFn: userApi.checkBlacklist,
    enabled: !!accessToken && (role === 'USER' || role === 'CURATOR'),
  });

  const { data: myProfile } = useQuery({
    queryKey: ['my-profile'],
    queryFn: userApi.getMe,
    enabled: !!accessToken && (role === 'USER' || role === 'CURATOR'),
  });

  const isBlacklisted = blacklistStatus?.inBlacklist ?? false;
  const ageRating = festival?.ageRating ?? 0;

  const payDisabledReason = useMemo(() => {
    if (!accessToken) return 'Необходимо войти в аккаунт';
    if (role === 'ADMIN' || role === 'COMPANY') return 'Покупка билетов недоступна для компаний и администраторов';
    if (isBlacklisted) return `Вы в чёрном списке${blacklistStatus?.until ? ` до ${fmtDateLong(blacklistStatus.until)}` : ''}`;
    if (myProfile) {
      if (!myProfile.phoneNumber) return 'Укажите номер телефона в профиле';
      if (myProfile.birthDate) {
        const [y, m, d] = myProfile.birthDate.split('-').map(Number);
        const birth = new Date(y, m - 1, d);
        const today = new Date();
        let age = today.getFullYear() - birth.getFullYear();
        const monthDiff = today.getMonth() - birth.getMonth();
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) age--;
        if (age < ageRating) return `Вам должно быть не менее ${ageRating} лет`;
      }
      if (ageRating >= 16 && (!myProfile.passportSeries || !myProfile.passportNumber)) {
        return 'Укажите паспортные данные в профиле (требуется для фестивалей 16+)';
      }
    }
    return null;
  }, [accessToken, role, isBlacklisted, blacklistStatus, myProfile, ageRating]);

  const canPay = !payDisabledReason;

  const days = useMemo(() => {
    if (!festival) return [];
    return getDays(festival.startDate, festival.endDate);
  }, [festival]);

  useEffect(() => {
    if (days.length > 0 && !activeDay) setActiveDay(days[0]);
  }, [days, activeDay]);

  const isFirstDay = activeDay === days[0];

  const eventsForDay = useMemo(
    () => sortByTime(festivalEvents.filter(ev => {
      if (!ev.startDate || !days.includes(ev.startDate)) return isFirstDay;
      return ev.startDate === activeDay;
    })),
    [festivalEvents, activeDay, isFirstDay, days],
  );

  const standsForDay = useMemo(
    () => festivalStands.filter(st => {
      if (!st.startDate || !days.includes(st.startDate)) return isFirstDay;
      return st.startDate === activeDay;
    }),
    [festivalStands, activeDay, isFirstDay, days],
  );

  const eventCountsByDay = useMemo(() => {
    const counts: Record<string, number> = {};
    const firstDay = days[0];
    festivalEvents.forEach(ev => {
      const day = (ev.startDate && days.includes(ev.startDate)) ? ev.startDate : firstDay;
      if (day) counts[day] = (counts[day] || 0) + 1;
    });
    return counts;
  }, [festivalEvents, days]);

  const selectedTicket = festival?.ticketTypes.find(t => t.id === selectedTicketId);

  const buyMutation = useMutation({
    mutationFn: (ticketTypeId: string) => festivalApi.buyTicket(ticketTypeId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival', id] });
      qc.invalidateQueries({ queryKey: ['my-tickets'] });
      setBuySuccess(true);
    },
  });

  const approveMutation = useMutation({
    mutationFn: () => applicationApi.approve(adminReviewAppId!),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-applications'] });
      navigate('/admin/applications');
    },
  });

  const rejectMutation = useMutation({
    mutationFn: () => applicationApi.reject(adminReviewAppId!, rejectReason),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-applications'] });
      navigate('/admin/applications');
    },
  });

  const closeBuyModal = () => {
    setBuyModal(false);
    setSelectedTicketId(null);
    setSelectedDays(new Set());
    setBuySuccess(false);
  };

  const toggleDay = (day: string) => {
    setSelectedDays(prev => {
      const next = new Set(prev);
      if (next.has(day)) next.delete(day); else next.add(day);
      return next;
    });
  };

  if (isLoading) return <Layout><Spinner centered /></Layout>;
  if (!festival) return <Layout><p style={{ color: 'var(--text-muted)' }}>Фестиваль не найден</p></Layout>;

  const hasTickets = festival.ticketTypes.length > 0;
  const locationStr = [festival.cityName, festival.platformName].filter(Boolean).join(' · ');

  return (
    <Layout>
      {/* ── Admin review bar ── */}
      {isAdminReview && (
        <div className={styles.adminReviewBar}>
          <div className={styles.adminReviewBarInfo}>
            <span className={styles.adminReviewBarTitle}>Заявка на утверждение фестиваля</span>
            <span className={styles.adminReviewBarSub}>Заявка #{adminReviewAppId} · {festival.name}</span>
          </div>
          <div className={styles.adminReviewBarActions}>
            <Button variant="danger" onClick={() => setRejectModal(true)}>
              <X size={16} /> Отклонить
            </Button>
            <Button loading={approveMutation.isPending} onClick={() => approveMutation.mutate()}>
              <Check size={16} /> Одобрить
            </Button>
          </div>
        </div>
      )}

      {/* ── Hero ── */}
      <div className={styles.hero}>
        {hasCover ? (
          <div className={styles.heroBg}>
            <img
              src={`/api/files/FESTIVAL_LOCATION/${festival.id}`}
              alt={festival.name}
              onError={() => setHasCover(false)}
            />
          </div>
        ) : (
          <div className={styles.heroBgGradient} />
        )}
        <div className={styles.heroOverlay} />

        <div className={styles.heroContent}>
          <div className={styles.heroTop}>
            <Badge status="info">{festival.ageRating}+</Badge>
            {festival.status === 'APPROVED' && <Badge status="active">Опубликован</Badge>}
          </div>

          <h1 className={styles.heroTitle}>{festival.name}</h1>

          <div className={styles.heroMeta}>
            {locationStr && (
              <div className={styles.heroMetaItem}>
                <MapPin size={14} />
                <span>{locationStr}</span>
              </div>
            )}
            {festival.address && (
              <div className={`${styles.heroMetaItem} ${styles.heroMetaIndented}`}>
                <span className={styles.heroMetaAddress}>{festival.address}</span>
              </div>
            )}
            <div className={styles.heroMetaItem}>
              <Calendar size={14} />
              <span>{fmtDateLong(festival.startDate)} — {fmtDateLong(festival.endDate)}</span>
            </div>
          </div>

          {festival.companies.length > 0 && (
            <div className={styles.heroOrgs}>
              {festival.companies.map(c => (
                <div key={c.id} className={styles.orgChip}>
                  {c.logoUrl && <img src={c.logoUrl} alt={c.brandName ?? c.fullName} className={styles.orgLogo} />}
                  <span>{c.brandName || c.fullName}</span>
                </div>
              ))}
            </div>
          )}

          {hasTickets && (
            <div className={styles.heroBuyBtn}>
              <Button onClick={() => setBuyModal(true)} size="lg">
                <Ticket size={16} /> Купить билет
              </Button>
            </div>
          )}
        </div>
      </div>

      {/* ── Description ── */}
      {festival.description && <p className={styles.desc}>{festival.description}</p>}

      {/* ── Day tabs (full-width, equal) ── */}
      {days.length > 0 && (
        <div className={styles.dayTabs}>
          {days.map((day, i) => (
            <button
              key={day}
              className={[styles.dayTab, activeDay === day ? styles.dayTabActive : ''].join(' ')}
              onClick={() => setActiveDay(day)}
            >
              <span className={styles.dayTabDate}>{fmtDayFull(day)}</span>
              <span className={styles.dayTabNum}>День {i + 1}</span>
            </button>
          ))}
        </div>
      )}

      {/* ── Section tabs: Мероприятия / Стенды ── */}
      <div className={styles.sectionTabs}>
        <button
          className={[styles.sectionTab, activeSection === 'events' ? styles.sectionTabActive : ''].join(' ')}
          onClick={() => setActiveSection('events')}
        >
          <Trophy size={14} />
          Мероприятия
          {eventsForDay.length > 0 && (
            <span className={styles.sectionTabCount}>{eventsForDay.length}</span>
          )}
        </button>
        <button
          className={[styles.sectionTab, activeSection === 'stands' ? styles.sectionTabActive : ''].join(' ')}
          onClick={() => setActiveSection('stands')}
        >
          <Store size={14} />
          Стенды
          {standsForDay.length > 0 && (
            <span className={styles.sectionTabCount}>{standsForDay.length}</span>
          )}
        </button>
      </div>

      {/* ── Content ── */}
      <section className={styles.contentSection}>
        {activeSection === 'events' ? (
          eventsLoading ? (
            <Spinner />
          ) : eventsForDay.length === 0 ? (
            <p className={styles.emptyHint}>Мероприятия не запланированы на этот день</p>
          ) : (
            <div className={styles.itemGrid}>
              {eventsForDay.map(ev => (
                <EventCard key={ev.id} ev={ev} onClick={() => navigate(`/event/${ev.id}`)} />
              ))}
            </div>
          )
        ) : (
          standsForDay.length === 0 ? (
            <p className={styles.emptyHint}>Стенды не запланированы на этот день</p>
          ) : (
            <div className={styles.itemGrid}>
              {standsForDay.map(st => (
                <StandCard
                  key={st.id}
                  st={st}
                  onClick={() =>
                    navigate(`/stand/${st.id}`, {
                      state: { stand: st, festivalName: festival.name, festivalId: id },
                    })
                  }
                />
              ))}
            </div>
          )
        )}
      </section>

      {/* ── Admin reject modal ── */}
      <Modal open={rejectModal} onClose={() => setRejectModal(false)} title="Причина отказа" size="sm">
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <textarea
            value={rejectReason}
            onChange={e => setRejectReason(e.target.value)}
            placeholder="Укажите причину отказа..."
            rows={4}
          />
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <Button variant="secondary" onClick={() => setRejectModal(false)}>Отмена</Button>
            <Button
              variant="danger"
              disabled={!rejectReason.trim()}
              loading={rejectMutation.isPending}
              onClick={() => rejectMutation.mutate()}
            >
              Отклонить
            </Button>
          </div>
        </div>
      </Modal>

      {/* ── Buy ticket modal ── */}
      <Modal open={buyModal} onClose={closeBuyModal} title="Купить билет">
        {buySuccess ? (
          <div className={styles.successState}>
            <CheckCircle size={52} className={styles.successIcon} />
            <p className={styles.successTitle}>Билет успешно оформлен!</p>
            <p className={styles.successSub}>Билет добавлен в раздел «Мои билеты»</p>
            <div className={styles.successActions}>
              <Button onClick={() => { closeBuyModal(); navigate('/tickets'); }}>Мои билеты</Button>
              <Button variant="secondary" onClick={closeBuyModal}>Закрыть</Button>
            </div>
          </div>
        ) : (
          <div className={styles.buyBody}>
            <p className={styles.buyLabel}>Тип билета</p>
            <div className={styles.ticketChoices}>
              {festival.ticketTypes.map(tt => (
                <div
                  key={tt.id}
                  className={[
                    styles.ticketChoice,
                    selectedTicketId === tt.id ? styles.ticketChoiceActive : '',
                    tt.remaining === 0 ? styles.ticketChoiceSoldOut : '',
                  ].join(' ')}
                  onClick={() => tt.remaining !== 0 && setSelectedTicketId(tt.id)}
                >
                  <div className={styles.ticketChoiceInfo}>
                    <p className={styles.ticketChoiceName}>{tt.name}</p>
                    {tt.description && <p className={styles.ticketChoiceDesc}>{tt.description}</p>}
                    {tt.remaining === 0 && <p className={styles.soldOutLabel}>Распродано</p>}
                  </div>
                  <span className={styles.ticketChoicePrice}>
                    {tt.price === 0 ? 'Бесплатно' : `${tt.price.toLocaleString('ru-RU')} ₽`}
                  </span>
                </div>
              ))}
            </div>

            {days.length > 1 && (
              <>
                <p className={styles.buyLabel}>Дни посещения</p>
                <div className={styles.dayCircles}>
                  {days.map((day, i) => (
                    <button
                      key={day}
                      type="button"
                      className={[styles.dayCircle, selectedDays.has(day) ? styles.dayCircleSelected : ''].join(' ')}
                      onClick={() => toggleDay(day)}
                    >
                      <span className={styles.dayCircleNum}>Д{i + 1}</span>
                      <span className={styles.dayCircleDate}>{fmtDayShort(day)}</span>
                    </button>
                  ))}
                </div>
                {selectedDays.size > 0 && (
                  <div className={styles.buyPeriod}>
                    <Info size={12} />
                    {fmtDateLong([...selectedDays].sort()[0])}{selectedDays.size > 1 ? ` — ${fmtDateLong([...selectedDays].sort().at(-1)!)}` : ''}
                  </div>
                )}
              </>
            )}

            {(() => {
              const daysCount = selectedDays.size > 0 ? selectedDays.size : 1;
              const total = selectedTicket ? selectedTicket.price * daysCount : null;
              return (
                <div className={styles.buyTotal}>
                  {selectedDays.size > 1 && selectedTicket && selectedTicket.price > 0 && (
                    <span className={styles.buyTotalCalc}>
                      {selectedTicket.price.toLocaleString('ru-RU')} ₽ × {daysCount} дн.
                    </span>
                  )}
                  Итого:{' '}
                  <strong>
                    {total === null ? '—' : total === 0 ? 'Бесплатно' : `${total.toLocaleString('ru-RU')} ₽`}
                  </strong>
                </div>
              );
            })()}

            {payDisabledReason && (
              <div className={styles.payDisabledNotice}>
                <AlertCircle size={14} />
                {payDisabledReason}
              </div>
            )}

            <Button
              fullWidth
              loading={buyMutation.isPending}
              disabled={!canPay || !selectedTicketId}
              onClick={() => selectedTicketId && buyMutation.mutate(selectedTicketId)}
            >
              Оплатить
            </Button>
          </div>
        )}
      </Modal>
    </Layout>
  );
}
