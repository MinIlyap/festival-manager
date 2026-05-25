import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { Plus, Trash2 } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Spinner from '../../components/ui/Spinner';
import CityCombobox from '../../components/CityCombobox';
import { festivalApi } from '../../api/festival';
import { locationApi } from '../../api/location';
import type { City } from '../../types/index';
import styles from './CompanyPages.module.css';

interface TicketEntry {
  ticketTypeId: string;
  description: string;
  price: string;
}

function FieldError({ msg }: { msg?: string }) {
  return msg ? <span className={styles.fieldError}>{msg}</span> : null;
}

export default function CompanyFestivalNewPage() {
  const navigate = useNavigate();

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [city, setCity] = useState<City | null>(null);
  const [platformId, setPlatformId] = useState('');
  const [dateStart, setDateStart] = useState('');
  const [timeStart, setTimeStart] = useState('');
  const [dateEnd, setDateEnd] = useState('');
  const [timeEnd, setTimeEnd] = useState('');
  const [ageLimitId, setAgeLimitId] = useState('');
  const [maxPeople, setMaxPeople] = useState('');
  const [organizerTins, setOrganizerTins] = useState<string[]>([]);
  const [curatorEmails, setCuratorEmails] = useState<string[]>([]);
  const [ticketEntries, setTicketEntries] = useState<TicketEntry[]>([]);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const { data: ageLimits = [], isLoading: ageLimitsLoading } = useQuery({
    queryKey: ['age-limits'],
    queryFn: festivalApi.getAgeLimits,
  });

  const { data: ticketTypes = [] } = useQuery({
    queryKey: ['ticket-types'],
    queryFn: festivalApi.getTicketTypes,
  });

  const { data: platforms = [] } = useQuery({
    queryKey: ['platforms', city?.id],
    queryFn: () => locationApi.getPlatforms(city?.id),
    enabled: !!city,
  });

  const createMutation = useMutation({
    mutationFn: festivalApi.create,
    onSuccess: (f) => navigate(`/company/festivals/${f.id}/edit`),
  });

  function validate() {
    const e: Record<string, string> = {};
    if (!name.trim()) e.name = 'Укажите название';
    if (!description.trim()) e.description = 'Описание обязательно';
    if (!city) e.city = 'Выберите город';
    if (!platformId) e.platformId = 'Выберите площадку';
    if (!dateStart) e.dateStart = 'Укажите дату начала';
    if (!timeStart) e.timeStart = 'Укажите время начала';
    if (!dateEnd) e.dateEnd = 'Укажите дату окончания';
    if (!timeEnd) e.timeEnd = 'Укажите время окончания';
    if (!ageLimitId) e.ageLimitId = 'Выберите возрастное ограничение';
    if (!maxPeople || isNaN(Number(maxPeople)) || Number(maxPeople) < 1) e.maxPeople = 'Укажите максимальное число участников';
    organizerTins.forEach((tin, i) => { if (!tin.trim()) e[`tin_${i}`] = 'ИНН не может быть пустым'; });
    curatorEmails.forEach((email, i) => { if (!email.trim()) e[`curator_${i}`] = 'Email не может быть пустым'; });
    ticketEntries.forEach((t, i) => {
      if (!t.ticketTypeId) e[`ticket_type_${i}`] = 'Выберите тип билета';
      if (!t.description.trim()) e[`ticket_desc_${i}`] = 'Укажите описание';
      if (!t.price || isNaN(Number(t.price)) || Number(t.price) <= 0) e[`ticket_price_${i}`] = 'Укажите цену';
    });
    return e;
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const errs = validate();
    setErrors(errs);
    if (Object.keys(errs).length > 0) return;

    createMutation.mutate({
      name: name.trim(),
      description: description.trim(),
      platformId,
      startDate: dateStart,
      startTime: timeStart,
      endDate: dateEnd,
      endTime: timeEnd,
      ageLimitId: Number(ageLimitId),
      maxPeople: Number(maxPeople),
      organizerTins: organizerTins.filter(Boolean),
      curatorEmails: curatorEmails.filter(Boolean),
      ticketTypes: ticketEntries.map(t => ({
        ticketTypeId: Number(t.ticketTypeId),
        description: t.description.trim(),
        price: Number(t.price),
      })),
    });
  }

  if (ageLimitsLoading) return <Layout><Spinner centered /></Layout>;

  return (
    <Layout>
      <div className={styles.pageHeader}>
        <h1 className={styles.title}>Создать заявку на фестиваль</h1>
      </div>

      <form onSubmit={handleSubmit}>
        <div className={styles.newFestivalLayout}>

          {/* Basic info */}
          <Card padding="lg">
            <h3 className={styles.sectionTitle}>Основная информация</h3>
            <div className={styles.form}>
              <div className={styles.field}>
                <label>Название *</label>
                <input value={name} onChange={e => setName(e.target.value)} placeholder="CyberFest 2026" />
                <FieldError msg={errors.name} />
              </div>
              <div className={styles.field}>
                <label>Описание *</label>
                <textarea value={description} onChange={e => setDescription(e.target.value)} rows={4} placeholder="Описание фестиваля..." />
                <FieldError msg={errors.description} />
              </div>
              <div className={styles.field}>
                <label>Город *</label>
                <CityCombobox value={city} onChange={c => { setCity(c); setPlatformId(''); }} error={errors.city} />
              </div>
              {city && (
                <div className={styles.field}>
                  <label>Площадка *</label>
                  {platforms.length === 0
                    ? <p style={{ fontSize: 13, color: 'var(--text-muted)' }}>В выбранном городе нет площадок</p>
                    : (
                      <select value={platformId} onChange={e => setPlatformId(e.target.value)}>
                        <option value="">— Выберите площадку —</option>
                        {platforms.map(p => <option key={p.id} value={p.id}>{p.name}{p.address ? ` (${p.address})` : ''}</option>)}
                      </select>
                    )
                  }
                  <FieldError msg={errors.platformId} />
                </div>
              )}
              <div className={styles.row}>
                <div className={styles.field}>
                  <label>Дата начала *</label>
                  <input type="date" value={dateStart} onChange={e => setDateStart(e.target.value)} />
                  <FieldError msg={errors.dateStart} />
                </div>
                <div className={styles.field}>
                  <label>Время начала *</label>
                  <input type="time" value={timeStart} onChange={e => setTimeStart(e.target.value)} />
                  <FieldError msg={errors.timeStart} />
                </div>
              </div>
              <div className={styles.row}>
                <div className={styles.field}>
                  <label>Дата окончания *</label>
                  <input type="date" value={dateEnd} onChange={e => setDateEnd(e.target.value)} />
                  <FieldError msg={errors.dateEnd} />
                </div>
                <div className={styles.field}>
                  <label>Время окончания *</label>
                  <input type="time" value={timeEnd} onChange={e => setTimeEnd(e.target.value)} />
                  <FieldError msg={errors.timeEnd} />
                </div>
              </div>
              <div className={styles.row}>
                <div className={styles.field}>
                  <label>Возрастное ограничение *</label>
                  <select value={ageLimitId} onChange={e => setAgeLimitId(e.target.value)}>
                    <option value="">— Выберите —</option>
                    {ageLimits.map(al => <option key={al.id} value={al.id}>{al.ageLimitValue}</option>)}
                  </select>
                  <FieldError msg={errors.ageLimitId} />
                </div>
                <div className={styles.field}>
                  <label>Максимум участников *</label>
                  <input type="number" min={1} value={maxPeople} onChange={e => setMaxPeople(e.target.value)} placeholder="1000" />
                  <FieldError msg={errors.maxPeople} />
                </div>
              </div>
            </div>
          </Card>

          {/* Organizer TINs */}
          <Card padding="lg">
            <h3 className={styles.sectionTitle}>Компании-организаторы (ИНН)</h3>
            <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 'var(--space-md)' }}>
              Ваша компания добавляется автоматически. Укажите ИНН дополнительных организаторов.
            </p>
            <div className={styles.form}>
              {organizerTins.map((tin, i) => (
                <div key={i} className={styles.dynamicRow}>
                  <input
                    value={tin}
                    onChange={e => setOrganizerTins(prev => { const a = [...prev]; a[i] = e.target.value; return a; })}
                    placeholder="ИНН компании"
                  />
                  <Button type="button" variant="danger" size="sm" onClick={() => setOrganizerTins(prev => prev.filter((_, j) => j !== i))}>
                    <Trash2 size={14} />
                  </Button>
                  <FieldError msg={errors[`tin_${i}`]} />
                </div>
              ))}
              <Button type="button" variant="secondary" size="sm" onClick={() => setOrganizerTins(prev => [...prev, ''])}>
                <Plus size={14} /> Добавить организатора
              </Button>
            </div>
          </Card>

          {/* Curator emails */}
          <Card padding="lg">
            <h3 className={styles.sectionTitle}>Кураторы (Email)</h3>
            <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 'var(--space-md)' }}>
              Укажите email пользователей, которые станут кураторами фестиваля.
            </p>
            <div className={styles.form}>
              {curatorEmails.map((email, i) => (
                <div key={i} className={styles.dynamicRow}>
                  <input
                    type="email"
                    value={email}
                    onChange={e => setCuratorEmails(prev => { const a = [...prev]; a[i] = e.target.value; return a; })}
                    placeholder="curator@example.com"
                  />
                  <Button type="button" variant="danger" size="sm" onClick={() => setCuratorEmails(prev => prev.filter((_, j) => j !== i))}>
                    <Trash2 size={14} />
                  </Button>
                  <FieldError msg={errors[`curator_${i}`]} />
                </div>
              ))}
              <Button type="button" variant="secondary" size="sm" onClick={() => setCuratorEmails(prev => [...prev, ''])}>
                <Plus size={14} /> Добавить куратора
              </Button>
            </div>
          </Card>

          {/* Ticket types */}
          {ticketTypes.length > 0 && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}>Типы билетов</h3>
              <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 'var(--space-md)' }}>
                Добавьте типы билетов на фестиваль. Можно добавить позже.
              </p>
              <div className={styles.form}>
                {ticketEntries.map((entry, i) => (
                  <Card key={i} padding="md" style={{ background: 'var(--bg-elevated)' }}>
                    <div className={styles.form}>
                      <div className={styles.row}>
                        <div className={styles.field}>
                          <label>Тип билета *</label>
                          <select
                            value={entry.ticketTypeId}
                            onChange={e => setTicketEntries(prev => { const a = [...prev]; a[i] = { ...a[i], ticketTypeId: e.target.value }; return a; })}
                          >
                            <option value="">— Выберите —</option>
                            {ticketTypes.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                          </select>
                          <FieldError msg={errors[`ticket_type_${i}`]} />
                        </div>
                        <div className={styles.field}>
                          <label>Цена (₽) *</label>
                          <input
                            type="number"
                            min={0.01}
                            step={0.01}
                            value={entry.price}
                            onChange={e => setTicketEntries(prev => { const a = [...prev]; a[i] = { ...a[i], price: e.target.value }; return a; })}
                            placeholder="500.00"
                          />
                          <FieldError msg={errors[`ticket_price_${i}`]} />
                        </div>
                      </div>
                      <div className={styles.field}>
                        <label>Описание *</label>
                        <input
                          value={entry.description}
                          onChange={e => setTicketEntries(prev => { const a = [...prev]; a[i] = { ...a[i], description: e.target.value }; return a; })}
                          placeholder="Стандартный билет"
                        />
                        <FieldError msg={errors[`ticket_desc_${i}`]} />
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                        <Button type="button" variant="danger" size="sm" onClick={() => setTicketEntries(prev => prev.filter((_, j) => j !== i))}>
                          <Trash2 size={14} /> Удалить
                        </Button>
                      </div>
                    </div>
                  </Card>
                ))}
                <Button type="button" variant="secondary" size="sm" onClick={() => setTicketEntries(prev => [...prev, { ticketTypeId: '', description: '', price: '' }])}>
                  <Plus size={14} /> Добавить тип билета
                </Button>
              </div>
            </Card>
          )}

          {createMutation.isError && (
            <p style={{ color: 'var(--accent-danger)', fontSize: 14 }}>
              Ошибка: {(createMutation.error as Error)?.message ?? 'Не удалось создать фестиваль'}
            </p>
          )}

          <div className={styles.formActions}>
            <Button variant="secondary" type="button" onClick={() => navigate(-1)}>Отмена</Button>
            <Button type="submit" loading={createMutation.isPending}>Создать черновик</Button>
          </div>

        </div>
      </form>
    </Layout>
  );
}
