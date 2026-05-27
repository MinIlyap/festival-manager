import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Plus, Trophy, BarChart2 } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import EmptyState from '../../components/ui/EmptyState';
import Spinner from '../../components/ui/Spinner';
import { festivalApi } from '../../api/festival';
import { applicationApi } from '../../api/application';
import type { Festival, Application } from '../../types';
import styles from './CompanyPages.module.css';

type EffectiveStatus = 'DRAFT' | 'PENDING' | 'REJECTED' | 'APPROVED';

function getEffectiveStatus(festival: Festival, apps: Application[]): { status: EffectiveStatus; app: Application | undefined } {
  const app = apps.find(
    a => a.applicationType === 'FESTIVAL_APPLICATION' && String(a.data?.festivalLocationId) === String(festival.id)
  );
  if (!app) return { status: 'DRAFT', app: undefined };
  return { status: app.applicationStatus as EffectiveStatus, app };
}

function FestivalCard({ festival, apps }: { festival: Festival; apps: Application[] }) {
  const navigate = useNavigate();
  const [imgErr, setImgErr] = useState(false);

  const { status, app } = getEffectiveStatus(festival, apps);

  const badgeStatus: Record<EffectiveStatus, 'draft' | 'pending' | 'rejected' | 'approved'> = {
    DRAFT: 'draft', PENDING: 'pending', REJECTED: 'rejected', APPROVED: 'approved',
  };
  const badgeLabel: Record<EffectiveStatus, string> = {
    DRAFT: 'Черновик', PENDING: 'На рассмотрении', REJECTED: 'Отклонена', APPROVED: 'Опубликован',
  };

  return (
    <Card hoverable={false} padding="none">
      <div className={styles.festCover}>
        {!imgErr
          ? <img src={`/api/files/FESTIVAL_LOCATION/${festival.id}`} alt={festival.name} onError={() => setImgErr(true)} />
          : <div className={styles.festCoverPlaceholder}><Trophy size={28} /></div>
        }
        {status !== 'APPROVED' && (
          <Badge status={badgeStatus[status]} className={styles.statusBadge}>{badgeLabel[status]}</Badge>
        )}
        {status === 'APPROVED' && (
          <Badge status="approved" className={styles.statusBadge}>Опубликован</Badge>
        )}
      </div>

      <div className={styles.festBody}>
        <h3 className={styles.festName}>{festival.name}</h3>
        <p className={styles.festDate}>{festival.startDate?.split('-').reverse().join('.')} — {festival.endDate?.split('-').reverse().join('.')}</p>

        {status === 'REJECTED' && app?.data?.comment && (
          <details style={{ marginTop: 6 }}>
            <summary style={{ fontSize: 12, color: 'var(--accent-danger)', cursor: 'pointer', userSelect: 'none' }}>
              Причина отказа
            </summary>
            <p style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 4, lineHeight: 1.5 }}>
              {app.data.comment}
            </p>
          </details>
        )}

        <div className={styles.festActions} style={{ marginTop: 8 }}>
          <Button variant="secondary" size="sm" onClick={() => navigate(`/festival/${festival.id}`)}>
            Просмотреть
          </Button>

          {status !== 'APPROVED' && (
            <Button size="sm" onClick={() => navigate(`/company/festivals/${festival.id}/edit`)}>
              Редактировать
            </Button>
          )}

          {status === 'APPROVED' && (
            <Button size="sm" onClick={() => navigate(`/company/festivals/${festival.id}/edit`)}>
              Управление
            </Button>
          )}

          <Button
            variant="secondary"
            size="sm"
            onClick={() => navigate(`/company/festivals/${festival.id}/analytics`)}
          >
            <BarChart2 size={14} /> Аналитика
          </Button>
        </div>
      </div>
    </Card>
  );
}

export default function CompanyFestivalsPage() {
  const navigate = useNavigate();

  const { data: festivals, isLoading: festivalsLoading } = useQuery({
    queryKey: ['my-company-festivals'],
    queryFn: festivalApi.myCompanyFestivals,
  });

  const { data: applications = [], isLoading: appsLoading } = useQuery({
    queryKey: ['my-applications'],
    queryFn: applicationApi.myApplications,
  });

  const isLoading = festivalsLoading || appsLoading;

  return (
    <Layout>
      <div className={styles.pageHeader}>
        <h1 className={styles.title}>Мои фестивали</h1>
        <Button onClick={() => navigate('/company/festivals/new')}><Plus size={16} /> Создать фестиваль</Button>
      </div>

      {isLoading ? <Spinner centered /> :
       !festivals?.length ? (
         <EmptyState icon={Trophy} message="Нет фестивалей" action={{ label: 'Создать первый', onClick: () => navigate('/company/festivals/new') }} />
       ) : (
        <div className={styles.grid}>
          {festivals.map(f => (
            <FestivalCard key={f.id} festival={f} apps={applications} />
          ))}
        </div>
      )}
    </Layout>
  );
}
