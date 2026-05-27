import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { QrCode, Ticket as TicketIcon, CalendarCheck, Calendar, Users, Hash, Tag, MapPin } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Tabs from '../../components/ui/Tabs';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import EmptyState from '../../components/ui/EmptyState';
import QRModal from '../../components/QRModal';
import Spinner from '../../components/ui/Spinner';
import { festivalApi } from '../../api/festival';
import { eventApi } from '../../api/event';
import type { Ticket, EventRegistration } from '../../types';
import styles from './TicketsPage.module.css';

const TICKET_STATUS_LABEL: Record<string, string> = { ACTIVE: 'Активный', EXPIRED: 'Истёк', USED: 'Использован' };
const TICKET_STATUS_BADGE: Record<string, 'active' | 'expired' | 'rejected'> = { ACTIVE: 'active', EXPIRED: 'expired', USED: 'rejected' };
const REG_STATUS_LABEL: Record<string, string> = { ACTIVE: 'Активна', COMPLETED: 'Завершена', CANCELLED: 'Отменена' };
const REG_STATUS_BADGE: Record<string, 'active' | 'expired' | 'rejected'> = { ACTIVE: 'active', COMPLETED: 'expired', CANCELLED: 'rejected' };

function fmtDate(iso: string) {
  return new Date(iso).toLocaleDateString('ru-RU', { day: 'numeric', month: 'short', year: 'numeric' });
}
function fmtDateLong(iso: string) {
  return new Date(iso).toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric' });
}

function useFestivalInfo(festivalId: string) {
  return useQuery({
    queryKey: ['festival-basic', festivalId],
    queryFn: () => festivalApi.getFestivalBasicInfo(festivalId),
    staleTime: 10 * 60 * 1000,
    enabled: !!festivalId,
  });
}

function LocationLine({ cityName, platformName }: { cityName?: string; platformName?: string }) {
  if (!cityName && !platformName) return null;
  const text = [cityName, platformName].filter(Boolean).join(' · ');
  return (
    <div className={styles.ticketMetaItem}>
      <MapPin size={12} className={styles.ticketMetaIcon} />
      <span className={styles.ticketMetaValue}>{text}</span>
    </div>
  );
}

function TicketCard({ ticket }: { ticket: Ticket }) {
  const [qrOpen, setQrOpen] = useState(false);
  const [qrToken, setQrToken] = useState<string | null>(null);
  const { data: festInfo } = useFestivalInfo(ticket.festivalId);

  const { mutate: generateQr, isPending: qrLoading } = useMutation({
    mutationFn: () => festivalApi.generateTicketQr(ticket.id),
    onSuccess: (data: { token: string }) => { setQrToken(data.token); setQrOpen(true); },
  });

  const handleQrClick = () => { if (qrToken) setQrOpen(true); else generateQr(); };

  const startStr = ticket.startDate ? fmtDate(ticket.startDate) : null;
  const endStr = ticket.endDate ? fmtDate(ticket.endDate) : null;
  const isActive = ticket.status === 'ACTIVE';

  return (
    <Card padding="none">
      <div className={styles.ticketCard}>
        <div className={[styles.ticketStripe, isActive ? styles.ticketStripeActive : styles.ticketStripeInactive].join(' ')} />
        <div className={styles.ticketBody}>
          <div className={styles.ticketTop}>
            <div className={styles.ticketTitleBlock}>
              <p className={styles.ticketFest}>{ticket.festivalName}</p>
              <p className={styles.ticketType}>
                <Tag size={11} /> {ticket.ticketTypeName}
              </p>
            </div>
            <Badge status={TICKET_STATUS_BADGE[ticket.status] ?? 'info'}>
              {TICKET_STATUS_LABEL[ticket.status] ?? ticket.status}
            </Badge>
          </div>

          <div className={styles.ticketMeta}>
            <LocationLine cityName={festInfo?.cityName} platformName={festInfo?.platformName} />
            {(startStr || endStr) && (
              <div className={styles.ticketMetaItem}>
                <Calendar size={12} className={styles.ticketMetaIcon} />
                <span className={styles.ticketMetaValue}>
                  {startStr && endStr ? `${startStr} — ${endStr}` : (startStr ?? endStr)}
                </span>
              </div>
            )}
            <div className={styles.ticketMetaItem}>
              <Tag size={12} className={styles.ticketMetaIcon} />
              <span className={styles.ticketMetaValue}>
                {ticket.price === 0 ? 'Бесплатно' : `${ticket.price.toLocaleString('ru-RU')} ₽`}
              </span>
            </div>
          </div>

          <div className={styles.ticketFooter}>
            <span className={styles.ticketNum}>
              <Hash size={11} /> {ticket.number}
            </span>
            {isActive && (
              <Button size="sm" variant="secondary" onClick={handleQrClick} loading={qrLoading}>
                <QrCode size={14} /> QR-код
              </Button>
            )}
          </div>
        </div>
      </div>
      {qrToken && (
        <QRModal open={qrOpen} onClose={() => setQrOpen(false)} value={qrToken} title="Билет QR" subtitle={ticket.number} />
      )}
    </Card>
  );
}

