import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { ClipboardList } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import EmptyState from '../../components/ui/EmptyState';
import Spinner from '../../components/ui/Spinner';
import { applicationApi } from '../../api/application';
import styles from '../company/CompanyPages.module.css';

const typeLabels: Record<string, string> = {
  COMPANY_APPLICATION: 'Регистрация компании',
  FESTIVAL_APPLICATION: 'Заявка на фестиваль',
};

export default function AdminApplicationsPage() {
  const navigate = useNavigate();
  const { data, isLoading } = useQuery({
    queryKey: ['admin-applications'],
    queryFn: () => applicationApi.adminList({ status: 'PENDING', page: 0, size: 50 }),
  });

  if (isLoading) return <Layout><Spinner centered /></Layout>;

  return (
    <Layout>
      <h1 className={styles.title}>Заявки на рассмотрении</h1>
      {!data?.content.length ? (
        <EmptyState icon={ClipboardList} message="Нет заявок на рассмотрении" />
      ) : (
        <div className={styles.list} style={{ marginTop: 'var(--space-lg)' }}>
          {data.content.map(app => (
            <Card key={app.id} hoverable padding="md" onClick={() => navigate(`/admin/applications/${app.id}`)}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16 }}>
                <div>
                  <p style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, fontSize: 16 }}>
                    {typeLabels[app.applicationType] ?? app.applicationType}
                  </p>
                  <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
                    Заявка #{app.id} · {new Date(app.createdAt).toLocaleDateString('ru-RU')}
                  </p>
                  {app.data?.companyId && (
                    <p style={{ fontSize: 13, color: 'var(--text-muted)' }}>Компания ID: {app.data.companyId}</p>
                  )}
                  {app.data?.festivalLocationId && (
                    <p style={{ fontSize: 13, color: 'var(--text-muted)' }}>Площадка ID: {app.data.festivalLocationId}</p>
                  )}
                </div>
                <Badge status="pending">На рассмотрении</Badge>
              </div>
            </Card>
          ))}
        </div>
      )}
    </Layout>
  );
}
