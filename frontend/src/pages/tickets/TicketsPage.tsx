import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { QrCode, Ticket as TicketIcon, CalendarCheck } from 'lucide-react';
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

function TicketCard({ ticket }: { ticket: Ticket }) {
  const [qrOpen, setQrOpen] = useState(false);
  const statusLabel: Record<string, string> = { ACTIVE: 'Активный', EXPIRED: 'Истёк', USED: 'Использован' };
  const statusBadge: Record<string, 'active' | 'expired' | 'rejected'> = { ACTIVE: 'active', EXPIRED: 'expired', USED: 'rejected' };

  return (
    <Card padding="md">
      <div className={styles.ticketHeader}>
        <div>
          <p className={styles.ticketFest}>{ticket.festivalName}</p>
          <p className={styles.ticketType}>{ticket.ticketTypeName} · {ticket.price === 0 ? 'Бесплатно' : `${ticket.price} ₽`}</p>
          <p className={styles.ticketDates}>{new Date(ticket.startDate).toLocaleDateString('ru-RU')} — {new Date(ticket.endDate).toLocaleDateString('ru-RU')}</p>
          <p className={styles.ticketNum}>{ticket.number}</p>
        </div>
        <div className={styles.ticketRight}>
          <Badge status={statusBadge[ticket.status] ?? 'info'}>{statusLabel[ticket.status] ?? ticket.status}</Badge>
          {ticket.status === 'ACTIVE' && ticket.qrToken && (
            <Button size="sm" variant="secondary" onClick={() => setQrOpen(true)}>
              <QrCode size={14} /> QR-код
            </Button>
          )}
        </div>
      </div>
      {ticket.qrToken && (
        <QRModal open={qrOpen} onClose={() => setQrOpen(false)} value={ticket.qrToken} title="Билет QR" subtitle={ticket.number} />
      )}
    </Card>
  );
}

function RegistrationCard({ reg }: { reg: EventRegistration }) {
  const [qrOpen, setQrOpen] = useState(false);
  const statusBadge: Record<string, 'active' | 'expired' | 'rejected'> = { ACTIVE: 'active', COMPLETED: 'expired', CANCELLED: 'rejected' };

  return (
    <Card padding="md">
      <div className={styles.ticketHeader}>
        <div>
          <p className={styles.ticketFest}>{reg.festivalName} → {reg.eventName}</p>
          <p className={styles.ticketDates}>{new Date(reg.eventDate).toLocaleDateString('ru-RU')}</p>
          <p className={styles.ticketType}>{reg.format === 'SOLO' ? 'Одиночное' : `Команда: ${reg.teamName ?? '—'}`}</p>
        </div>
        <div className={styles.ticketRight}>
          <Badge status={statusBadge[reg.status] ?? 'info'}>{reg.status}</Badge>
          {reg.qrToken && (
            <Button size="sm" variant="secondary" onClick={() => setQrOpen(true)}>
              <QrCode size={14} /> QR-код
            </Button>
          )}
        </div>
      </div>
      {reg.qrToken && (
        <QRModal open={qrOpen} onClose={() => setQrOpen(false)} value={reg.qrToken} title="Регистрация QR" />
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
