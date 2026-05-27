import { useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, Package } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import Spinner from '../../components/ui/Spinner';
import { eventApi } from '../../api/event';
import type { Stand } from '../../types';
import styles from './StandPage.module.css';

function ProductPhoto({ id, name }: { id: number; name: string }) {
  const [err, setErr] = useState(false);
  if (err) return null;
  return (
    <img
      src={`/api/files/PRODUCT_INSTANCE/${id}`}
      alt={name}
      className={styles.productPhoto}
      onError={() => setErr(true)}
    />
  );
}

interface StandLocationState {
  stand?: Stand;
  festivalName?: string;
  festivalId?: string;
}

export default function StandPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { state } = useLocation() as { state: StandLocationState | null };

  const stand = state?.stand;
  const festivalId = state?.festivalId;
  const festivalName = state?.festivalName;

  const [imgErr, setImgErr] = useState(false);

  const { data: products = [], isLoading: productsLoading } = useQuery({
    queryKey: ['stand-products', id],
    queryFn: () => eventApi.getProductsByStand(id!),
    enabled: !!id && stand?.type === 'COMMERCIAL',
  });

  if (!stand) {
    return (
      <Layout>
        <button className={styles.backBtn} onClick={() => navigate(-1)}>
          <ArrowLeft size={16} /> Назад
        </button>
        <p style={{ color: 'var(--text-muted)' }}>Стенд не найден. Перейдите к нему со страницы фестиваля.</p>
      </Layout>
    );
  }

  const handleBack = () => {
    if (festivalId) navigate(`/festival/${festivalId}`);
    else navigate(-1);
  };

  return (
    <Layout>
      {/* Back */}
      <button className={styles.backBtn} onClick={handleBack}>
        <ArrowLeft size={16} />
        {festivalName ? `К фестивалю «${festivalName}»` : 'Назад'}
      </button>

      {/* Photo */}
      {!imgErr && (
        <div className={styles.photoWrapper}>
          <img
            src={`/api/files/FESTIVAL_STAND/${id}`}
            alt={stand.name}
            className={styles.photo}
            onError={() => setImgErr(true)}
          />
        </div>
      )}

      {/* Header */}
      <div className={styles.header}>
        <Badge status={stand.type === 'COMMERCIAL' ? 'active' : 'info'}>
          {stand.type === 'COMMERCIAL' ? 'Коммерческий стенд' : 'Демонстрационный стенд'}
        </Badge>
        <h1 className={styles.title}>{stand.name}</h1>
        {stand.companyName && <p className={styles.company}>{stand.companyName}</p>}
        {festivalName && <p className={styles.festivalRef}>Фестиваль: {festivalName}</p>}
      </div>

      {/* Description */}
      {stand.description && (
        <Card padding="lg" className={styles.descCard}>
          <p className={styles.descLabel}>Описание</p>
          <p className={styles.descText}>{stand.description}</p>
        </Card>
      )}

      {/* Products — commercial only */}
      {stand.type === 'COMMERCIAL' && (
        <div className={styles.productsSection}>
          <h2 className={styles.sectionTitle}>
            <Package size={20} /> Товары
          </h2>
          {productsLoading ? (
            <Spinner />
          ) : products.length === 0 ? (
            <p className={styles.emptyText}>Товары не добавлены</p>
          ) : (
            <div className={styles.productsGrid}>
              {products.map(p => (
                <Card key={p.productInstanceId} padding="md">
                  <ProductPhoto id={p.productInstanceId} name={p.productName} />
                  <p className={styles.productName}>{p.productName}</p>
                  {p.description && <p className={styles.productDesc}>{p.description}</p>}
                  <div className={styles.productFooter}>
                    <span className={styles.productPrice}>
                      {p.price.toLocaleString('ru-RU')} ₽
                    </span>
                    <span className={styles.productQty}>В наличии: {p.quantity}</span>
                  </div>
                </Card>
              ))}
            </div>
          )}
        </div>
      )}
    </Layout>
  );
}
