import { useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Info, Building2, Users, CalendarDays, Store, Ticket, Image, Send, Trash2, Search, Plus } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import { festivalApi, type FestivalOrganizer, type FestivalCurator, type CompanyInfo } from '../../api/festival';
import { eventApi } from '../../api/event';
import DictCombobox, { type DictItem } from '../../components/DictCombobox';
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

const EVENT_TYPES = [
  { value: 'TOURNAMENT',    label: 'Турнир' },
  { value: 'QUEST',         label: 'Квест' },
  { value: 'MASTER_CLASS',  label: 'Мастер-класс' },
  { value: 'DEMONSTRATION', label: 'Демонстрация' },
];

export default function CompanyFestivalEditPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [activeTab, setActiveTab] = useState('info');
  const [submitModal, setSubmitModal] = useState(false);
  const photoFileRef = useRef<HTMLInputElement>(null);

  // ── Festival data ───────────────────────────────────────────────────────────
  const { data: festival, isLoading } = useQuery({
    queryKey: ['festival-edit', id],
    queryFn: () => festivalApi.getById(id!),
    enabled: !!id,
  });

  const isPublished = festival?.status === 'APPROVED';

  const submitMutation = useMutation({
    mutationFn: () => festivalApi.submitForModeration(id!),
    onSuccess: () => { setSubmitModal(false); navigate('/company/applications'); },
  });

  // ── Organizers ──────────────────────────────────────────────────────────────
  const { data: organizers = [], isLoading: organizersLoading } = useQuery({
    queryKey: ['festival-organizers', id],
    queryFn: () => festivalApi.getOrganizers(id!),
    enabled: !!id && activeTab === 'companies',
  });

  const [tinSearch, setTinSearch] = useState('');
  const [foundCompany, setFoundCompany] = useState<CompanyInfo | null | 'not-found'>(null);
  const [showCreateCompany, setShowCreateCompany] = useState(false);
  const [newCompanyTin, setNewCompanyTin] = useState('');
  const [newCompanyFullName, setNewCompanyFullName] = useState('');
  const [newCompanyBrand, setNewCompanyBrand] = useState('');
  const [newCompanyEmail, setNewCompanyEmail] = useState('');

  const searchCompanyMutation = useMutation({
    mutationFn: (tin: string) => festivalApi.searchCompanyByTin(tin),
    onSuccess: (data) => { setFoundCompany(data); setShowCreateCompany(false); },
    onError: () => { setFoundCompany('not-found'); setShowCreateCompany(false); },
  });

  const addOrganizerMutation = useMutation({
    mutationFn: (tin: string) => festivalApi.addOrganizer(id!, tin),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-organizers', id] });
      setTinSearch(''); setFoundCompany(null); setShowCreateCompany(false);
    },
  });

  const createCompanyMutation = useMutation({
    mutationFn: () => festivalApi.createCompany({
      tin: newCompanyTin, full_name: newCompanyFullName, brand_name: newCompanyBrand, email: newCompanyEmail,
    }),
    onSuccess: async (company) => {
      await addOrganizerMutation.mutateAsync(company.tin);
      setNewCompanyTin(''); setNewCompanyFullName(''); setNewCompanyBrand(''); setNewCompanyEmail('');
    },
  });

  const removeOrganizerMutation = useMutation({
    mutationFn: (companyId: number) => festivalApi.removeOrganizer(id!, companyId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-organizers', id] }),
  });

  // ── Curators ────────────────────────────────────────────────────────────────
  const { data: curators = [], isLoading: curatorsLoading } = useQuery({
    queryKey: ['festival-curators', id],
    queryFn: () => festivalApi.getCurators(id!),
    enabled: !!id && activeTab === 'curators',
  });

  const [emailSearch, setEmailSearch] = useState('');
  const [foundCurator, setFoundCurator] = useState<FestivalCurator | null | 'not-found'>(null);

  const addCuratorMutation = useMutation({
    mutationFn: (email: string) => festivalApi.addCurator(id!, email),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-curators', id] });
      setEmailSearch(''); setFoundCurator(null);
    },
    onError: () => setFoundCurator('not-found'),
  });

  const removeCuratorMutation = useMutation({
    mutationFn: (userId: number) => festivalApi.removeCurator(id!, userId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-curators', id] }),
  });

  // ── Events ──────────────────────────────────────────────────────────────────
  const { data: festivalEvents = [] } = useQuery({
    queryKey: ['festival-edit-events', id],
    queryFn: () => eventApi.getEventsByFestival(id!),
    enabled: !!id && activeTab === 'events',
  });

  const { data: dictEvents = [] } = useQuery({
    queryKey: ['dict-events'],
    queryFn: eventApi.getDictionaryEvents,
    enabled: activeTab === 'events',
  });

  const [selectedEventDict, setSelectedEventDict] = useState<DictItem | null>(null);
  const [newEventType, setNewEventType] = useState('');
  const [newEventDesc, setNewEventDesc] = useState('');
  const [newEventDate, setNewEventDate] = useState('');
  const [newEventTime, setNewEventTime] = useState('');
  const [newEventFormat, setNewEventFormat] = useState<'SOLO' | 'TEAM'>('SOLO');
  const [newEventMax, setNewEventMax] = useState('');
  const [newEventTeamSize, setNewEventTeamSize] = useState('');

  const addEventMutation = useMutation({
    mutationFn: () => eventApi.createEvent({
      festivalId: id!,
      eventId: selectedEventDict!.id,
      type: newEventType,
      description: newEventDesc || undefined,
      format: newEventFormat,
      maxParticipants: Number(newEventMax) || 0,
      teamSize: newEventFormat === 'TEAM' ? Number(newEventTeamSize) : undefined,
      startDate: newEventDate,
      startTime: newEventTime || '00:00',
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-edit-events', id] });
      qc.invalidateQueries({ queryKey: ['dict-events'] });
      setSelectedEventDict(null); setNewEventType(''); setNewEventDesc('');
      setNewEventDate(''); setNewEventTime(''); setNewEventMax(''); setNewEventTeamSize('');
    },
  });

  const deleteEventMutation = useMutation({
    mutationFn: (eventId: string) => eventApi.deleteEvent(eventId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit-events', id] }),
  });

  // ── Stands ──────────────────────────────────────────────────────────────────
  const { data: festivalStands = [] } = useQuery({
    queryKey: ['festival-edit-stands', id],
    queryFn: () => eventApi.getStandsByFestival(id!),
    enabled: !!id && activeTab === 'stands',
  });

  const { data: dictStands = [] } = useQuery({
    queryKey: ['dict-stands'],
    queryFn: eventApi.getDictionaryStands,
    enabled: activeTab === 'stands',
  });

  const [selectedStandDict, setSelectedStandDict] = useState<DictItem | null>(null);
  const [newStandType, setNewStandType] = useState<'TRADE' | 'DEMO'>('TRADE');
  const [newStandDesc, setNewStandDesc] = useState('');
  const [newStandDate, setNewStandDate] = useState('');

  const addStandMutation = useMutation({
    mutationFn: () => eventApi.createStand({
      festivalId: id!,
      standId: selectedStandDict!.id,
      type: newStandType,
      description: newStandDesc || undefined,
      startDate: newStandDate,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-edit-stands', id] });
      qc.invalidateQueries({ queryKey: ['dict-stands'] });
      setSelectedStandDict(null); setNewStandType('TRADE'); setNewStandDesc(''); setNewStandDate('');
    },
  });

  const deleteStandMutation = useMutation({
    mutationFn: (standId: string) => eventApi.deleteStand(standId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit-stands', id] }),
  });

  // ── Ticket Types ─────────────────────────────────────────────────────────────
  const { data: ticketTypes = [] } = useQuery({
    queryKey: ['ticket-types'],
    queryFn: festivalApi.getTicketTypes,
    enabled: activeTab === 'tickets',
  });

  const [selectedTicketType, setSelectedTicketType] = useState<DictItem | null>(null);
  const [ticketPrice, setTicketPrice] = useState('');
  const [ticketDesc, setTicketDesc] = useState('');

  const addTicketMutation = useMutation({
    mutationFn: () => festivalApi.createTicketType({
      ticketTypeId: selectedTicketType!.id,
      festivalLocationId: id,
      description: ticketDesc || undefined,
      price: Number(ticketPrice),
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-edit', id] });
      setSelectedTicketType(null); setTicketPrice(''); setTicketDesc('');
    },
  });

  const deleteTicketMutation = useMutation({
    mutationFn: (ticketId: string) => festivalApi.deleteTicketType(ticketId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit', id] }),
  });

  // ── Photos ──────────────────────────────────────────────────────────────────
  const photoUploadMutation = useMutation({
    mutationFn: (file: File) => festivalApi.uploadPhoto(id!, file),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit', id] }),
  });

  const photoDeleteMutation = useMutation({
    mutationFn: () => festivalApi.deletePhoto(id!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit', id] }),
  });

  if (isLoading) return <Layout><Spinner centered /></Layout>;

  return (
    <Layout>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.title}>{festival?.name}</h1>
          <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
            {festival?.startDate} — {festival?.endDate}
          </p>
        </div>
        {!isPublished && (
          <Button onClick={() => setSubmitModal(true)}>
            <Send size={16} /> Отправить заявку
          </Button>
        )}
      </div>

      <div className={styles.editLayout}>
        {/* Sidebar */}
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

        {/* Tab content */}
        <div className={styles.tabContent}>

          {/* ── General Info ── */}
          {activeTab === 'info' && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}>Информация о фестивале</h3>
              <div className={styles.form}>
                <InfoRow label="Название" value={festival?.name} />
                <InfoRow label="Описание" value={festival?.description} />
                <InfoRow label="Статус" value={isPublished ? 'Опубликован' : 'Черновик / На модерации'} />
                <InfoRow label="Дата начала" value={festival?.startDate} />
                <InfoRow label="Дата окончания" value={festival?.endDate} />
                <InfoRow label="Возрастное ограничение" value={festival?.ageRating != null ? `${festival.ageRating}+` : undefined} />
              </div>
            </Card>
          )}

          {/* ── Companies ── */}
          {activeTab === 'companies' && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}>Компании-организаторы</h3>

              {!isPublished && (
                <div style={{ marginBottom: 16 }}>
                  <div className={styles.addBar}>
                    <input
                      placeholder="ИНН компании"
                      value={tinSearch}
                      onChange={e => { setTinSearch(e.target.value); setFoundCompany(null); setShowCreateCompany(false); }}
                      style={{ flex: 1 }}
                    />
                    <Button
                      size="sm"
                      disabled={!tinSearch.trim()}
                      loading={searchCompanyMutation.isPending}
                      onClick={() => searchCompanyMutation.mutate(tinSearch.trim())}
                    >
                      <Search size={14} /> Найти
                    </Button>
                  </div>

                  {foundCompany === 'not-found' && (
                    <div style={{ marginTop: 8 }}>
                      <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 8 }}>Компания не найдена.</p>
                      <Button size="sm" variant="secondary" onClick={() => {
                        setShowCreateCompany(true); setNewCompanyTin(tinSearch); setFoundCompany(null);
                      }}>
                        <Plus size={14} /> Создать компанию
                      </Button>
                    </div>
                  )}

                  {foundCompany && foundCompany !== 'not-found' && (
                    <CompanyCard
                      company={foundCompany}
                      action={
                        <Button size="sm" loading={addOrganizerMutation.isPending}
                          onClick={() => addOrganizerMutation.mutate(foundCompany.tin)}>
                          Добавить
                        </Button>
                      }
                    />
                  )}

                  {showCreateCompany && (
                    <div className={styles.form} style={{ marginTop: 12, padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)' }}>
                      <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)' }}>Создать новую компанию</p>
                      <div className={styles.row}>
                        <input placeholder="ИНН *" value={newCompanyTin} onChange={e => setNewCompanyTin(e.target.value)} />
                        <input placeholder="Email *" value={newCompanyEmail} onChange={e => setNewCompanyEmail(e.target.value)} />
                      </div>
                      <input placeholder="Полное название *" value={newCompanyFullName} onChange={e => setNewCompanyFullName(e.target.value)} />
                      <input placeholder="Бренд / торговое название *" value={newCompanyBrand} onChange={e => setNewCompanyBrand(e.target.value)} />
                      <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                        <Button size="sm" variant="secondary" onClick={() => setShowCreateCompany(false)}>Отмена</Button>
                        <Button
                          size="sm"
                          loading={createCompanyMutation.isPending || addOrganizerMutation.isPending}
                          disabled={!newCompanyTin || !newCompanyFullName || !newCompanyBrand || !newCompanyEmail}
                          onClick={() => createCompanyMutation.mutate()}
                        >
                          Создать и добавить
                        </Button>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {organizersLoading ? <Spinner /> : (
                <div className={styles.list}>
                  {organizers.length === 0
                    ? <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Нет организаторов</p>
                    : organizers.map(org => (
                      <OrganizerRow key={org.company_id} organizer={org} canRemove={!isPublished}
                        onRemove={() => removeOrganizerMutation.mutate(org.company_id)}
                        removing={removeOrganizerMutation.isPending} />
                    ))
                  }
                </div>
              )}
            </Card>
          )}

          {/* ── Curators ── */}
          {activeTab === 'curators' && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}>Кураторы</h3>

              {!isPublished && (
                <div style={{ marginBottom: 16 }}>
                  <div className={styles.addBar}>
                    <input type="email" placeholder="Email пользователя" value={emailSearch}
                      onChange={e => { setEmailSearch(e.target.value); setFoundCurator(null); }} style={{ flex: 1 }} />
                    <Button size="sm" disabled={!emailSearch.trim()} loading={addCuratorMutation.isPending}
                      onClick={() => addCuratorMutation.mutate(emailSearch.trim())}>
                      <Plus size={14} /> Добавить
                    </Button>
                  </div>
                  {foundCurator === 'not-found' && (
                    <p style={{ fontSize: 13, color: 'var(--accent-danger)', marginTop: 6 }}>
                      Пользователь не найден или уже является куратором.
                    </p>
                  )}
                </div>
              )}

              {curatorsLoading ? <Spinner /> : (
                <div className={styles.list}>
                  {curators.length === 0
                    ? <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Нет кураторов</p>
                    : curators.map(c => (
                      <div key={c.user_id} className={styles.listItem}>
                        <div>
                          <p className={styles.listItemName}>{c.email}</p>
                          <p className={styles.listItemSub}>ID: {c.user_id}</p>
                        </div>
                        {!isPublished && (
                          <Button variant="danger" size="sm" loading={removeCuratorMutation.isPending}
                            onClick={() => removeCuratorMutation.mutate(c.user_id)}>
                            <Trash2 size={14} />
                          </Button>
                        )}
                      </div>
                    ))
                  }
                </div>
              )}
            </Card>
          )}

          {/* ── Events ── */}
          {activeTab === 'events' && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}>Мероприятия</h3>

              {!isPublished && (
                <div className={styles.form} style={{ marginBottom: 24, padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)' }}>
                  <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)' }}>Добавить мероприятие</p>

                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Название мероприятия *</label>
                      <DictCombobox
                        items={dictEvents}
                        value={selectedEventDict}
                        onChange={setSelectedEventDict}
                        onCreateNew={name => eventApi.createDictionaryEvent(name).then(newId => {
                          qc.invalidateQueries({ queryKey: ['dict-events'] });
                          return newId;
                        })}
                        placeholder="Выберите или создайте..."
                      />
                    </div>
                    <div className={styles.field}>
                      <label>Тип</label>
                      <select value={newEventType} onChange={e => setNewEventType(e.target.value)}>
                        <option value="">— Тип —</option>
                        {EVENT_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
                      </select>
                    </div>
                  </div>

                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Формат</label>
                      <select value={newEventFormat} onChange={e => setNewEventFormat(e.target.value as 'SOLO' | 'TEAM')}>
                        <option value="SOLO">Одиночное</option>
                        <option value="TEAM">Командное</option>
                      </select>
                    </div>
                    <div className={styles.field}>
                      <label>Макс. участников *</label>
                      <input type="number" min={1} value={newEventMax} onChange={e => setNewEventMax(e.target.value)} placeholder="100" />
                    </div>
                  </div>

                  {newEventFormat === 'TEAM' && (
                    <div className={styles.field}>
                      <label>Размер команды</label>
                      <input type="number" min={2} value={newEventTeamSize} onChange={e => setNewEventTeamSize(e.target.value)} placeholder="4" />
                    </div>
                  )}

                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Дата *</label>
                      <input type="date" value={newEventDate} onChange={e => setNewEventDate(e.target.value)} />
                    </div>
                    <div className={styles.field}>
                      <label>Время</label>
                      <input type="time" value={newEventTime} onChange={e => setNewEventTime(e.target.value)} />
                    </div>
                  </div>

                  <div className={styles.field}>
                    <label>Описание</label>
                    <input value={newEventDesc} onChange={e => setNewEventDesc(e.target.value)} placeholder="Необязательно" />
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                    <Button size="sm" loading={addEventMutation.isPending}
                      disabled={!selectedEventDict || !newEventType || !newEventDate || !newEventMax}
                      onClick={() => addEventMutation.mutate()}>
                      Добавить
                    </Button>
                  </div>
                </div>
              )}

              <div className={styles.list}>
                {festivalEvents.length === 0
                  ? <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Нет мероприятий</p>
                  : festivalEvents.map(ev => (
                    <div key={ev.id} className={styles.listItem}>
                      <div style={{ flex: 1 }}>
                        <p className={styles.listItemName}>{ev.name}</p>
                        <p className={styles.listItemSub}>
                          {EVENT_TYPES.find(t => t.value === ev.type)?.label ?? ev.type}
                          {' · '}{ev.format === 'TEAM' ? 'Командное' : 'Одиночное'}
                          {ev.startDate ? ` · ${ev.startDate}` : ''}
                        </p>
                      </div>
                      {!isPublished && (
                        <Button variant="danger" size="sm" loading={deleteEventMutation.isPending}
                          onClick={() => deleteEventMutation.mutate(ev.id)}>
                          <Trash2 size={14} />
                        </Button>
                      )}
                    </div>
                  ))
                }
              </div>
            </Card>
          )}

          {/* ── Stands ── */}
          {activeTab === 'stands' && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}>Стенды</h3>

              {!isPublished && (
                <div className={styles.form} style={{ marginBottom: 24, padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)' }}>
                  <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)' }}>Добавить стенд</p>

                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Название стенда *</label>
                      <DictCombobox
                        items={dictStands}
                        value={selectedStandDict}
                        onChange={setSelectedStandDict}
                        onCreateNew={name => eventApi.createDictionaryStand(name).then(newId => {
                          qc.invalidateQueries({ queryKey: ['dict-stands'] });
                          return newId;
                        })}
                        placeholder="Выберите или создайте..."
                      />
                    </div>
                    <div className={styles.field}>
                      <label>Тип</label>
                      <select value={newStandType} onChange={e => setNewStandType(e.target.value as 'TRADE' | 'DEMO')}>
                        <option value="TRADE">Торговый</option>
                        <option value="DEMO">Демонстрационный</option>
                      </select>
                    </div>
                  </div>

                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Дата начала *</label>
                      <input type="date" value={newStandDate} onChange={e => setNewStandDate(e.target.value)} />
                    </div>
                    <div className={styles.field}>
                      <label>Описание</label>
                      <input value={newStandDesc} onChange={e => setNewStandDesc(e.target.value)} placeholder="Необязательно" />
                    </div>
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                    <Button size="sm" loading={addStandMutation.isPending}
                      disabled={!selectedStandDict || !newStandDate}
                      onClick={() => addStandMutation.mutate()}>
                      Добавить
                    </Button>
                  </div>
                </div>
              )}

              <div className={styles.list}>
                {festivalStands.length === 0
                  ? <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Нет стендов</p>
                  : festivalStands.map(st => (
                    <div key={st.id} className={styles.listItem}>
                      <div style={{ flex: 1 }}>
                        <p className={styles.listItemName}>{st.name}</p>
                        <p className={styles.listItemSub}>{st.type === 'TRADE' ? 'Торговый' : 'Демонстрационный'}{st.description ? ` · ${st.description}` : ''}</p>
                      </div>
                      {!isPublished && (
                        <Button variant="danger" size="sm" loading={deleteStandMutation.isPending}
                          onClick={() => deleteStandMutation.mutate(st.id)}>
                          <Trash2 size={14} />
                        </Button>
                      )}
                    </div>
                  ))
                }
              </div>
            </Card>
          )}

          {/* ── Ticket Types ── */}
          {activeTab === 'tickets' && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}>Типы билетов</h3>

              {!isPublished && (
                <div className={styles.form} style={{ marginBottom: 16, padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)' }}>
                  <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)' }}>Добавить тип билета</p>
                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Тип билета *</label>
                      <DictCombobox
                        items={ticketTypes}
                        value={selectedTicketType}
                        onChange={setSelectedTicketType}
                        placeholder="Выберите тип..."
                      />
                    </div>
                    <div className={styles.field}>
                      <label>Цена (₽) *</label>
                      <input placeholder="500" value={ticketPrice} onChange={e => setTicketPrice(e.target.value)} type="number" min={0} />
                    </div>
                  </div>
                  <div className={styles.field}>
                    <label>Описание</label>
                    <input placeholder="Необязательно" value={ticketDesc} onChange={e => setTicketDesc(e.target.value)} />
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                    <Button size="sm" loading={addTicketMutation.isPending}
                      disabled={!selectedTicketType || !ticketPrice}
                      onClick={() => addTicketMutation.mutate()}>
                      Добавить
                    </Button>
                  </div>
                </div>
              )}

              <div className={styles.list}>
                {(festival?.ticketTypes ?? []).length === 0
                  ? <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Нет типов билетов</p>
                  : (festival?.ticketTypes ?? []).map(tt => (
                    <div key={tt.id} className={styles.listItem}>
                      <div>
                        <p className={styles.listItemName}>{tt.name}</p>
                        <p className={styles.listItemSub}>
                          {tt.price.toLocaleString('ru-RU')} ₽{tt.description ? ` · ${tt.description}` : ''}
                        </p>
                      </div>
                      {!isPublished && (
                        <Button variant="danger" size="sm" loading={deleteTicketMutation.isPending}
                          onClick={() => deleteTicketMutation.mutate(tt.id)}>
                          <Trash2 size={14} />
                        </Button>
                      )}
                    </div>
                  ))
                }
              </div>
            </Card>
          )}

          {/* ── Photos ── */}
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
                  <Button variant="danger" loading={photoDeleteMutation.isPending} onClick={() => photoDeleteMutation.mutate()}>
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

