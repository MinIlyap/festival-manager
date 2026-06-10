import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import Card from '../../../components/ui/Card';
import Button from '../../../components/ui/Button';
import { festivalApi } from '../../../api/festival';
import type { Festival } from '../../../types';
import { fmtDate } from './shared';
import styles from '../CompanyPages.module.css';

function InfoRow({ label, value }: { label: string; value?: string }) {
  if (!value) return null;
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      <span style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-muted)', fontFamily: 'var(--font-heading)' }}>{label}</span>
      <span style={{ fontSize: 14 }}>{value}</span>
    </div>
  );
}

interface Props {
  festivalId: string;
  festival: Festival | undefined;
  isPublished: boolean;
}

export function FestivalInfoTab({ festivalId, festival, isPublished }: Props) {
  const qc = useQueryClient();
  const [infoName, setInfoName] = useState('');
  const [infoDesc, setInfoDesc] = useState('');
  const [infoDateStart, setInfoDateStart] = useState('');
  const [infoTimeStart, setInfoTimeStart] = useState('');
  const [infoDateEnd, setInfoDateEnd] = useState('');
  const [infoTimeEnd, setInfoTimeEnd] = useState('');
  const [infoMaxPeople, setInfoMaxPeople] = useState('');
  const [infoAgeLimitId, setInfoAgeLimitId] = useState<number | ''>('');

  useEffect(() => {
    if (festival) {
      setInfoName(festival.name ?? '');
      setInfoDesc(festival.description ?? '');
      setInfoDateStart(festival.startDate ?? '');
      setInfoTimeStart(festival.timeStart ? festival.timeStart.substring(0, 5) : '');
      setInfoDateEnd(festival.endDate ?? '');
      setInfoTimeEnd(festival.timeEnd ? festival.timeEnd.substring(0, 5) : '');
      setInfoMaxPeople(festival.maxPeople != null ? String(festival.maxPeople) : '');
      setInfoAgeLimitId(festival.ageLimitId ?? '');
    }
  }, [festival]);

  const { data: ageLimits = [] } = useQuery({
    queryKey: ['age-limits'],
    queryFn: festivalApi.getAgeLimits,
  });

  const updateInfoMutation = useMutation({
    mutationFn: () => festivalApi.update(festivalId, {
      name: infoName,
      description: infoDesc || undefined,
      dateStart: infoDateStart,
      timeStart: infoTimeStart,
      dateEnd: infoDateEnd,
      timeEnd: infoTimeEnd,
      maxPeople: Number(infoMaxPeople),
      ageLimitId: Number(infoAgeLimitId),
    }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit', festivalId] }),
  });

  return (
    <Card padding="lg">
      <h3 className={styles.sectionTitle}>Информация о фестивале</h3>
      {isPublished ? (
        <div className={styles.form}>
          <InfoRow label="Название" value={festival?.name} />
          <InfoRow label="Описание" value={festival?.description} />
          <InfoRow label="Статус" value="Опубликован" />
          <InfoRow label="Дата начала" value={fmtDate(festival?.startDate)} />
          <InfoRow label="Дата окончания" value={fmtDate(festival?.endDate)} />
          <InfoRow label="Возрастное ограничение" value={festival?.ageRating != null ? `${festival.ageRating}+` : undefined} />
          <InfoRow label="Макс. посетителей" value={festival?.maxPeople != null ? String(festival.maxPeople) : undefined} />
        </div>
      ) : (
        <div className={styles.form}>
          <p style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 4 }}>
            Статус: <strong>Черновик / На модерации</strong>
          </p>

          <div className={styles.row}>
            <div className={styles.field}>
              <label>Название *</label>
              <input value={infoName} onChange={e => setInfoName(e.target.value)} placeholder="Название фестиваля" />
            </div>
          </div>

          <div className={styles.field}>
            <label>Описание</label>
            <textarea value={infoDesc} onChange={e => setInfoDesc(e.target.value)} placeholder="Описание фестиваля" rows={4} />
          </div>

          <div className={styles.row}>
            <div className={styles.field}>
              <label>Дата начала *</label>
              <input type="date" value={infoDateStart} onChange={e => setInfoDateStart(e.target.value)} />
            </div>
            <div className={styles.field}>
              <label>Время начала *</label>
              <input type="time" value={infoTimeStart} onChange={e => setInfoTimeStart(e.target.value)} />
            </div>
          </div>

          <div className={styles.row}>
            <div className={styles.field}>
              <label>Дата окончания *</label>
              <input type="date" value={infoDateEnd} onChange={e => setInfoDateEnd(e.target.value)} />
            </div>
            <div className={styles.field}>
              <label>Время окончания *</label>
              <input type="time" value={infoTimeEnd} onChange={e => setInfoTimeEnd(e.target.value)} />
            </div>
          </div>

          <div className={styles.row}>
            <div className={styles.field}>
              <label>Макс. посетителей *</label>
              <input type="number" min={1} value={infoMaxPeople} onChange={e => setInfoMaxPeople(e.target.value)} placeholder="1000" />
            </div>
            <div className={styles.field}>
              <label>Возрастное ограничение *</label>
              <select value={infoAgeLimitId} onChange={e => setInfoAgeLimitId(Number(e.target.value))}>
                <option value="">— Выберите —</option>
                {ageLimits.map(al => (
                  <option key={al.id} value={al.id}>{al.ageLimitValue}</option>
                ))}
              </select>
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 8 }}>
            <Button
              loading={updateInfoMutation.isPending}
              disabled={!infoName.trim() || !infoDateStart || !infoTimeStart || !infoDateEnd || !infoTimeEnd || !infoMaxPeople || !infoAgeLimitId}
              onClick={() => updateInfoMutation.mutate()}
            >
              Сохранить
            </Button>
          </div>
        </div>
      )}
    </Card>
  );
}
