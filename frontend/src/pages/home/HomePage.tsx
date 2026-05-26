import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { Search, MapPin, Calendar, X, Trophy, Store } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Tabs from '../../components/ui/Tabs';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import EmptyState from '../../components/ui/EmptyState';
import CityCombobox from '../../components/CityCombobox';
import { festivalApi } from '../../api/festival';
import { eventApi } from '../../api/event';
import type { City, Festival, FestivalEvent, Stand } from '../../types';
import styles from './HomePage.module.css';

function useDebounce<T>(value: T, delay: number): T {
  const [d, setD] = useState(value);
  useState(() => {
    const t = setTimeout(() => setD(value), delay);
    return () => clearTimeout(t);
  });
  return d;
}

function formatDate(d: string) {
  return new Date(d).toLocaleDateString('ru-RU', { day: 'numeric', month: 'short', year: 'numeric' });
}

function FestivalCard({ festival }: { festival: Festival }) {
  const navigate = useNavigate();
  const [imgErr, setImgErr] = useState(false);
  return (
    <Card hoverable padding="none" onClick={() => navigate(`/festival/${festival.id}`)}>
      <div className={styles.cardCover}>
        {!imgErr
          ? <img src={`/api/files/FESTIVAL_LOCATION/${festival.id}`} alt={festival.name} onError={() => setImgErr(true)} />
          : <div className={styles.cardCoverPlaceholder}><Trophy size={32} /></div>
        }
        <Badge status="info" className={styles.ageBadge}>{festival.ageRating}+</Badge>
      </div>
      <div className={styles.cardBody}>
        <h3 className={styles.cardTitle}>{festival.name}</h3>
        <div className={styles.cardMeta}>
          <span>
            <MapPin size={12} />
            {festival.cityName}{festival.platformName ? ` · ${festival.platformName}` : ''}
          </span>
          {festival.address && (
            <span className={styles.cardMetaIndent}>{festival.address}</span>
          )}
          <span><Calendar size={12} /> {formatDate(festival.startDate)} — {formatDate(festival.endDate)}</span>
        </div>
        <Button variant="secondary" size="sm" fullWidth onClick={e => { e.stopPropagation(); navigate(`/festival/${festival.id}`); }}>
          Подробнее
        </Button>
      </div>
    </Card>
  );
}

function EventCard({ event }: { event: FestivalEvent }) {
  const navigate = useNavigate();
  const typeLabels: Record<string, string> = { TOURNAMENT: 'Турнир', QUEST: 'Квест', MASTERCLASS: 'Мастер-класс', DEMO: 'Демонстрация' };
  return (
    <Card hoverable padding="md" onClick={() => navigate(`/event/${event.id}`)}>
      <div className={styles.eventCardHeader}>
        <Badge status="info">{typeLabels[event.type] ?? event.type}</Badge>
        <Badge status={event.format === 'SOLO' ? 'active' : 'draft'}>{event.format === 'SOLO' ? 'Одиночное' : 'Командное'}</Badge>
      </div>
      <h3 className={styles.cardTitle} style={{ marginTop: 8 }}>{event.name}</h3>
      <div className={styles.cardMeta}>
        <span><Calendar size={12} /> {formatDate(event.startDate)}</span>
        {event.companyName && <span>{event.companyName}</span>}
      </div>
      <Button variant="ghost" size="sm" fullWidth onClick={e => { e.stopPropagation(); navigate(`/event/${event.id}`); }}>
        Подробнее
      </Button>
    </Card>
  );
}

function StandCard({ stand }: { stand: Stand }) {
  const typeLabels: Record<string, string> = { TRADE: 'Торговый', DEMO: 'Демонстрационный' };
  return (
    <Card hoverable padding="none">
      <div className={styles.cardCover}>
        {stand.photoUrl
          ? <img src={stand.photoUrl} alt={stand.name} />
          : <div className={styles.cardCoverPlaceholder}><Store size={32} /></div>
        }
      </div>
      <div className={styles.cardBody}>
        <h3 className={styles.cardTitle}>{stand.name}</h3>
        <Badge status={stand.type === 'TRADE' ? 'active' : 'info'}>{typeLabels[stand.type]}</Badge>
        <div className={styles.cardMeta} style={{ marginTop: 8 }}>
          {stand.companyName && <span>{stand.companyName}</span>}
          {stand.festivalName && <span>{stand.festivalName}{stand.eventName ? ` · ${stand.eventName}` : ''}</span>}
        </div>
      </div>
    </Card>
  );
}

