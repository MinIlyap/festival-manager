import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ClipboardList } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Tabs from '../../components/ui/Tabs';
import EmptyState from '../../components/ui/EmptyState';
import Spinner from '../../components/ui/Spinner';
import { applicationApi } from '../../api/application';
import styles from './CompanyPages.module.css';

const statusLabel: Record<string, string> = { PENDING: 'На рассмотрении', APPROVED: 'Одобрена', REJECTED: 'Отклонена' };
const statusBadge: Record<string, 'pending' | 'approved' | 'rejected'> = { PENDING: 'pending', APPROVED: 'approved', REJECTED: 'rejected' };

export default function CompanyApplicationsPage() {
  const navigate = useNavigate();
  const [tab, setTab] = useState('active');

  const { data, isLoading } = useQuery({
    queryKey: ['my-applications'],
    queryFn: applicationApi.myApplications,
  });

  const active = (data ?? []).filter(a => a.status === 'PENDING' || a.status === 'REJECTED');
  const closed = (data ?? []).filter(a => a.status === 'APPROVED');

  const list = tab === 'active' ? active : closed;

  return (
    <Layout>
      <h1 className={styles.title}>Мои заявки</h1>
      <Tabs
        tabs={[{ key: 'active', label: 'Активные' }, { key: 'closed', label: 'Закрытые' }]}
        activeKey={tab}
        onChange={setTab}
      />
      <div style={{ marginTop: 'var(--space-lg)' }}>
        {isLoading ? <Spinner centered /> :
         !list.length ? <EmptyState icon={ClipboardList} message={tab === 'active' ? 'Нет активных заявок' : 'Нет закрытых заявок'} /> : (
          <div className={styles.list}>
            {list.map(app => (
              <Card key={app.id} hoverable padding="md" onClick={() => navigate(`/festival/${app.festivalId}`)}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16 }}>
                  <div>
                    <p style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, fontSize: 16 }}>{app.festivalName}</p>
                    <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>Подано: {new Date(app.createdAt).toLocaleDateString('ru-RU')}</p>
                    {app.rejectReason && (
                      <details style={{ marginTop: 8 }}>
                        <summary style={{ fontSize: 13, color: 'var(--accent-danger)', cursor: 'pointer' }}>Причина отказа</summary>
                        <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>{app.rejectReason}</p>
                      </details>
                    )}
                  </div>
                  <Badge status={statusBadge[app.status] ?? 'info'}>{statusLabel[app.status]}</Badge>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
}
