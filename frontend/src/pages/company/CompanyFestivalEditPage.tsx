import { useState, useRef, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { CalendarDays, Store, Ticket, Image, Send, Trash2 } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import { festivalApi } from '../../api/festival';
import { applicationApi } from '../../api/application';
import { eventApi } from '../../api/event';
import styles from './CompanyPages.module.css';

const TABS = [
  { key: 'events',  label: 'Мероприятия',  icon: CalendarDays },
  { key: 'stands',  label: 'Стенды',        icon: Store },
  { key: 'tickets', label: 'Типы билетов',  icon: Ticket },
  { key: 'photos',  label: 'Фотографии',    icon: Image },
];

export default function CompanyFestivalEditPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [activeTab, setActiveTab] = useState('events');
  const photoFileRef = useRef<HTMLInputElement>(null);
  const [submitModal, setSubmitModal] = useState(false);

  const [ticketTypeId, setTicketTypeId] = useState('');
  const [ticketPrice, setTicketPrice] = useState('');
  const [ticketDesc, setTicketDesc] = useState('');

  const { data: festival, isLoading } = useQuery({
    queryKey: ['festival-edit', id],
    queryFn: () => festivalApi.getById(id!),
    enabled: !!id,
  });

  const { data: ticketTypes = [] } = useQuery({
    queryKey: ['ticket-types'],
    queryFn: festivalApi.getTicketTypes,
  });

  const submitMutation = useMutation({
    mutationFn: () => applicationApi.submitFestivalApplication(id!),
    onSuccess: () => { setSubmitModal(false); navigate('/company/applications'); },
  });

  const { data: festivalEvents = [] } = useQuery({
    queryKey: ['festival-edit-events', id],
    queryFn: () => eventApi.getEventsByFestival(id!),
    enabled: !!id && activeTab === 'events',
  });

  const { data: festivalStands = [] } = useQuery({
    queryKey: ['festival-edit-stands', id],
    queryFn: () => eventApi.getStandsByFestival(id!),
    enabled: !!id && activeTab === 'stands',
  });

  const photoUploadMutation = useMutation({
    mutationFn: (file: File) => festivalApi.uploadPhoto(id!, file),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit', id] }),
  });

  const photoDeleteMutation = useMutation({
    mutationFn: () => festivalApi.deletePhoto(id!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit', id] }),
  });

  const eventPhotoMutation = useMutation({
    mutationFn: ({ eventId, file }: { eventId: string; file: File }) =>
      eventApi.uploadEventPhoto(eventId, file),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit-events', id] }),
  });

  const eventPhotoDeleteMutation = useMutation({
    mutationFn: (eventId: string) => eventApi.deleteEventPhoto(eventId),
  });

  const standPhotoMutation = useMutation({
    mutationFn: ({ standId, file }: { standId: string; file: File }) =>
      eventApi.uploadStandPhoto(standId, file),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit-stands', id] }),
  });

  const standPhotoDeleteMutation = useMutation({
    mutationFn: (standId: string) => eventApi.deleteStandPhoto(standId),
  });

  const addTicketMutation = useMutation({
    mutationFn: () => festivalApi.createTicketType({
      ticketTypeId: Number(ticketTypeId) || 0,
      festivalLocationId: id,
      description: ticketDesc || undefined,
      price: Number(ticketPrice) || 0,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-edit', id] });
      setTicketTypeId(''); setTicketPrice(''); setTicketDesc('');
    },
  });

  const deleteTicketMutation = useMutation({
    mutationFn: (ticketId: string) => festivalApi.deleteTicketType(ticketId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit', id] }),
  });

  if (isLoading) return <Layout><Spinner centered /></Layout>;

  const canSubmit = festival?.status !== 'APPROVED';

  return (
    <Layout>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.title}>{festival?.name}</h1>
          <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
            {festival?.startDate} — {festival?.endDate}
          </p>
        </div>
        <Button onClick={() => setSubmitModal(true)} disabled={!canSubmit}>
          <Send size={16} /> Отправить заявку
        </Button>
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
          {activeTab === 'events' && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}>Мероприятия фестиваля</h3>
              {festivalEvents.length === 0
                ? <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Нет мероприятий</p>
                : festivalEvents.map(ev => (
                  <div key={ev.id} className={styles.listItem} style={{ marginBottom: 12 }}>
                    <div style={{ flex: 1 }}>
                      <p className={styles.listItemName}>{ev.name}</p>
                      <p className={styles.listItemSub}>{ev.type} · {ev.format === 'SOLO' ? 'Одиночное' : 'Командное'}</p>
                    </div>
                    <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                      <img
                        src={eventApi.getEventPhotoUrl(ev.id)}
                        alt=""
                        onError={e => { (e.target as HTMLImageElement).style.display = 'none'; }}
                        style={{ width: 48, height: 48, objectFit: 'cover', borderRadius: 6 }}
                      />
                      <label style={{ cursor: 'pointer' }}>
                        <span style={{ fontSize: 12, color: 'var(--accent-primary)', fontWeight: 600 }}>
                          {eventPhotoMutation.isPending ? '…' : 'Загрузить фото'}
                        </span>
                        <input type="file" accept="image/*" style={{ display: 'none' }}
                          onChange={e => { const f = e.target.files?.[0]; if (f) eventPhotoMutation.mutate({ eventId: ev.id, file: f }); }} />
                      </label>
                      <Button variant="danger" size="sm" onClick={() => eventPhotoDeleteMutation.mutate(ev.id)}>
                        <Trash2 size={12} />
                      </Button>
                    </div>
                  </div>
                ))
              }
            </Card>
          )}

          {activeTab === 'stands' && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}>Стенды фестиваля</h3>
              {festivalStands.length === 0
                ? <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Нет стендов</p>
                : festivalStands.map(st => (
                  <div key={st.id} className={styles.listItem} style={{ marginBottom: 12 }}>
                    <div style={{ flex: 1 }}>
                      <p className={styles.listItemName}>{st.name}</p>
                      <p className={styles.listItemSub}>{st.type === 'TRADE' ? 'Торговый' : 'Демо'}{st.description ? ` · ${st.description}` : ''}</p>
                    </div>
                    <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                      <img
                        src={`/api/files/FESTIVAL_STAND/${st.id}`}
                        alt=""
                        onError={e => { (e.target as HTMLImageElement).style.display = 'none'; }}
                        style={{ width: 48, height: 48, objectFit: 'cover', borderRadius: 6 }}
                      />
                      <label style={{ cursor: 'pointer' }}>
                        <span style={{ fontSize: 12, color: 'var(--accent-primary)', fontWeight: 600 }}>
                          {standPhotoMutation.isPending ? '…' : 'Загрузить фото'}
                        </span>
                        <input type="file" accept="image/*" style={{ display: 'none' }}
                          onChange={e => { const f = e.target.files?.[0]; if (f) standPhotoMutation.mutate({ standId: st.id, file: f }); }} />
                      </label>
                      <Button variant="danger" size="sm" onClick={() => standPhotoDeleteMutation.mutate(st.id)}>
                        <Trash2 size={12} />
                      </Button>
                    </div>
                  </div>
                ))
              }
            </Card>
          )}

          {activeTab === 'tickets' && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}>Типы билетов</h3>
              <div className={styles.addBar} style={{ alignItems: 'flex-start', flexWrap: 'wrap', gap: 8 }}>
                <select
                  value={ticketTypeId}
                  onChange={e => setTicketTypeId(e.target.value)}
                  style={{ minWidth: 160 }}
                >
                  <option value="">— Тип билета —</option>
                  {ticketTypes.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                </select>
                <input placeholder="Цена (руб.)" value={ticketPrice} onChange={e => setTicketPrice(e.target.value)} style={{ width: 120 }} />
                <input placeholder="Описание" value={ticketDesc} onChange={e => setTicketDesc(e.target.value)} style={{ flex: 1, minWidth: 140 }} />
                <Button onClick={() => addTicketMutation.mutate()} loading={addTicketMutation.isPending} disabled={!ticketTypeId || !ticketPrice || !ticketDesc}>
                  Добавить
                </Button>
              </div>
              <div className={styles.list} style={{ marginTop: 16 }}>
                {(festival?.ticketTypes ?? []).length === 0
                  ? <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Нет типов билетов</p>
                  : (festival?.ticketTypes ?? []).map(tt => (
                    <div key={tt.id} className={styles.listItem}>
                      <div>
                        <p className={styles.listItemName}>{tt.name}</p>
                        <p className={styles.listItemSub}>{tt.price.toLocaleString('ru-RU')} ₽{tt.description ? ` · ${tt.description}` : ''}</p>
                      </div>
                      <Button variant="danger" size="sm" loading={deleteTicketMutation.isPending} onClick={() => deleteTicketMutation.mutate(tt.id)}>
                        <Trash2 size={14} />
                      </Button>
                    </div>
                  ))
                }
              </div>
            </Card>
          )}

          {activeTab === 'photos' && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}>Обложка фестиваля</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12, maxWidth: 480 }}>
                <img
                  src={`/api/files/FESTIVAL_LOCATION/${id}`}
                  alt="Обложка"
                  onError={e => { (e.target as HTMLImageElement).style.display = 'none'; }}
                  style={{ width: '100%', aspectRatio: '16/9', objectFit: 'cover', borderRadius: 8, background: 'var(--surface-secondary)' }}
                />
                <div style={{ display: 'flex', gap: 8 }}>
                  <Button fullWidth loading={photoUploadMutation.isPending} onClick={() => photoFileRef.current?.click()}>
                    <Image size={14} /> Загрузить обложку
                  </Button>
                  <input ref={photoFileRef} type="file" accept="image/*" style={{ display: 'none' }}
                    onChange={e => { const f = e.target.files?.[0]; if (f) photoUploadMutation.mutate(f); }} />
                  <Button variant="danger" onClick={() => photoDeleteMutation.mutate()} loading={photoDeleteMutation.isPending}>
                    <Trash2 size={14} />
                  </Button>
                </div>
              </div>
            </Card>
          )}
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