// ── Sub-components ─────────────────────────────────────────────────────────────

function InfoRow({ label, value }: { label: string; value?: string }) {
  if (!value) return null;
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      <span style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-muted)', fontFamily: 'var(--font-heading)' }}>{label}</span>
      <span style={{ fontSize: 14 }}>{value}</span>
    </div>
  );
}

function CompanyCard({ company, action }: { company: CompanyInfo; action?: React.ReactNode }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 16px', background: 'var(--bg-elevated)', borderRadius: 'var(--radius-sm)', marginTop: 8 }}>
      <div>
        <p style={{ fontSize: 14, fontWeight: 600 }}>{company.brand_name || company.full_name}</p>
        <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>ИНН: {company.tin} · {company.email}</p>
        {!company.is_active && <p style={{ fontSize: 11, color: 'var(--accent-warning)' }}>Не активирована</p>}
      </div>
      {action}
    </div>
  );
}

function OrganizerRow({ organizer, canRemove, onRemove, removing }: {
  organizer: FestivalOrganizer;
  canRemove: boolean;
  onRemove: () => void;
  removing: boolean;
}) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 16px', background: 'var(--bg-elevated)', borderRadius: 'var(--radius-sm)' }}>
      <div>
        <p style={{ fontSize: 14, fontWeight: 600 }}>{organizer.brand_name || organizer.full_name || `Компания #${organizer.company_id}`}</p>
        <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>ИНН: {organizer.tin}{organizer.email ? ` · ${organizer.email}` : ''}</p>
      </div>
      {canRemove && (
        <Button variant="danger" size="sm" loading={removing} onClick={onRemove}>
          <Trash2 size={14} />
        </Button>
      )}
    </div>
  );
}
