import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Plus, Trophy } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import EmptyState from '../../components/ui/EmptyState';
import Spinner from '../../components/ui/Spinner';
import { festivalApi } from '../../api/festival';
import styles from './CompanyPages.module.css';

const statusLabel: Record<string, string> = { DRAFT: 'Черновик', PENDING: 'На рассмотрении', APPROVED: 'Одобрен', REJECTED: 'Отклонён' };
const statusBadge: Record<string, 'draft' | 'pending' | 'approved' | 'rejected'> = { DRAFT: 'draft', PENDING: 'pending', APPROVED: 'approved', REJECTED: 'rejected' };

export default function CompanyFestivalsPage() {
  const navigate = useNavigate();
  const { data: festivals, isLoading } = useQuery({
    queryKey: ['my-company-festivals'],
    queryFn: festivalApi.myCompanyFestivals,
  });

  return (
    <Layout>
      <div className={styles.pageHeader}>
        <h1 className={styles.title}>Мои фестивали</h1>
        <Button onClick={() => navigate('/company/festivals/new')}><Plus size={16} /> Создать фестиваль</Button>
      </div>

      {isLoading ? <Spinner centered /> :
       !festivals?.length ? <EmptyState icon={Trophy} message="Нет фестивалей" action={{ label: 'Создать первый', onClick: () => navigate('/company/festivals/new') }} /> : (
        <div className={styles.grid}>
          {festivals.map(f => (
            <Card key={f.id} hoverable padding="none">
              <div className={styles.festCover}>
                {f.coverUrl ? <img src={f.coverUrl} alt={f.name} /> : <div className={styles.festCoverPlaceholder}><Trophy size={28} /></div>}
                <Badge status={statusBadge[f.status] ?? 'info'} className={styles.statusBadge}>{statusLabel[f.status]}</Badge>
              </div>
              <div className={styles.festBody}>
                <h3 className={styles.festName}>{f.name}</h3>
                <p className={styles.festDate}>{f.startDate?.split('-').reverse().join('.')} — {f.endDate?.split('-').reverse().join('.')}</p>
                <div className={styles.festActions}>
                  <Button variant="secondary" size="sm" onClick={() => navigate(`/festival/${f.id}`)}>Просмотреть</Button>
                  <Button size="sm" onClick={() => navigate(`/company/festivals/${f.id}/edit`)}>
                    {f.status === 'APPROVED' ? 'Управление' : 'Редактировать'}
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </Layout>
  );
}
