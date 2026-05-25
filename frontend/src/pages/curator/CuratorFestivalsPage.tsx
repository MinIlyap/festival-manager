import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Star } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import EmptyState from '../../components/ui/EmptyState';
import Spinner from '../../components/ui/Spinner';
import { festivalApi } from '../../api/festival';
import styles from '../company/CompanyPages.module.css';

const statusLabel: Record<string, string> = {
  DRAFT: 'Черновик',
  PENDING: 'На рассмотрении',
  APPROVED: 'Одобрен',
  REJECTED: 'Отклонён',
};

const statusBadge: Record<string, 'draft' | 'pending' | 'approved' | 'rejected'> = {
  DRAFT: 'draft',
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected',
};

export default function CuratorFestivalsPage() {
  const navigate = useNavigate();

  const { data: festivals, isLoading } = useQuery({
    queryKey: ['curator-festivals'],
    queryFn: festivalApi.myCuratorFestivals,
  });

  if (isLoading) return <Layout><Spinner centered /></Layout>;

  return (
    <Layout>
      <h1 className={styles.title}>Мои фестивали</h1>
      {!festivals?.length ? (
        <EmptyState icon={Star} message="Вы не назначены куратором ни одного фестиваля" />
      ) : (
        <div className={styles.list} style={{ marginTop: 'var(--space-lg)' }}>
          {festivals.map(f => (
            <Card key={f.id} hoverable padding="md" onClick={() => navigate(`/festival/${f.id}`)}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16 }}>
                <div>
                  <p style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, fontSize: 16 }}>{f.name}</p>
                  <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
                    {f.cityName} · {new Date(f.startDate).toLocaleDateString('ru-RU')} — {new Date(f.endDate).toLocaleDateString('ru-RU')}
                  </p>
                </div>
                <Badge status={statusBadge[f.status] ?? 'info'}>{statusLabel[f.status] ?? f.status}</Badge>
              </div>
            </Card>
          ))}
        </div>
      )}
    </Layout>
  );
}
