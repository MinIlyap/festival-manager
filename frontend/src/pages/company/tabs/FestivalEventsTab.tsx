import { useState, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Pencil, Trash2, Trophy, Image, X, Plus } from 'lucide-react';
import Card from '../../../components/ui/Card';
import Button from '../../../components/ui/Button';
import { festivalApi, type CompanyInfo } from '../../../api/festival';
import { eventApi, type EventReward } from '../../../api/event';
import DictCombobox, { type DictItem } from '../../../components/DictCombobox';
import { fmtDate, PhotoImg, OrganizerBlock } from './shared';
import styles from '../CompanyPages.module.css';

const ORDINALS_RU = [
  'Первое', 'Второе', 'Третье', 'Четвёртое', 'Пятое',
  'Шестое', 'Седьмое', 'Восьмое', 'Девятое', 'Десятое',
  'Одиннадцатое', 'Двенадцатое', 'Тринадцатое', 'Четырнадцатое', 'Пятнадцатое',
  'Шестнадцатое', 'Семнадцатое', 'Восемнадцатое', 'Девятнадцатое', 'Двадцатое',
];
const ordinalRu = (n: number) =>
  ORDINALS_RU[n - 1] ? `${ORDINALS_RU[n - 1]} место` : `Место ${n}`;

const EVENT_TYPES = [
  { value: 'TOURNAMENT',    label: 'Турнир' },
  { value: 'QUEST',         label: 'Квест' },
  { value: 'MASTER_CLASS',  label: 'Мастер-класс' },
  { value: 'DEMONSTRATION', label: 'Демонстрация' },
];

interface Props {
  festivalId: string;
  isPublished: boolean;
}

