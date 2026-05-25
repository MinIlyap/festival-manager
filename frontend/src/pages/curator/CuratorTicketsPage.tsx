import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Ticket, CalendarDays } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Tabs from '../../components/ui/Tabs';
import Badge from '../../components/ui/Badge';
import EmptyState from '../../components/ui/EmptyState';
import Spinner from '../../components/ui/Spinner';
import QRModal from '../../components/QRModal';
import { festivalApi } from '../../api/festival';
import { eventApi } from '../../api/event';
import styles from '../tickets/TicketsPage.module.css';

export default function CuratorTicketsPage() {
  const [tab, setTab] = useState('festival');
  const [subTab, setSubTab] = useState('active');
  const [qrValue, setQrValue] = useState<string | null>(null);

  const { data: tickets, isLoading: ticketsLoading } = useQuery({
    queryKey: ['my-tickets'],
    queryFn: festivalApi.myTickets,
    enabled: tab === 'festival',
  });

  const { data: registrations, isLoading: regsLoading } = useQuery({
    queryKey: ['my-registrations'],
    queryFn: eventApi.myRegistrations,
    enabled: tab === 'event',
  });

  const activeTickets = (tickets ?? []).filter(t => t.status === 'ACTIVE');
  const expiredTickets = (tickets ?? []).filter(t => t.status !== 'ACTIVE');
  const festivalList = subTab === 'active' ? activeTickets : expiredTickets;
  const isLoading = tab === 'festival' ? ticketsLoading : regsLoading;

  return (
    <Layout>
      <h1 className={styles.title}>Мои билеты</h1>
      <Tabs
        tabs={[
          { key: 'festival', label: 'Фестивали' },
          { key: 'event', label: 'Мероприятия' },
        ]}
        activeKey={tab}
        onChange={t => { setTab(t); setSubTab('active'); }}
      />

      {tab === 'festival' && (
        <div style={{ marginTop: 12 }}>
          <Tabs
            tabs={[
              { key: 'active', label: 'Активные' },
              { key: 'expired', label: 'Истёкшие' },
            ]}
            activeKey={subTab}
            onChange={setSubTab}
          />
        </div>
      )}

      <div style={{ marginTop: 'var(--space-lg)' }}>
        {isLoading ? (
          <Spinner centered />
        ) : tab === 'festival' ? (
          !festivalList.length ? (
            <EmptyState icon={Ticket} message={subTab === 'active' ? 'Нет активных билетов' : 'Нет истёкших билетов'} />
          ) : (
            <div className={styles.list}>
              {festivalList.map(t => (
                <Card key={t.id} padding="md">
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16 }}>
                    <div>
                      <p style={{ fontWeight: 700, fontSize: 16 }}>{t.festivalName}</p>
                      <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
                        {t.ticketTypeName} · {new Date(t.startDate).toLocaleDateString('ru-RU')}
                      </p>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                      <Badge status={subTab === 'active' ? 'active' : 'expired'}>
                        {subTab === 'active' ? 'Активен' : 'Истёк'}
                      </Badge>
                      {subTab === 'active' && (
                        <Button size="sm" variant="secondary" onClick={() => setQrValue(t.qrToken ?? '')}>
                          QR
                        </Button>
                      )}
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          )
        ) : (
          !(registrations ?? []).length ? (
            <EmptyState icon={CalendarDays} message="Нет регистраций на мероприятия" />
          ) : (
            <div className={styles.list}>
              {(registrations ?? []).map(r => (
                <Card key={r.id} padding="md">
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16 }}>
                    <div>
                      <p style={{ fontWeight: 700, fontSize: 16 }}>{r.eventName}</p>
                      <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
                        {r.festivalName} · {new Date(r.eventDate).toLocaleDateString('ru-RU')}
                      </p>
                    </div>
                    <Button size="sm" variant="secondary" onClick={() => setQrValue(r.qrToken ?? '')}>
                      QR
                    </Button>
                  </div>
                </Card>
              ))}
            </div>
          )
        )}
      </div>

      <QRModal open={!!qrValue} onClose={() => setQrValue(null)} value={qrValue ?? ''} title="QR-код" />
    </Layout>
  );
}
