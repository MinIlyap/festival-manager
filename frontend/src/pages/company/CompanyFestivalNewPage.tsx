import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import Layout from '../../components/layout/Layout';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Spinner from '../../components/ui/Spinner';
import PlatformCombobox from '../../components/PlatformCombobox';
import { festivalApi } from '../../api/festival';
import type { PlatformInstanceResponse } from '../../api/location';
import styles from './CompanyPages.module.css';

function FieldError({ msg }: { msg?: string }) {
  return msg ? <span className={styles.fieldError}>{msg}</span> : null;
}

export default function CompanyFestivalNewPage() {
  const navigate = useNavigate();

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [platform, setPlatform] = useState<PlatformInstanceResponse | null>(null);
  const [dateStart, setDateStart] = useState('');
  const [timeStart, setTimeStart] = useState('');
  const [dateEnd, setDateEnd] = useState('');
  const [timeEnd, setTimeEnd] = useState('');
  const [ageLimitId, setAgeLimitId] = useState('');
  const [maxPeople, setMaxPeople] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});

  const { data: ageLimits = [], isLoading } = useQuery({
    queryKey: ['age-limits'],
    queryFn: festivalApi.getAgeLimits,
  });

  const createMutation = useMutation({
    mutationFn: festivalApi.create,
    onSuccess: (f) => navigate(`/company/festivals/${f.id}/edit`),
  });

  function validate() {
    const e: Record<string, string> = {};
    if (!name.trim()) e.name = 'Укажите название';
    if (!description.trim()) e.description = 'Описание обязательно';
    if (!platform) e.platform = 'Выберите площадку';
    if (!dateStart) e.dateStart = 'Укажите дату начала';
    if (!timeStart) e.timeStart = 'Укажите время начала';
    if (!dateEnd) e.dateEnd = 'Укажите дату окончания';
    if (!timeEnd) e.timeEnd = 'Укажите время окончания';
    if (!ageLimitId) e.ageLimitId = 'Выберите возрастное ограничение';
    if (!maxPeople || isNaN(Number(maxPeople)) || Number(maxPeople) < 1) e.maxPeople = 'Укажите максимальное число участников';
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
      platformId: String(platform!.id),
      startDate: dateStart,
      startTime: timeStart,
      endDate: dateEnd,
      endTime: timeEnd,
      ageLimitId: Number(ageLimitId),
      maxPeople: Number(maxPeople),
    });
  }

  if (isLoading) return <Layout><Spinner centered /></Layout>;

  return (
    <Layout>
      <div className={styles.pageHeader}>
        <h1 className={styles.title}>Создать черновик фестиваля</h1>
      </div>

      <form onSubmit={handleSubmit}>
        <div className={styles.newFestivalLayout}>
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
                <label>Площадка *</label>
                <PlatformCombobox value={platform} onChange={setPlatform} error={errors.platform} />
              </div>

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
                    {ageLimits.map(al => (
                      <option key={al.id} value={al.id}>{al.ageLimitValue}</option>
                    ))}
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

          {createMutation.isError && (
            <p style={{ color: 'var(--accent-danger)', fontSize: 14 }}>
              Ошибка: {(createMutation.error as Error)?.message ?? 'Не удалось создать черновик'}
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
