import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { CheckCircle, XCircle } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import Tabs from '../../components/ui/Tabs';
import EmptyState from '../../components/ui/EmptyState';
import Spinner from '../../components/ui/Spinner';
import { applicationApi } from '../../api/application';
import styles from '../company/CompanyPages.module.css';

const typeLabels: Record<string, string> = {
  COMPANY_APPLICATION: 'Регистрация компании',
  FESTIVAL_APPLICATION: 'Заявка на фестиваль',
};

export default function AdminHistoryPage() {
  const [tab, setTab] = useState('approved');

  const { data = [], isLoading } = useQuery({
    queryKey: ['admin-history'],
    queryFn: () => applicationApi.adminHistory(),
  });

  const approved = data.filter(a => a.applicationStatus === 'APPROVED');
  const rejected = data.filter(a => a.applicationStatus === 'REJECTED');
  const list = tab === 'approved' ? approved : rejected;

  return (
    <Layout>
      <h1 className={styles.title}>История заявок</h1>
      <Tabs
        tabs={[
          { key: 'approved', label: 'Одобренные' },
          { key: 'rejected', label: 'Отклонённые' },
        ]}
        activeKey={tab}
        onChange={setTab}
      />
      <div style={{ marginTop: 'var(--space-lg)' }}>
        {isLoading ? (
          <Spinner centered />
        ) : !list.length ? (
          <EmptyState
            icon={tab === 'approved' ? CheckCircle : XCircle}
            message={tab === 'approved' ? 'Нет одобренных заявок' : 'Нет отклонённых заявок'}
          />
        ) : (
          <div className={styles.list}>
            {list.map(app => (
              <Card key={app.id} padding="md">
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16 }}>
                  <div>
                    <p style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, fontSize: 16 }}>
                      {typeLabels[app.applicationType] ?? app.applicationType}
                    </p>
                    <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
                      Заявка #{app.id} · {new Date(app.createdAt).toLocaleDateString('ru-RU')}
                    </p>
                    {app.data?.comment && (
                      <p style={{ fontSize: 13, color: 'var(--accent-danger)', marginTop: 4 }}>
                        Причина: {app.data.comment}
                      </p>
                    )}
                  </div>
                  <Badge status={tab === 'approved' ? 'approved' : 'rejected'}>
                    {tab === 'approved' ? 'Одобрена' : 'Отклонена'}
                  </Badge>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
}