function SkeletonCard() {
  return (
    <div className={styles.skeletonCard}>
      <div className={`skeleton ${styles.skeletonImg}`} />
      <div className={styles.skeletonBody}>
        <div className={`skeleton ${styles.skeletonLine}`} style={{ width: '70%' }} />
        <div className={`skeleton ${styles.skeletonLine}`} style={{ width: '50%' }} />
        <div className={`skeleton ${styles.skeletonLine}`} style={{ width: '40%' }} />
      </div>
    </div>
  );
}

function FestivalsTab() {
  const [search, setSearch] = useState('');
  const [city, setCity] = useState<City | null>(null);
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [page, setPage] = useState(0);

  const dSearch = useDebounce(search, 400);

  const { data, isLoading } = useQuery({
    queryKey: ['festivals', dSearch, city?.id, dateFrom, dateTo, page],
    queryFn: () => festivalApi.list({ name: dSearch, cityId: city?.id, dateFrom, dateTo, page, size: 12, status: 'APPROVED' } as Parameters<typeof festivalApi.list>[0]),
  });

  const hasFilters = search || city || dateFrom || dateTo;

  return (
    <div>
      <div className={styles.filters}>
        <div className={styles.searchWrap}>
          <Search size={16} className={styles.searchIcon} />
          <input className={styles.searchInput} placeholder="Поиск по названию..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <div className={styles.filterCity}>
          <CityCombobox value={city} onChange={setCity} placeholder="Город" />
        </div>
        <input type="date" className={styles.datePicker} value={dateFrom} onChange={e => setDateFrom(e.target.value)} title="Дата начала" />
        <input type="date" className={styles.datePicker} value={dateTo} onChange={e => setDateTo(e.target.value)} title="Дата окончания" />
        {hasFilters && (
          <Button variant="ghost" size="sm" onClick={() => { setSearch(''); setCity(null); setDateFrom(''); setDateTo(''); }}>
            <X size={14} /> Сбросить
          </Button>
        )}
      </div>

      {isLoading ? (
        <div className={styles.grid}>
          {Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)}
        </div>
      ) : !data?.content.length ? (
        <EmptyState icon={Trophy} message="Фестивали не найдены" description={hasFilters ? 'Попробуйте изменить фильтры' : 'Пока нет доступных фестивалей'} />
      ) : (
        <>
          <div className={styles.grid}>
            {data.content.map((f, i) => (
              <motion.div key={f.id} initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}>
                <FestivalCard festival={f} />
              </motion.div>
            ))}
          </div>
          {!data.last && (
            <div className={styles.loadMore}>
              <Button variant="secondary" onClick={() => setPage(p => p + 1)}>Загрузить ещё</Button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

const EVENT_TYPES = [
  { value: '', label: 'Все типы' },
  { value: 'TOURNAMENT', label: 'Турнир' },
  { value: 'QUEST', label: 'Квест' },
  { value: 'MASTERCLASS', label: 'Мастер-класс' },
  { value: 'DEMO', label: 'Демонстрация' },
];

function EventsTab() {
  const [search, setSearch] = useState('');
  const [city, setCity] = useState<City | null>(null);
  const [dateFrom, setDateFrom] = useState('');
  const [eventType, setEventType] = useState('');
  const dSearch = useDebounce(search, 400);
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ['events', dSearch, city?.id, dateFrom, eventType, page],
    queryFn: () => eventApi.listEvents({ name: dSearch, cityId: city?.id, dateFrom, type: eventType || undefined, page, size: 12 }),
  });

  const hasFilters = search || city || dateFrom || eventType;
  const grouped = (data?.content ?? []).reduce<Record<string, FestivalEvent[]>>((acc, e) => {
    const k = e.festivalName || 'Мероприятия';
    if (!acc[k]) acc[k] = [];
    acc[k].push(e);
    return acc;
  }, {});

  if (isLoading) return <Spinner centered />;

  if (!data?.content.length) {
    return (
      <>
        <div className={styles.filters}>
          <div className={styles.searchWrap}>
            <Search size={16} className={styles.searchIcon} />
            <input className={styles.searchInput} placeholder="Поиск..." value={search} onChange={e => setSearch(e.target.value)} />
          </div>
          <div className={styles.filterCity}>
            <CityCombobox value={city} onChange={setCity} placeholder="Город" />
          </div>
          <input type="date" className={styles.datePicker} value={dateFrom} onChange={e => setDateFrom(e.target.value)} title="Дата начала" />
          <select className={styles.filterSelect} value={eventType} onChange={e => setEventType(e.target.value)}>
            {EVENT_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
          </select>
          {hasFilters && <Button variant="ghost" size="sm" onClick={() => { setSearch(''); setCity(null); setDateFrom(''); setEventType(''); }}><X size={14} /> Сбросить</Button>}
        </div>
        <EmptyState icon={Trophy} message="Мероприятий нет" description={hasFilters ? 'Попробуйте изменить фильтры' : 'Пока нет доступных мероприятий'} />
      </>
    );
  }

  return (
    <div>
      <div className={styles.filters}>
        <div className={styles.searchWrap}>
          <Search size={16} className={styles.searchIcon} />
          <input className={styles.searchInput} placeholder="Поиск..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <div className={styles.filterCity}>
          <CityCombobox value={city} onChange={setCity} placeholder="Город" />
        </div>
        <input type="date" className={styles.datePicker} value={dateFrom} onChange={e => setDateFrom(e.target.value)} title="Дата начала" />
        <select className={styles.filterSelect} value={eventType} onChange={e => setEventType(e.target.value)}>
          {EVENT_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
        </select>
        {hasFilters && <Button variant="ghost" size="sm" onClick={() => { setSearch(''); setCity(null); setDateFrom(''); setEventType(''); }}><X size={14} /> Сбросить</Button>}
      </div>
      {Object.entries(grouped).map(([fest, events]) => (
        <div key={fest} className={styles.group}>
          <h3 className={styles.groupTitle}>{fest}</h3>
          <div className={styles.grid}>
            {events.map((e, i) => (
              <motion.div key={e.id} initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}>
                <EventCard event={e} />
              </motion.div>
            ))}
          </div>
        </div>
      ))}
      {!data.last && (
        <div className={styles.loadMore}>
          <Button variant="secondary" onClick={() => setPage(p => p + 1)}>Загрузить ещё</Button>
        </div>
      )}
    </div>
  );
}

const STAND_TYPES = [
  { value: '', label: 'Все типы' },
  { value: 'TRADE', label: 'Торговый' },
  { value: 'DEMO', label: 'Демонстрационный' },
];

function StandsTab() {
  const [search, setSearch] = useState('');
  const [city, setCity] = useState<City | null>(null);
  const [dateFrom, setDateFrom] = useState('');
  const [standType, setStandType] = useState('');
  const [page, setPage] = useState(0);

  const dSearch = useDebounce(search, 400);

  const { data, isLoading } = useQuery({
    queryKey: ['stands', dSearch, city?.id, dateFrom, standType, page],
    queryFn: () => eventApi.listStands({ name: dSearch || undefined, cityId: city?.id, dateFrom, type: standType || undefined, page, size: 12 }),
  });

  const hasFilters = search || city || dateFrom || standType;

  if (isLoading) return <Spinner centered />;

  return (
    <div>
      <div className={styles.filters}>
        <div className={styles.searchWrap}>
          <Search size={16} className={styles.searchIcon} />
          <input className={styles.searchInput} placeholder="Поиск по названию..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <div className={styles.filterCity}>
          <CityCombobox value={city} onChange={setCity} placeholder="Город" />
        </div>
        <input type="date" className={styles.datePicker} value={dateFrom} onChange={e => setDateFrom(e.target.value)} title="Дата начала" />
        <select className={styles.filterSelect} value={standType} onChange={e => setStandType(e.target.value)}>
          {STAND_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
        </select>
        {hasFilters && <Button variant="ghost" size="sm" onClick={() => { setSearch(''); setCity(null); setDateFrom(''); setStandType(''); }}><X size={14} /> Сбросить</Button>}
      </div>
      {!data?.content.length
        ? <EmptyState icon={Store} message="Стенды не найдены" description={hasFilters ? 'Попробуйте изменить фильтры' : 'Пока нет доступных стендов'} />
        : (
          <>
            <div className={styles.grid}>
              {(data?.content ?? []).map((s, i) => (
                <motion.div key={s.id} initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}>
                  <StandCard stand={s} />
                </motion.div>
              ))}
            </div>
            {!data.last && (
              <div className={styles.loadMore}>
                <Button variant="secondary" onClick={() => setPage(p => p + 1)}>Загрузить ещё</Button>
              </div>
            )}
          </>
        )
      }
    </div>
  );
}

const TABS = [
  { key: 'festivals', label: 'Фестивали' },
  { key: 'events',    label: 'Мероприятия' },
  { key: 'stands',    label: 'Стенды' },
];

export default function HomePage() {
  const [tab, setTab] = useState('festivals');

  return (
    <Layout>
      <div className={styles.page}>
        <Tabs tabs={TABS} activeKey={tab} onChange={setTab} className={styles.tabs} />
        <div className={styles.content}>
          {tab === 'festivals' && <FestivalsTab />}
          {tab === 'events'    && <EventsTab />}
          {tab === 'stands'    && <StandsTab />}
        </div>
      </div>
    </Layout>
  );
}
