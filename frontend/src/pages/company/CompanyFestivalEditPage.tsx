import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { Info, Building2, Users, CalendarDays, Store, Ticket, Image, Send } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import { festivalApi } from '../../api/festival';
import { FestivalInfoTab } from './tabs/FestivalInfoTab';
import { FestivalOrganizersTab } from './tabs/FestivalOrganizersTab';
import { FestivalCuratorsTab } from './tabs/FestivalCuratorsTab';
import { FestivalEventsTab } from './tabs/FestivalEventsTab';
import { FestivalStandsTab } from './tabs/FestivalStandsTab';
import { FestivalTicketsTab } from './tabs/FestivalTicketsTab';
import { FestivalPhotosTab } from './tabs/FestivalPhotosTab';
import { fmtDate } from './tabs/shared';
import styles from './CompanyPages.module.css';

const TABS = [
  { key: 'info',      label: 'Общая информация',      icon: Info },
  { key: 'companies', label: 'Компании-организаторы',  icon: Building2 },
  { key: 'curators',  label: 'Кураторы',               icon: Users },
  { key: 'events',    label: 'Мероприятия',            icon: CalendarDays },
  { key: 'stands',    label: 'Стенды',                 icon: Store },
  { key: 'tickets',   label: 'Типы билетов',           icon: Ticket },
  { key: 'photos',    label: 'Фотографии',             icon: Image },
];

export default function CompanyFestivalEditPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('info');
  const [submitModal, setSubmitModal] = useState(false);

  const { data: festival, isLoading } = useQuery({
    queryKey: ['festival-edit', id],
    queryFn: () => festivalApi.getById(id!),
    enabled: !!id,
  });

  const submitMutation = useMutation({
    mutationFn: () => festivalApi.submitForModeration(id!),
    onSuccess: () => { setSubmitModal(false); navigate('/company/applications'); },
  });

  const isPublished = festival?.status === 'APPROVED';

  if (isLoading) return <Layout><Spinner centered /></Layout>;

  return (
    <Layout>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.title}>{festival?.name}</h1>
          <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
            {fmtDate(festival?.startDate)} — {fmtDate(festival?.endDate)}
          </p>
        </div>
        {!isPublished && (
          <Button onClick={() => setSubmitModal(true)}>
            <Send size={16} /> Отправить заявку
          </Button>
        )}
      </div>

      <div className={styles.editLayout}>
        <div className={styles.sidebar}>
          {TABS.map(t => {
            const Icon = t.icon;
            return (
              <button
                key={t.key}
                className={[styles.sidebarItem, activeTab === t.key ? styles.sidebarItemActive : ''].join(' ')}
                onClick={() => setActiveTab(t.key)}
              >
                <Icon size={16} /> {t.label}
              </button>
            );
          })}
        </div>

        <div className={styles.tabContent}>
          {activeTab === 'info'      && <FestivalInfoTab festivalId={id!} festival={festival} isPublished={isPublished} />}
          {activeTab === 'companies' && <FestivalOrganizersTab festivalId={id!} isPublished={isPublished} />}
          {activeTab === 'curators'  && <FestivalCuratorsTab festivalId={id!} isPublished={isPublished} />}
          {activeTab === 'events'    && <FestivalEventsTab festivalId={id!} isPublished={isPublished} />}
          {activeTab === 'stands'    && <FestivalStandsTab festivalId={id!} isPublished={isPublished} />}
          {activeTab === 'tickets'   && <FestivalTicketsTab festivalId={id!} festival={festival} isPublished={isPublished} />}
          {activeTab === 'photos'    && <FestivalPhotosTab festivalId={id!} />}
        </div>
      </div>

      <Modal open={submitModal} onClose={() => setSubmitModal(false)} title="Отправить заявку" size="sm">
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <p>Отправить заявку на рассмотрение? После отправки редактирование будет недоступно до решения администратора.</p>
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <Button variant="secondary" onClick={() => setSubmitModal(false)}>Отмена</Button>
            <Button loading={submitMutation.isPending} onClick={() => submitMutation.mutate()}>Отправить</Button>
          </div>
        </div>
      </Modal>
    </Layout>
  );
}
