import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { ClipboardList, Building2, CalendarDays } from 'lucide-react';
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

function AppRow({ app }: { app: Application }) {
  const navigate = useNavigate();
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

  const handleClick = () => {
    if (isFestival && app.data?.festivalLocationId) {
      navigate(`/festival/${app.data.festivalLocationId}?adminReview=${app.id}`);
    } else {
      navigate(`/admin/applications/${app.id}`);
    }
  };

  return (
    <Card hoverable padding="md" onClick={handleClick}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
          <div style={{ color: 'var(--accent-primary)', flexShrink: 0 }}>
            {isCompany ? <Building2 size={22} /> : <CalendarDays size={22} />}
          </div>
          <div>
            <p style={{ fontFamily: 'var(--font-body)', fontWeight: 700, fontSize: 16, color: 'var(--text-primary)' }}>
              {title}
            </p>
            <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
              Заявка №{app.id} · {new Date(app.updatedAt).toLocaleString('ru-RU')}
            </p>
          </div>
        </div>
        <Badge status="pending">На рассмотрении</Badge>
      </div>
    </Card>
  );
}

export default function AdminApplicationsPage() {
  const [tab, setTab] = useState<'company' | 'festival'>('company');

  const { data, isLoading } = useQuery({
    queryKey: ['admin-applications'],
    queryFn: () => applicationApi.adminList({ status: 'PENDING', page: 0, size: 50 }),
  });

  if (isLoading) return <Layout><Spinner centered /></Layout>;

  const all = data?.content ?? [];
  const companyApps = all.filter(a => a.applicationType === 'COMPANY_APPLICATION');
  const festivalApps = all.filter(a => a.applicationType === 'FESTIVAL_APPLICATION');
  const shown = tab === 'company' ? companyApps : festivalApps;

  return (
    <Layout>
      <h1 className={styles.title}>Заявки на рассмотрение</h1>
      <Tabs
        tabs={[
          { key: 'company',  label: <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}><Building2 size={15} />Заявки на компанию</span> },
          { key: 'festival', label: <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}><CalendarDays size={15} />Заявки на фестиваль</span> },
        ]}
        activeKey={tab}
        onChange={k => setTab(k as 'company' | 'festival')}
      />
      {!shown.length ? (
        <EmptyState
          icon={tab === 'company' ? Building2 : CalendarDays}
          message={tab === 'company' ? 'Нет заявок на регистрацию компаний' : 'Нет заявок на фестивали'}
        />
      ) : (
        <div className={styles.list} style={{ marginTop: 'var(--space-lg)' }}>
          {shown.map(app => (
            <AppRow key={app.id} app={app} />
          ))}
        </div>
      )}
    </Layout>
  );
}