export function FestivalEventsTab({ festivalId, isPublished }: Props) {
  const qc = useQueryClient();

  const { data: festivalEvents = [] } = useQuery({
    queryKey: ['festival-edit-events', festivalId],
    queryFn: () => eventApi.getEventsByFestival(festivalId),
    enabled: !!festivalId,
  });

  const { data: dictEvents = [] } = useQuery({
    queryKey: ['dict-events'],
    queryFn: eventApi.getDictionaryEvents,
  });

  const [selectedEventDict, setSelectedEventDict] = useState<DictItem | null>(null);
  const [newEventType, setNewEventType] = useState('');
  const [newEventDesc, setNewEventDesc] = useState('');
  const [newEventDate, setNewEventDate] = useState('');
  const [newEventTime, setNewEventTime] = useState('');
  const [newEventDuration, setNewEventDuration] = useState('');
  const [newEventFormat, setNewEventFormat] = useState<'SOLO' | 'TEAM'>('SOLO');
  const [newEventMax, setNewEventMax] = useState('');
  const [newEventTeamSize, setNewEventTeamSize] = useState('');

  const [editingEventId, setEditingEventId] = useState<string | null>(null);
  const [editEventType, setEditEventType] = useState('');
  const [editEventDesc, setEditEventDesc] = useState('');
  const [editEventDate, setEditEventDate] = useState('');
  const [editEventTime, setEditEventTime] = useState('');
  const [editEventDuration, setEditEventDuration] = useState('');
  const [editEventFormat, setEditEventFormat] = useState<'SOLO' | 'TEAM'>('SOLO');
  const [editEventMax, setEditEventMax] = useState('');
  const [editEventTeamSize, setEditEventTeamSize] = useState('');

  const [newEventOrgTin, setNewEventOrgTin] = useState('');
  const [newEventOrgFound, setNewEventOrgFound] = useState<CompanyInfo | null | 'not-found'>(null);
  const [newEventOrgSelected, setNewEventOrgSelected] = useState<CompanyInfo | null>(null);

  const searchNewEventOrgMutation = useMutation({
    mutationFn: (tin: string) => festivalApi.searchCompanyByTin(tin),
    onSuccess: (data) => setNewEventOrgFound(data),
    onError: () => setNewEventOrgFound('not-found'),
  });

  const [editEventOrgTin, setEditEventOrgTin] = useState('');
  const [editEventOrgFound, setEditEventOrgFound] = useState<CompanyInfo | null | 'not-found'>(null);
  const [editEventOrgSelected, setEditEventOrgSelected] = useState<CompanyInfo | null>(null);

  const searchEditEventOrgMutation = useMutation({
    mutationFn: (tin: string) => festivalApi.searchCompanyByTin(tin),
    onSuccess: (data) => setEditEventOrgFound(data),
    onError: () => setEditEventOrgFound('not-found'),
  });

  const openEditEvent = async (ev: { id: string; type: string; format: string; startDate: string; startTime?: string; approximateDuration?: number; description?: string; maxParticipants?: number; teamSize?: number; organizerCompanyId?: number }) => {
    setEditingEventId(ev.id);
    setEditEventType(ev.type);
    setEditEventDesc(ev.description ?? '');
    setEditEventDate(ev.startDate ?? '');
    setEditEventTime(ev.startTime ? ev.startTime.substring(0, 5) : '');
    setEditEventDuration(ev.approximateDuration ? String(ev.approximateDuration) : '');
    setEditEventFormat(ev.format === 'TEAM' ? 'TEAM' : 'SOLO');
    setEditEventMax(String(ev.maxParticipants ?? ''));
    setEditEventTeamSize(String(ev.teamSize ?? ''));
    setEditEventOrgTin('');
    setEditEventOrgFound(null);
    if (ev.organizerCompanyId) {
      try {
        const company = await festivalApi.getCompanyById(ev.organizerCompanyId);
        setEditEventOrgSelected(company);
      } catch {
        setEditEventOrgSelected({ id: ev.organizerCompanyId, tin: '', full_name: `Компания #${ev.organizerCompanyId}`, brand_name: '', email: '', is_active: true });
      }
    } else {
      setEditEventOrgSelected(null);
    }
    setRewardModalEventId(null);
  };

  const [rewardModalEventId, setRewardModalEventId] = useState<string | null>(null);
  const [prizePlacesCount, setPrizePlacesCount] = useState(0);
  const [placeRewards, setPlaceRewards] = useState<Record<number, DictItem | null>>({});
  const [placeDescriptions, setPlaceDescriptions] = useState<Record<number, string>>({});

  const { data: dictRewards = [] } = useQuery({
    queryKey: ['dict-rewards'],
    queryFn: eventApi.getDictionaryRewards,
    enabled: !!rewardModalEventId,
  });

  const { data: existingRewards = [], refetch: refetchRewards } = useQuery({
    queryKey: ['event-rewards', rewardModalEventId],
    queryFn: () => eventApi.getRewardsByEvent(rewardModalEventId!),
    enabled: !!rewardModalEventId,
  });

  const openRewardModal = (eventId: string) => {
    setRewardModalEventId(eventId);
    setEditingEventId(null);
    setPrizePlacesCount(0);
    setPlaceRewards({});
    setPlaceDescriptions({});
  };

  const addRewardMutation = useMutation({
    mutationFn: ({ place, reward, description }: { place: number; reward: DictItem; description?: string }) =>
      eventApi.addRewardToEvent(rewardModalEventId!, { rewardId: reward.id, place, description }),
    onSuccess: (_, { place }) => {
      setPlaceRewards(p => ({ ...p, [place]: null }));
      setPlaceDescriptions(d => { const next = { ...d }; delete next[place]; return next; });
      refetchRewards();
    },
  });

  const deleteRewardMutation = useMutation({
    mutationFn: (eventRewardId: string) => eventApi.deleteReward(eventRewardId),
    onSuccess: () => refetchRewards(),
  });

  const addEventMutation = useMutation({
    mutationFn: () => eventApi.createEvent({
      festivalId: festivalId,
      eventId: selectedEventDict!.id,
      type: newEventType,
      description: newEventDesc || undefined,
      format: newEventFormat,
      maxParticipants: Number(newEventMax) || 0,
      teamSize: newEventFormat === 'TEAM' ? Number(newEventTeamSize) : undefined,
      startDate: newEventDate,
      startTime: newEventTime || '00:00',
      approximateDuration: newEventDuration ? Number(newEventDuration) : undefined,
      organizerCompanyId: newEventOrgSelected?.id ?? undefined,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-edit-events', festivalId] });
      qc.invalidateQueries({ queryKey: ['dict-events'] });
      setSelectedEventDict(null); setNewEventType(''); setNewEventDesc('');
      setNewEventDate(''); setNewEventTime(''); setNewEventDuration(''); setNewEventMax(''); setNewEventTeamSize('');
      setNewEventOrgTin(''); setNewEventOrgFound(null); setNewEventOrgSelected(null);
    },
  });

  const updateEventMutation = useMutation({
    mutationFn: () => eventApi.updateEvent(editingEventId!, {
      type: editEventType,
      description: editEventDesc || undefined,
      format: editEventFormat,
      maxParticipants: Number(editEventMax) || 0,
      teamSize: editEventFormat === 'TEAM' ? Number(editEventTeamSize) : undefined,
      startDate: editEventDate,
      startTime: editEventTime || '00:00',
      approximateDuration: editEventDuration ? Number(editEventDuration) : undefined,
      organizerCompanyId: editEventOrgSelected?.id ?? undefined,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-edit-events', festivalId] });
      setEditingEventId(null);
    },
  });

  const deleteEventMutation = useMutation({
    mutationFn: (eventId: string) => eventApi.deleteEvent(eventId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit-events', festivalId] }),
  });

  const [photoEventId, setPhotoEventId] = useState<string | null>(null);
  const [photoKey, setPhotoKey] = useState(0);
  const eventPhotoFileRef = useRef<HTMLInputElement>(null);

  const uploadEventPhotoMutation = useMutation({
    mutationFn: (file: File) => eventApi.uploadEventPhoto(photoEventId!, file),
    onSuccess: () => setPhotoKey(k => k + 1),
  });

  const deleteEventPhotoMutation = useMutation({
    mutationFn: () => eventApi.deleteEventPhoto(photoEventId!),
    onSuccess: () => setPhotoKey(k => k + 1),
  });

  const [photoRewardId, setPhotoRewardId] = useState<string | null>(null);
  const [rewardPhotoKey, setRewardPhotoKey] = useState(0);
  const rewardPhotoFileRef = useRef<HTMLInputElement>(null);

  const uploadRewardPhotoMutation = useMutation({
    mutationFn: (file: File) => eventApi.uploadRewardPhoto(photoRewardId!, file),
    onSuccess: () => setRewardPhotoKey(k => k + 1),
  });

  const deleteRewardPhotoMutation = useMutation({
    mutationFn: () => eventApi.deleteRewardPhoto(photoRewardId!),
    onSuccess: () => setRewardPhotoKey(k => k + 1),
  });

  return (
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
              <label>Тип *</label>
              <select value={newEventType} onChange={e => setNewEventType(e.target.value)}>
                <option value="">— Тип —</option>
                {EVENT_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
              </select>
            </div>
          </div>

          <div className={styles.row}>
            <div className={styles.field}>
              <label>Формат *</label>
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
              <label>Время начала *</label>
              <input type="time" value={newEventTime} onChange={e => setNewEventTime(e.target.value)} />
            </div>
          </div>

          <div className={styles.field}>
            <label>Описание *</label>
            <textarea value={newEventDesc} onChange={e => setNewEventDesc(e.target.value)} placeholder="Описание мероприятия" rows={3} />
          </div>

          <div className={styles.row}>
            <div className={styles.field}>
              <label>Длительность (мин.)</label>
              <input type="number" min={1} value={newEventDuration} onChange={e => setNewEventDuration(e.target.value)} placeholder="60" />
            </div>
          </div>

          <OrganizerBlock
            label="Компания-организатор мероприятия"
            tin={newEventOrgTin} onTinChange={tin => { setNewEventOrgTin(tin); setNewEventOrgFound(null); }}
            found={newEventOrgFound} searching={searchNewEventOrgMutation.isPending}
            onSearch={() => searchNewEventOrgMutation.mutate(newEventOrgTin.trim())}
            selected={newEventOrgSelected}
            onSelect={c => { setNewEventOrgSelected(c); setNewEventOrgFound(null); }}
            onClear={() => { setNewEventOrgSelected(null); setNewEventOrgFound(null); setNewEventOrgTin(''); }}
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Button size="sm" loading={addEventMutation.isPending}
              disabled={!selectedEventDict || !newEventType || !newEventDate || !newEventMax || !newEventDesc.trim() || !newEventTime}
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
            <div key={ev.id}>
              <div className={styles.listItem}>
                <div style={{ flex: 1 }}>
                  <p className={styles.listItemName}>{ev.name}</p>
                  <p className={styles.listItemSub}>
                    {EVENT_TYPES.find(t => t.value === ev.type)?.label ?? ev.type}
                    {' · '}{ev.format === 'TEAM' ? 'Командное' : 'Одиночное'}
                    {ev.startDate ? ` · ${fmtDate(ev.startDate)}` : ''}
                  </p>
                </div>
                <div style={{ display: 'flex', gap: 6 }}>
                  <Button size="sm" variant="secondary"
                    onClick={() => rewardModalEventId === ev.id ? setRewardModalEventId(null) : openRewardModal(ev.id)}>
                    <Trophy size={14} /> Награды
                  </Button>
                  <Button size="sm" variant="secondary"
                    onClick={() => { setPhotoEventId(photoEventId === ev.id ? null : ev.id); setRewardModalEventId(null); }}>
                    <Image size={14} />
                  </Button>
                  {!isPublished && (
                    <>
                      <Button size="sm" variant="secondary"
                        onClick={() => editingEventId === ev.id ? setEditingEventId(null) : openEditEvent(ev)}>
                        <Pencil size={14} />
                      </Button>
                      <Button variant="danger" size="sm" loading={deleteEventMutation.isPending}
                        onClick={() => deleteEventMutation.mutate(ev.id)}>
                        <Trash2 size={14} />
                      </Button>
                    </>
                  )}
                </div>
              </div>

              {editingEventId === ev.id && !isPublished && (
                <div className={styles.form} style={{ margin: '8px 0 4px', padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
                  <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)', marginBottom: 8 }}>Редактировать мероприятие</p>
                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Тип *</label>
                      <select value={editEventType} onChange={e => setEditEventType(e.target.value)}>
                        <option value="">— Тип —</option>
                        {EVENT_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
                      </select>
                    </div>
                    <div className={styles.field}>
                      <label>Формат *</label>
                      <select value={editEventFormat} onChange={e => setEditEventFormat(e.target.value as 'SOLO' | 'TEAM')}>
                        <option value="SOLO">Одиночное</option>
                        <option value="TEAM">Командное</option>
                      </select>
                    </div>
                  </div>
                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Макс. участников *</label>
                      <input type="number" min={1} value={editEventMax} onChange={e => setEditEventMax(e.target.value)} />
                    </div>
                    {editEventFormat === 'TEAM' && (
                      <div className={styles.field}>
                        <label>Размер команды</label>
                        <input type="number" min={2} value={editEventTeamSize} onChange={e => setEditEventTeamSize(e.target.value)} />
                      </div>
                    )}
                  </div>
                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Дата *</label>
                      <input type="date" value={editEventDate} onChange={e => setEditEventDate(e.target.value)} />
                    </div>
                    <div className={styles.field}>
                      <label>Время начала *</label>
                      <input type="time" value={editEventTime} onChange={e => setEditEventTime(e.target.value)} />
                    </div>
                  </div>
                  <div className={styles.field}>
                    <label>Описание *</label>
                    <textarea value={editEventDesc} onChange={e => setEditEventDesc(e.target.value)} placeholder="Описание мероприятия" rows={3} />
                  </div>
                  <div className={styles.field}>
                    <label>Длительность (мин.)</label>
                    <input type="number" min={1} value={editEventDuration} onChange={e => setEditEventDuration(e.target.value)} placeholder="60" />
                  </div>

                  <OrganizerBlock
                    label="Компания-организатор мероприятия"
                    tin={editEventOrgTin} onTinChange={tin => { setEditEventOrgTin(tin); setEditEventOrgFound(null); }}
                    found={editEventOrgFound} searching={searchEditEventOrgMutation.isPending}
                    onSearch={() => searchEditEventOrgMutation.mutate(editEventOrgTin.trim())}
                    selected={editEventOrgSelected}
                    onSelect={c => { setEditEventOrgSelected(c); setEditEventOrgFound(null); }}
                    onClear={() => { setEditEventOrgSelected(null); setEditEventOrgFound(null); setEditEventOrgTin(''); }}
                  />

                  <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                    <Button size="sm" variant="secondary" onClick={() => setEditingEventId(null)}>Отмена</Button>
                    <Button size="sm" loading={updateEventMutation.isPending}
                      disabled={!editEventType || !editEventDate || !editEventMax || !editEventDesc.trim() || !editEventTime}
                      onClick={() => updateEventMutation.mutate()}>
                      Сохранить
                    </Button>
                  </div>
                </div>
              )}

              {rewardModalEventId === ev.id && (
                <div style={{ margin: '8px 0 4px', padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                    <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)' }}>
                      <Trophy size={14} style={{ display: 'inline', marginRight: 6 }} />
                      Награды мероприятия
                    </p>
                    <button style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }} onClick={() => setRewardModalEventId(null)}>
                      <X size={16} />
                    </button>
                  </div>

                  <div className={styles.field} style={{ marginBottom: 12 }}>
                    <label>Количество призовых мест</label>
                    <input
                      type="number" min={0} max={20}
                      value={prizePlacesCount}
                      onChange={e => {
                        const n = Math.max(0, Math.min(20, Number(e.target.value)));
                        setPrizePlacesCount(n);
                        setPlaceRewards(prev => {
                          const next: Record<number, DictItem | null> = {};
                          for (let i = 1; i <= n; i++) next[i] = prev[i] ?? null;
                          return next;
                        });
                      }}
                      style={{ width: 100 }}
                    />
                  </div>

                  {prizePlacesCount > 0 && (
                    <div style={{ marginBottom: 16 }}>
                      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
                        <thead>
                          <tr style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                            <th style={{ textAlign: 'left', padding: '6px 8px', width: 70, color: 'var(--text-muted)', fontWeight: 600 }}>Место</th>
                            <th style={{ textAlign: 'left', padding: '6px 8px', color: 'var(--text-muted)', fontWeight: 600 }}>Награда</th>
                            <th style={{ textAlign: 'left', padding: '6px 8px', color: 'var(--text-muted)', fontWeight: 600 }}>Описание</th>
                            <th style={{ width: 52 }} />
                          </tr>
                        </thead>
                        <tbody>
                          {Array.from({ length: prizePlacesCount }, (_, i) => i + 1).map(place => (
                            <tr key={place} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                              <td style={{ padding: '8px', fontWeight: 600 }}>
                                {place === 1 ? '🥇 1' : place === 2 ? '🥈 2' : place === 3 ? '🥉 3' : `${place}`}
                              </td>
                              <td style={{ padding: '8px' }}>
                                <DictCombobox
                                  items={dictRewards}
                                  value={placeRewards[place] ?? null}
                                  onChange={item => setPlaceRewards(p => ({ ...p, [place]: item }))}
                                  onCreateNew={name => eventApi.createDictionaryReward(name).then(newId => {
                                    qc.invalidateQueries({ queryKey: ['dict-rewards'] });
                                    return newId;
                                  })}
                                  placeholder="Выберите или создайте..."
                                />
                              </td>
                              <td style={{ padding: '8px' }}>
                                <input
                                  placeholder="Описание *"
                                  value={placeDescriptions[place] ?? ''}
                                  onChange={e => setPlaceDescriptions(d => ({ ...d, [place]: e.target.value }))}
                                  style={{ width: '100%' }}
                                />
                              </td>
                              <td style={{ padding: '8px' }}>
                                <Button size="sm"
                                  disabled={!placeRewards[place] || !placeDescriptions[place]?.trim()}
                                  loading={addRewardMutation.isPending}
                                  onClick={() => placeRewards[place] && addRewardMutation.mutate({
                                    place,
                                    reward: placeRewards[place]!,
                                    description: placeDescriptions[place] || undefined,
                                  })}>
                                  <Plus size={13} />
                                </Button>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}

                  {existingRewards.length > 0 && (
                    <div>
                      <p style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-muted)', marginBottom: 8, fontFamily: 'var(--font-heading)' }}>Добавленные награды</p>
                      {existingRewards
                        .slice()
                        .sort((a: EventReward, b: EventReward) => (a.place ?? 99) - (b.place ?? 99))
                        .map((rew: EventReward) => (
                          <div key={rew.eventRewardId} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '8px 0' }}>
                              <div style={{ flex: 1 }}>
                                <span style={{ fontWeight: 600, marginRight: 8 }}>
                                  {rew.place != null ? `${ordinalRu(rew.place)} · ` : ''}
                                </span>
                                <span>{rew.rewardName}</span>
                                {rew.description && <span style={{ color: 'var(--text-muted)', fontSize: 12, marginLeft: 6 }}>— {rew.description}</span>}
                              </div>
                              <Button size="sm" variant="secondary"
                                onClick={() => setPhotoRewardId(photoRewardId === String(rew.eventRewardId) ? null : String(rew.eventRewardId))}>
                                <Image size={13} />
                              </Button>
                              <Button size="sm" variant="danger"
                                loading={deleteRewardMutation.isPending}
                                onClick={() => deleteRewardMutation.mutate(String(rew.eventRewardId))}>
                                <Trash2 size={13} />
                              </Button>
                            </div>
                            {photoRewardId === String(rew.eventRewardId) && (
                              <div style={{ paddingBottom: 10 }}>
                                <PhotoImg
                                  key={rewardPhotoKey}
                                  src={`/api/files/EVENT_REWARD/${rew.eventRewardId}?k=${rewardPhotoKey}`}
                                />
                                <div style={{ display: 'flex', gap: 8 }}>
                                  <Button size="sm" loading={uploadRewardPhotoMutation.isPending} onClick={() => rewardPhotoFileRef.current?.click()}>
                                    <Image size={13} /> Загрузить фото
                                  </Button>
                                  <input ref={rewardPhotoFileRef} type="file" accept="image/*" style={{ display: 'none' }}
                                    onChange={e => { const f = e.target.files?.[0]; if (f) { uploadRewardPhotoMutation.mutate(f); e.target.value = ''; } }} />
                                  <Button size="sm" variant="danger" loading={deleteRewardPhotoMutation.isPending} onClick={() => deleteRewardPhotoMutation.mutate()}>
                                    <Trash2 size={13} />
                                  </Button>
                                </div>
                              </div>
                            )}
                          </div>
                        ))}
                    </div>
                  )}
                  {existingRewards.length === 0 && prizePlacesCount === 0 && (
                    <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>Нет наград. Укажите количество призовых мест выше.</p>
                  )}
                </div>
              )}

              {photoEventId === ev.id && (
                <div style={{ margin: '8px 0 4px', padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                    <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)' }}>
                      <Image size={14} style={{ display: 'inline', marginRight: 6 }} />
                      Фотография мероприятия
                    </p>
                    <button style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }} onClick={() => setPhotoEventId(null)}>
                      <X size={16} />
                    </button>
                  </div>
                  <img
                    key={photoKey}
                    src={`/api/files/FESTIVAL_EVENT/${ev.id}?k=${photoKey}`}
                    alt="Фото мероприятия"
                    onError={e => { (e.target as HTMLImageElement).style.display = 'none'; }}
                    onLoad={e => { (e.target as HTMLImageElement).style.display = 'block'; }}
                    style={{ width: '100%', maxHeight: 200, objectFit: 'cover', borderRadius: 8, background: 'var(--surface-secondary)', display: 'none' }}
                  />
                  <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                    <Button loading={uploadEventPhotoMutation.isPending} onClick={() => eventPhotoFileRef.current?.click()}>
                      <Image size={14} /> Загрузить фото
                    </Button>
                    <input ref={eventPhotoFileRef} type="file" accept="image/*" style={{ display: 'none' }}
                      onChange={e => { const f = e.target.files?.[0]; if (f) { uploadEventPhotoMutation.mutate(f); e.target.value = ''; } }} />
                    <Button variant="danger" loading={deleteEventPhotoMutation.isPending} onClick={() => deleteEventPhotoMutation.mutate()}>
                      <Trash2 size={14} />
                    </Button>
                  </div>
                </div>
              )}
            </div>
          ))
        }
      </div>
    </Card>
  );
}
