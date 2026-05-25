import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { CheckCircle, XCircle } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import QRScanner from '../../components/QRScanner';
import { festivalApi } from '../../api/festival';
import styles from '../company/CompanyPages.module.css';

type ScanResult = { valid: boolean; message: string };

export default function CuratorScannerPage() {
  const [result, setResult] = useState<ScanResult | null>(null);

  const verifyMutation = useMutation({
    mutationFn: (token: string) => festivalApi.verifyTicketQr(token),
    onSuccess: () => setResult({ valid: true, message: 'Билет действителен' }),
    onError: () => setResult({ valid: false, message: 'Недействительный билет' }),
  });

  const reset = () => setResult(null);

  return (
    <Layout>
      <h1 className={styles.title}>Сканер билетов</h1>
      <div style={{ maxWidth: 480, margin: '0 auto', marginTop: 'var(--space-lg)' }}>
        {result ? (
          <Card padding="lg">
            <div style={{ textAlign: 'center', padding: '24px 0' }}>
              {result.valid ? (
                <CheckCircle size={64} style={{ color: 'var(--accent-success)', margin: '0 auto' }} />
              ) : (
                <XCircle size={64} style={{ color: 'var(--accent-danger)', margin: '0 auto' }} />
              )}
              <p style={{ fontSize: 22, fontWeight: 700, marginTop: 16, color: result.valid ? 'var(--accent-success)' : 'var(--accent-danger)' }}>
                {result.message}
              </p>
              <Button style={{ marginTop: 24 }} onClick={reset}>
                Сканировать ещё
              </Button>
            </div>
          </Card>
        ) : (
          <Card padding="lg">
            <p style={{ fontSize: 14, color: 'var(--text-muted)', marginBottom: 16, textAlign: 'center' }}>
              Наведите камеру на QR-код билета
            </p>
            <QRScanner onScan={token => verifyMutation.mutate(token)} />
          </Card>
        )}
      </div>
    </Layout>
  );
}
