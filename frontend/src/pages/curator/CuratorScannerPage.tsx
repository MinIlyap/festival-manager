import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { CheckCircle, XCircle, Camera, Star } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import EmptyState from '../../components/ui/EmptyState';
import QRScanner from '../../components/QRScanner';
import { festivalApi } from '../../api/festival';
import type { QrVerifyResponse } from '../../types';
import styles from '../company/CompanyPages.module.css';

const STATUS_INFO: Record<QrVerifyResponse['status'], { valid: boolean; message: string }> = {
  SUCCESS:           { valid: true,  message: 'Билет действителен' },
  EXPIRED:           { valid: false, message: 'Срок действия билета истёк' },
  FORGED:            { valid: false, message: 'QR-код недействителен (подделка)' },
  INVALID:           { valid: false, message: 'Недействительный QR-код' },
  LOCATION_MISMATCH: { valid: false, message: 'Билет не относится к этому фестивалю' },
};

type ScanResult = { valid: boolean; message: string };

export default function CuratorScannerPage() {
  const [result, setResult] = useState<ScanResult | null>(null);
  const [festivalId, setFestivalId] = useState<string | null>(null);

  const { data: festivals, isLoading } = useQuery({
    queryKey: ['curator-festivals'],
    queryFn: festivalApi.myCuratorFestivals,
  });

  const approved = (festivals ?? []).filter(f => f.status === 'APPROVED');
  const activeFestivalId = festivalId ?? (approved.length === 1 ? String(approved[0].id) : null);

  const verifyMutation = useMutation({
    mutationFn: (token: string) =>
      festivalApi.verifyTicketQr(token, activeFestivalId ?? '0'),
    onSuccess: (data) => {
      const info = STATUS_INFO[data.status] ?? { valid: false, message: `Неизвестный статус: ${data.status}` };
      setResult(info);
    },
    onError: () => setResult({ valid: false, message: 'Ошибка при проверке билета' }),
  });

  const reset = () => setResult(null);

  if (isLoading) return <Layout><Spinner centered /></Layout>;

  if (!approved.length) {
    return (
      <Layout>
        <h1 className={styles.title}>Сканер билетов</h1>
        <EmptyState
          icon={Star}
          message="Нет назначенных фестивалей"
          description="Вы не являетесь куратором ни одного опубликованного фестиваля"
        />
      </Layout>
    );
  }

  return (
    <Layout>
      <h1 className={styles.title}>Сканер билетов</h1>
      <div style={{ maxWidth: 480, margin: '0 auto', marginTop: 'var(--space-lg)' }}>

        {/* Festival selector (only if multiple and not yet selected) */}
        {approved.length > 1 && !festivalId && (
          <Card padding="lg">
            <p style={{ fontWeight: 600, marginBottom: 12 }}>Выберите фестиваль для сканирования:</p>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {approved.map(f => (
                <Button key={f.id} variant="secondary" onClick={() => setFestivalId(String(f.id))}>
                  {f.name}
                </Button>
              ))}
            </div>
          </Card>
        )}

        {/* Scanner / Result */}
        {activeFestivalId && (
          result ? (
            <Card padding="lg">
              <div style={{ textAlign: 'center', padding: '24px 0' }}>
                {result.valid
                  ? <CheckCircle size={64} style={{ color: 'var(--accent-success)', margin: '0 auto' }} />
                  : <XCircle   size={64} style={{ color: 'var(--accent-danger)',  margin: '0 auto' }} />
                }
                <p style={{
                  fontSize: 22, fontWeight: 700, marginTop: 16,
                  color: result.valid ? 'var(--accent-success)' : 'var(--accent-danger)',
                }}>
                  {result.message}
                </p>
                <Button style={{ marginTop: 24 }} onClick={reset}>
                  Сканировать ещё
                </Button>
              </div>
            </Card>
          ) : (
            <Card padding="lg">
              <p style={{ fontSize: 14, color: 'var(--text-muted)', marginBottom: 4, textAlign: 'center' }}>
                Наведите камеру на QR-код билета
              </p>
              {approved.length > 1 && (
                <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 16, textAlign: 'center' }}>
                  <Camera size={12} style={{ verticalAlign: 'middle', marginRight: 4 }} />
                  {approved.find(f => String(f.id) === activeFestivalId)?.name}
                  {' · '}
                  <button
                    onClick={() => setFestivalId(null)}
                    style={{ color: 'var(--accent-primary)', background: 'none', border: 'none', cursor: 'pointer', fontSize: 13 }}
                  >
                    изменить
                  </button>
                </p>
              )}
              <QRScanner onScan={token => verifyMutation.mutate(token)} />
            </Card>
          )
        )}

      </div>
    </Layout>
  );
}
