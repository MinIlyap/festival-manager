import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Star, MapPin, Calendar, Trophy } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import EmptyState from '../../components/ui/EmptyState';
import { festivalApi } from '../../api/festival';
import styles from './CuratorFestivalsPage.module.css';

function fmtDate(iso: string) {
  return new Date(iso).toLocaleDateString('ru-RU', { day: 'numeric', month: 'short', year: 'numeric' });
}

function FestivalCard({ festival }: { festival: { id: string; name: string; cityName: string; platformName?: string; address?: string; startDate: string; endDate: string; ageRating: number } }) {
  const navigate = useNavigate();
  const [imgErr, setImgErr] = useState(false);
  const location = [festival.cityName, festival.platformName].filter(Boolean).join(' · ');

  return (
    <div className={styles.card} onClick={() => navigate(`/festival/${festival.id}`)}>
      <div className={styles.cardCover}>
        {!imgErr
          ? <img src={`/api/files/FESTIVAL_LOCATION/${festival.id}`} alt={festival.name} onError={() => setImgErr(true)} />
          : <div className={styles.cardCoverPlaceholder}><Trophy size={32} /></div>
        }
        <Badge status="info" className={styles.ageBadge}>{festival.ageRating}+</Badge>
      </div>
      <div className={styles.cardBody}>
        <h3 className={styles.cardName}>{festival.name}</h3>
        <div className={styles.cardMeta}>
          {location && (
            <span className={styles.cardMetaRow}>
              <MapPin size={12} /> {location}
            </span>
          )}
          {festival.address && (
            <span className={styles.cardMetaRow} style={{ paddingLeft: 16 }}>{festival.address}</span>
          )}
          <span className={styles.cardMetaRow}>
            <Calendar size={12} /> {fmtDate(festival.startDate)} — {fmtDate(festival.endDate)}
          </span>
        </div>
        <Button
          variant="secondary"
          size="sm"
          fullWidth
          onClick={e => { e.stopPropagation(); navigate(`/festival/${festival.id}`); }}
        >
          Перейти к фестивалю
        </Button>
      </div>
    </div>
  );
}

export default function CuratorFestivalsPage() {
  const { data: festivals, isLoading } = useQuery({
    queryKey: ['curator-festivals'],
    queryFn: festivalApi.myCuratorFestivals,
  });

  if (isLoading) return <Layout><Spinner centered /></Layout>;

  const approved = (festivals ?? []).filter(f => f.status === 'APPROVED');

  return (
    <Layout>
      <h1 className={styles.title}>Курируемые фестивали</h1>
      {!approved.length ? (
        <EmptyState
          icon={Star}
          message="Нет опубликованных фестивалей"
          description="Вы не являетесь куратором ни одного опубликованного фестиваля"
        />
      ) : (
        <div className={styles.grid}>
          {approved.map(f => <FestivalCard key={f.id} festival={f} />)}
        </div>
      )}
    </Layout>
  );
}
