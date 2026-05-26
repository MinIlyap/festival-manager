import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { CheckCircle, XCircle, Building2, CalendarDays } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import Tabs from '../../components/ui/Tabs';
import EmptyState from '../../components/ui/EmptyState';
import Spinner from '../../components/ui/Spinner';
import { applicationApi } from '../../api/application';
import { companyApi } from '../../api/company';
import { festivalApi } from '../../api/festival';
import type { Application } from '../../types';
import styles from '../company/CompanyPages.module.css';

function HistoryRow({ app, tab }: { app: Application; tab: string }) {
  const isCompany = app.applicationType === 'COMPANY_APPLICATION';
  const isFestival = app.applicationType === 'FESTIVAL_APPLICATION';

  const { data: company } = useQuery({
    queryKey: ['app-company', app.data?.companyId],
    queryFn: () => companyApi.getById(String(app.data!.companyId)),
    enabled: isCompany && !!app.data?.companyId,
  });

  const { data: festival } = useQuery({
    queryKey: ['app-festival', app.data?.festivalLocationId],
    queryFn: () => festivalApi.getById(String(app.data!.festivalLocationId)),
    enabled: isFestival && !!app.data?.festivalLocationId,
  });

  const companyName = company ? (company.brandName || company.fullName) : null;
  const festivalName = festival ? festival.name : null;

  const title = isCompany
    ? `Регистрация компании${companyName ? ` «${companyName}»` : ' …'}`
    : isFestival
    ? `Утверждение фестиваля${festivalName ? ` «${festivalName}»` : ' …'}`
    : app.applicationType;

  return (
    <Card padding="md">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
          <div style={{ color: 'var(--accent-primary)', flexShrink: 0 }}>
            {isCompany ? <Building2 size={22} /> : <CalendarDays size={22} />}
          </div>
          <div>
            <p style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, fontSize: 16, color: 'var(--text-primary)' }}>
              {title}
            </p>
            <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
              Заявка #{app.id} · {new Date(app.updatedAt).toLocaleString('ru-RU')}
            </p>
            {app.data?.comment && tab === 'rejected' && (
              <p style={{ fontSize: 13, color: 'var(--accent-danger)', marginTop: 4 }}>
                Причина: {app.data.comment}
              </p>
            )}
          </div>
        </div>
        <Badge status={tab === 'approved' ? 'approved' : 'rejected'}>
          {tab === 'approved' ? 'Одобрена' : 'Отклонена'}
        </Badge>
      </div>
    </Card>
  );
}

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
              <HistoryRow key={app.id} app={app} tab={tab} />
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
}