function RegistrationCard({ reg }: { reg: EventRegistration }) {
  const [qrOpen, setQrOpen] = useState(false);
  const [qrToken, setQrToken] = useState<string | null>(null);
  const { data: festInfo } = useFestivalInfo(reg.festivalId);

  const { mutate: generateQr, isPending: qrLoading } = useMutation({
    mutationFn: () => eventApi.generateEventQr(reg.eventId),
    onSuccess: (data: { token: string }) => { setQrToken(data.token); setQrOpen(true); },
  });

  const handleQrClick = () => { if (qrToken) setQrOpen(true); else generateQr(); };

  const isActive = reg.status === 'ACTIVE';
  const festivalDisplayName = festInfo?.name || reg.festivalName;

  return (
    <Card padding="none">
      <div className={styles.ticketCard}>
        <div className={[styles.ticketStripe, isActive ? styles.ticketStripeEvent : styles.ticketStripeInactive].join(' ')} />
        <div className={styles.ticketBody}>
          <div className={styles.ticketTop}>
            <div className={styles.ticketTitleBlock}>
              <p className={styles.ticketFest}>{reg.eventName}</p>
              {festivalDisplayName && (
                <p className={styles.ticketSubtitle}>{festivalDisplayName}</p>
              )}
            </div>
            <Badge status={REG_STATUS_BADGE[reg.status] ?? 'info'}>
              {REG_STATUS_LABEL[reg.status] ?? reg.status}
            </Badge>
          </div>

          <div className={styles.ticketMeta}>
            <LocationLine cityName={festInfo?.cityName} platformName={festInfo?.platformName} />
            <div className={styles.ticketMetaItem}>
              <Calendar size={12} className={styles.ticketMetaIcon} />
              <span className={styles.ticketMetaValue}>{fmtDateLong(reg.eventDate)}</span>
            </div>
            <div className={styles.ticketMetaItem}>
              <Users size={12} className={styles.ticketMetaIcon} />
              <span className={styles.ticketMetaValue}>
                {reg.format === 'SOLO' ? 'Одиночное участие' : `Командное${reg.teamName ? ` · ${reg.teamName}` : ''}`}
              </span>
            </div>
          </div>

          {isActive && (
            <div className={styles.ticketFooter}>
              <span />
              <Button size="sm" variant="secondary" onClick={handleQrClick} loading={qrLoading}>
                <QrCode size={14} /> QR-код
              </Button>
            </div>
          )}
        </div>
      </div>
      {qrToken && (
        <QRModal open={qrOpen} onClose={() => setQrOpen(false)} value={qrToken} title="Регистрация QR" subtitle={reg.eventName} />
      )}
    </Card>
  );
}

export default function TicketsPage() {
  const [tab, setTab] = useState('tickets');
  const [subTab, setSubTab] = useState('active');

  const { data: tickets, isLoading: tLoading } = useQuery({
    queryKey: ['my-tickets'],
    queryFn: festivalApi.myTickets,
  });

  const { data: regs, isLoading: rLoading } = useQuery({
    queryKey: ['my-regs'],
    queryFn: eventApi.myRegistrations,
  });

  const activeTickets = (tickets ?? []).filter(t => t.status === 'ACTIVE');
  const expiredTickets = (tickets ?? []).filter(t => t.status !== 'ACTIVE');

  return (
    <Layout>
      <h1 className={styles.title}>Мои билеты и регистрации</h1>
      <Tabs
        tabs={[{ key: 'tickets', label: 'Билеты на фестивали' }, { key: 'regs', label: 'Регистрации на мероприятия' }]}
        activeKey={tab}
        onChange={setTab}
      />
      <div className={styles.content}>
        {tab === 'tickets' && (
          <>
            <Tabs
              tabs={[{ key: 'active', label: 'Активные' }, { key: 'expired', label: 'Прошедшие' }]}
              activeKey={subTab}
              onChange={setSubTab}
              className={styles.subTabs}
            />
            {tLoading ? <Spinner centered /> :
              subTab === 'active'
                ? activeTickets.length === 0 ? <EmptyState icon={TicketIcon} message="Нет активных билетов" />
                  : <div className={styles.list}>{activeTickets.map(t => <TicketCard key={t.id} ticket={t} />)}</div>
                : expiredTickets.length === 0 ? <EmptyState icon={TicketIcon} message="Нет прошедших билетов" />
                  : <div className={styles.list}>{expiredTickets.map(t => <TicketCard key={t.id} ticket={t} />)}</div>
            }
          </>
        )}
        {tab === 'regs' && (
          rLoading ? <Spinner centered /> :
          !regs?.length ? <EmptyState icon={CalendarCheck} message="Нет регистраций" />
          : <div className={styles.list}>{regs.map(r => <RegistrationCard key={r.id} reg={r} />)}</div>
        )}
      </div>
    </Layout>
  );
}
