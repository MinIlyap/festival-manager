import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Check, X, ExternalLink } from 'lucide-react';
import { Link } from 'react-router-dom';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import Badge from '../../components/ui/Badge';
import Spinner from '../../components/ui/Spinner';
import { applicationApi } from '../../api/application';
import { companyApi } from '../../api/company';
import { festivalApi } from '../../api/festival';
import styles from '../company/CompanyPages.module.css';

const typeLabels: Record<string, string> = {
  COMPANY_APPLICATION: 'Регистрация компании',
  FESTIVAL_APPLICATION: 'Заявка на фестиваль',
};

const statusLabels: Record<string, string> = {
  PENDING: 'На рассмотрении',
  APPROVED: 'Одобрена',
  REJECTED: 'Отклонена',
};

const statusMap: Record<string, 'pending' | 'approved' | 'rejected' | 'info'> = {
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected',
};

const festivalStatusLabels: Record<string, string> = {
  DRAFT: 'Черновик',
  PENDING: 'На рассмотрении',
  APPROVED: 'Одобрен',
  REJECTED: 'Отклонён',
};

export default function AdminApplicationDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [rejectModal, setRejectModal] = useState(false);
  const [reason, setReason] = useState('');

  const { data: app, isLoading } = useQuery({
    queryKey: ['admin-app', id],
    queryFn: () => applicationApi.getById(id!),
    enabled: !!id,
  });

  const isCompany = app?.applicationType === 'COMPANY_APPLICATION';
  const isFestival = app?.applicationType === 'FESTIVAL_APPLICATION';

  const { data: company, isLoading: companyLoading } = useQuery({
    queryKey: ['admin-company', app?.data?.companyId],
    queryFn: () => companyApi.getById(String(app!.data!.companyId)),
    enabled: isCompany && !!app?.data?.companyId,
  });

  const { data: festival, isLoading: festivalLoading } = useQuery({
    queryKey: ['admin-festival', app?.data?.festivalLocationId],
    queryFn: () => festivalApi.getById(String(app!.data!.festivalLocationId)),
    enabled: isFestival && !!app?.data?.festivalLocationId,
  });

  const approveMutation = useMutation({
    mutationFn: () => applicationApi.approve(id!),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['admin-applications'] }); navigate('/admin/applications'); },
  });

  const rejectMutation = useMutation({
    mutationFn: () => applicationApi.reject(id!, reason),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['admin-applications'] }); navigate('/admin/applications'); },
  });

  if (isLoading) return <Layout><Spinner centered /></Layout>;
  if (!app) return <Layout><p>Заявка не найдена</p></Layout>;

  const isPending = app.applicationStatus === 'PENDING';
  const detailsLoading = (isCompany && companyLoading) || (isFestival && festivalLoading);

  return (
    <Layout>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.title}>{typeLabels[app.applicationType] ?? app.applicationType}</h1>
          <p style={{ color: 'var(--text-muted)', marginTop: 4 }}>
            Заявка №{app.id} · {new Date(app.createdAt).toLocaleDateString('ru-RU')}
          </p>
        </div>
        {isPending && (
          <div style={{ display: 'flex', gap: 12 }}>
            <Button variant="danger" onClick={() => setRejectModal(true)}><X size={16} /> Отклонить</Button>
            <Button loading={approveMutation.isPending} onClick={() => approveMutation.mutate()}>
              <Check size={16} /> Одобрить
            </Button>
          </div>
        )}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
        <Card padding="lg">
          <h3 className={styles.sectionTitle}>Информация о заявке</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8, fontSize: 14 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <span>Статус:</span>
              <Badge status={statusMap[app.applicationStatus] ?? 'info'}>
                {statusLabels[app.applicationStatus] ?? app.applicationStatus}
              </Badge>
            </div>
            <p><b>Тип:</b> {typeLabels[app.applicationType] ?? app.applicationType}</p>
            <p><b>Дата подачи:</b> {new Date(app.createdAt).toLocaleString('ru-RU')}</p>
            <p><b>Обновлено:</b> {new Date(app.updatedAt).toLocaleString('ru-RU')}</p>
            {app.agentId && <p><b>Рассмотрел (ID):</b> {app.agentId}</p>}
            {app.data?.comment && (
              <div>
                <b>Комментарий к заявке:</b>
                <p style={{ marginTop: 4, color: 'var(--text-muted)' }}>{app.data.comment}</p>
              </div>
            )}
          </div>
        </Card>

        <Card padding="lg">
          <h3 className={styles.sectionTitle}>
            {isCompany ? 'Данные компании' : isFestival ? 'Данные фестиваля' : 'Данные заявки'}
          </h3>

          {detailsLoading && <Spinner />}

          {isCompany && company && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8, fontSize: 14 }}>
              <p><b>Полное название:</b> {company.fullName}</p>
              {company.brandName && <p><b>Бренд:</b> {company.brandName}</p>}
              <p><b>ИНН:</b> {company.tin}</p>
              <p><b>Email:</b> {company.email}</p>
              {company.website && (
                <p>
                  <b>Сайт:</b>{' '}
                  <a href={company.website} target="_blank" rel="noopener noreferrer" style={{ color: 'var(--accent-primary)' }}>
                    {company.website} <ExternalLink size={12} style={{ verticalAlign: 'middle' }} />
                  </a>
                </p>
              )}
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <b>Статус:</b>
                <Badge status={company.active ? 'approved' : 'pending'}>
                  {company.active ? 'Активна' : 'Неактивна'}
                </Badge>
              </div>
            </div>
          )}

          {isCompany && !company && !companyLoading && (
            <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>
              Компания ID {app.data?.companyId} — данные недоступны
            </p>
          )}

          {isFestival && festival && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8, fontSize: 14 }}>
              <p>
                <b>Название:</b>{' '}
                <Link to={`/festival/${festival.id}`} style={{ color: 'var(--accent-primary)' }}>
                  {festival.name} <ExternalLink size={12} style={{ verticalAlign: 'middle' }} />
                </Link>
              </p>
              {festival.description && <p><b>Описание:</b> {festival.description}</p>}
              <p><b>Город:</b> {festival.cityName}</p>
              {festival.platformName && <p><b>Площадка:</b> {festival.platformName}</p>}
              <p><b>Даты:</b> {new Date(festival.startDate).toLocaleDateString('ru-RU')} — {new Date(festival.endDate).toLocaleDateString('ru-RU')}</p>
              <p><b>Возрастной рейтинг:</b> {festival.ageRating}+</p>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <b>Статус фестиваля:</b>
                <Badge status={statusMap[festival.status] ?? 'info'}>
                  {festivalStatusLabels[festival.status] ?? festival.status}
                </Badge>
              </div>
            </div>
          )}

          {isFestival && !festival && !festivalLoading && (
            <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>
              Фестиваль ID {app.data?.festivalLocationId} — данные недоступны
            </p>
          )}

          {!isCompany && !isFestival && (
            <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Дополнительные данные отсутствуют</p>
          )}
        </Card>
      </div>

      <Modal open={rejectModal} onClose={() => setRejectModal(false)} title="Причина отказа" size="sm">
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <textarea value={reason} onChange={e => setReason(e.target.value)} placeholder="Укажите причину отказа..." rows={4} />
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <Button variant="secondary" onClick={() => setRejectModal(false)}>Отмена</Button>
            <Button variant="danger" disabled={!reason.trim()} loading={rejectMutation.isPending} onClick={() => rejectMutation.mutate()}>Отклонить</Button>
          </div>
        </div>
      </Modal>
    </Layout>
  );
}
