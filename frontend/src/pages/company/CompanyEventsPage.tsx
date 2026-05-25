import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { QrCode, Award, LayoutList } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import EmptyState from '../../components/ui/EmptyState';
import Spinner from '../../components/ui/Spinner';
import QRScanner from '../../components/QRScanner';
import { eventApi } from '../../api/event';
import styles from './CompanyPages.module.css';

export default function CompanyEventsPage() {
  const [scanModal, setScanModal] = useState(false);
  const [scanResult, setScanResult] = useState<{ valid: boolean; message: string } | null>(null);
  const [rewardModal, setRewardModal] = useState(false);
  const [selectedEventId, setSelectedEventId] = useState<string | null>(null);
  const [userNickname, setUserNickname] = useState('');
  const [rewardSearch, setRewardSearch] = useState<{ userId: string; rewardId: string } | null>(null);

  const { data: events, isLoading } = useQuery({
    queryKey: ['company-events'],
    queryFn: eventApi.myCompanyEvents,
  });

  const verifyMutation = useMutation({
    mutationFn: (token: string) => eventApi.verifyEventQr(token),
    onSuccess: () => setScanResult({ valid: true, message: '✅ Регистрация действительна' }),
    onError: () => setScanResult({ valid: false, message: '❌ Недействительный QR-код' }),
  });

  const distributeMutation = useMutation({
    mutationFn: () => eventApi.distributeReward(rewardSearch!.rewardId, rewardSearch!.userId),
    onSuccess: () => { setRewardModal(false); setRewardSearch(null); },
  });

  if (isLoading) return <Layout><Spinner centered /></Layout>;

  return (
    <Layout>
      <div className={styles.pageHeader}>
        <h1 className={styles.title}>Мои мероприятия</h1>
        <Button variant="secondary" onClick={() => setScanModal(true)}>
          <QrCode size={16} /> Сканер QR
        </Button>
      </div>

      {!events?.length ? (
        <EmptyState icon={LayoutList} message="Нет мероприятий" />
      ) : (
        <div className={styles.list}>
          {events.map(ev => (
            <Card key={ev.id} padding="md">
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16 }}>
                <div>
                  <p style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, fontSize: 16 }}>{ev.name}</p>
                  <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>{ev.festivalName} · {new Date(ev.startDate).toLocaleDateString('ru-RU')}</p>
                </div>
                <Button size="sm" variant="secondary" onClick={() => { setSelectedEventId(ev.id); setRewardModal(true); }}>
                  <Award size={14} /> Выдать награду
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* QR Scanner modal */}
      <Modal open={scanModal} onClose={() => { setScanModal(false); setScanResult(null); }} title="Сканер QR" size="md">
        {scanResult ? (
          <div style={{ textAlign: 'center', padding: '24px 0' }}>
            <p style={{ fontSize: 20, fontWeight: 700 }}>{scanResult.message}</p>
            <Button style={{ marginTop: 16 }} onClick={() => setScanResult(null)}>Сканировать ещё</Button>
          </div>
        ) : (
          <QRScanner onScan={text => verifyMutation.mutate(text)} />
        )}
      </Modal>

      {/* Reward modal */}
      <Modal open={rewardModal} onClose={() => { setRewardModal(false); setUserNickname(''); setRewardSearch(null); }} title="Выдать награду" size="sm">
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div>
            <label style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: 6 }}>Псевдоним участника</label>
            <input value={userNickname} onChange={e => setUserNickname(e.target.value)} placeholder="nickname" />
          </div>
          <Button disabled={!userNickname} loading={distributeMutation.isPending} fullWidth onClick={() => distributeMutation.mutate()}>
            Выдать
          </Button>
        </div>
      </Modal>
    </Layout>
  );
}
