import { useNavigate } from 'react-router-dom';
import { ChevronLeft, BarChart2, Calendar, MapPin, Users, FileText, Building2 } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import styles from './CompanyFestivalAnalyticsPage.module.css';


const FESTIVAL = {
  name: 'СПб: Кубики и сенсоры',
  dateStart: '17.07.2026',
  timeStart: '12:00',
  dateEnd: '19.07.2026',
  timeEnd: '21:00',
  city: 'Санкт-Петербург',
  venue: 'КВЦ «Экспофорум»',
  maxParticipants: 220,
  description: 'Уютный фестиваль мобильных и настольных коллекционных карточных игр.',
  organizers: ['ДайсМастерс', 'ТапСфера'],
  ticketsTotal: 220,
  ticketsSold: 215,
  ticketsLeft: 5,
  actualAttendees: 198,
};

const EVENTS = [
  { name: 'Гвинт Mobile',                 type: 'Турнир',        organizer: 'ТапСфера',    max: 64,  registered: 64,  attended: 58 },
  { name: 'Монополия в реальной жизни',   type: 'Квест',         organizer: 'ДайсМастерс', max: 40,  registered: 40,  attended: 36 },
  { name: 'Новинки TapSphere',            type: 'Демонстрация',  organizer: 'ТапСфера',    max: 120, registered: 115, attended: 104 },
  { name: 'MTG: Modern',                  type: 'Турнир',        organizer: 'ДайсМастерс', max: 100, registered: 92,  attended: 85 },
  { name: 'Баланс в ККИ',                 type: 'Мастер-класс',  organizer: 'ТапСфера',    max: 50,  registered: 46,  attended: 39 },
];


function DonutChart({ pct, color, label }: { pct: number; color: string; label: string }) {
  return (
    <div className={styles.donutWrap}>
      <div
        className={styles.donutChart}
        style={{
          background: `conic-gradient(${color} 0% ${pct}%, var(--bg-elevated) ${pct}% 100%)`,
        }}
      >
        <div className={styles.donutHole}>
          <span className={styles.donutPct} style={{ color }}>{pct}%</span>
        </div>
      </div>
      <p className={styles.donutLabel}>{label}</p>
    </div>
  );
}


function ProgressBar({ pct, color }: { pct: number; color: string }) {
  return (
    <div className={styles.progressTrack}>
      <div className={styles.progressFill} style={{ width: `${pct}%`, background: color }} />
    </div>
  );
}


export default function CompanyFestivalAnalyticsPage() {
  const navigate = useNavigate();

  const soldPct = Math.round((FESTIVAL.ticketsSold / FESTIVAL.ticketsTotal) * 100);
  const attendPct = Math.round((FESTIVAL.actualAttendees / FESTIVAL.ticketsSold) * 100);

  return (
    <Layout>
      <div className={styles.page}>

        <button className={styles.back} onClick={() => navigate(-1)}>
          <ChevronLeft size={18} /> Назад
        </button>

        {/* Title */}
        <div className={styles.titleRow}>
          <BarChart2 size={28} className={styles.titleIcon} />
          <div>
            <p className={styles.titleSup}>Аналитика фестиваля</p>
            <h1 className={styles.title}>{FESTIVAL.name}</h1>
          </div>
        </div>

        {/* Info card */}
        <div className={styles.infoCard}>
          <div className={styles.infoGrid}>
            <div className={styles.infoRow}>
              <Calendar size={15} className={styles.infoIcon} />
              <span className={styles.infoLabel}>Даты и время</span>
              <span className={styles.infoValue}>
                {FESTIVAL.dateStart} {FESTIVAL.timeStart} — {FESTIVAL.dateEnd} {FESTIVAL.timeEnd}
              </span>
            </div>
            <div className={styles.infoRow}>
              <MapPin size={15} className={styles.infoIcon} />
              <span className={styles.infoLabel}>Город, площадка</span>
              <span className={styles.infoValue}>{FESTIVAL.city}, {FESTIVAL.venue}</span>
            </div>
            <div className={styles.infoRow}>
              <Users size={15} className={styles.infoIcon} />
              <span className={styles.infoLabel}>Макс. участников</span>
              <span className={styles.infoValue}>{FESTIVAL.maxParticipants} человек</span>
            </div>
            <div className={styles.infoRow}>
              <FileText size={15} className={styles.infoIcon} />
              <span className={styles.infoLabel}>Описание</span>
              <span className={styles.infoValue}>{FESTIVAL.description}</span>
            </div>
            <div className={styles.infoRow}>
              <Building2 size={15} className={styles.infoIcon} />
              <span className={styles.infoLabel}>Организаторы</span>
              <span className={styles.infoValue}>{FESTIVAL.organizers.join(', ')}</span>
            </div>
          </div>
        </div>

        {/* Stat boxes */}
        <div className={styles.statsRow}>
          <div className={styles.statBox}>
            <span className={styles.statNum}>{FESTIVAL.ticketsTotal}</span>
            <span className={styles.statLabel}>всего мест</span>
          </div>
          <div className={[styles.statBox, styles.statBoxAccent].join(' ')}>
            <span className={styles.statNum}>{FESTIVAL.ticketsSold}</span>
            <span className={styles.statLabel}>куплено билетов</span>
          </div>
          <div className={styles.statBox}>
            <span className={styles.statNum}>{FESTIVAL.ticketsLeft}</span>
            <span className={styles.statLabel}>осталось в продаже</span>
          </div>
          <div className={[styles.statBox, styles.statBoxGreen].join(' ')}>
            <span className={styles.statNum}>{FESTIVAL.actualAttendees}</span>
            <span className={styles.statLabel}>пришло на фестиваль</span>
          </div>
        </div>

        {/* Donut charts */}
        <div className={styles.chartsSection}>
          <DonutChart
            pct={soldPct}
            color="var(--accent-primary)"
            label="Продано билетов от общей вместимости"
          />
          <div className={styles.chartsDivider} />
          <DonutChart
            pct={attendPct}
            color="#10b981"
            label="Явка от числа купивших билет"
          />
        </div>

        {/* Events */}
        <div className={styles.eventsSection}>
          <h2 className={styles.sectionTitle}>Мероприятия по популярности</h2>
          <p className={styles.sectionHint}>
            Отсортированы по доле зарегистрированных от максимального числа мест
          </p>

          <div className={styles.eventList}>
            {EVENTS.map((ev, i) => {
              const regPct = Math.round((ev.registered / ev.max) * 100);
              const attPct = Math.round((ev.attended / ev.registered) * 100);
              const free = ev.max - ev.registered;

              return (
                <div key={ev.name} className={styles.eventRow}>
                  <div className={styles.eventHead}>
                    <span className={styles.eventRank}>№{i + 1}</span>
                    <div>
                      <span className={styles.eventName}>{ev.name}</span>
                      <div className={styles.eventMeta}>
                        <span className={styles.eventTypeBadge}>{ev.type}</span>
                        <span className={styles.eventOrg}>{ev.organizer}</span>
                        {free === 0 && <span className={styles.soldOut}>Все места заняты</span>}
                      </div>
                    </div>
                  </div>

                  <div className={styles.eventStats}>
                    <div className={styles.eventStat}>
                      <div className={styles.eventStatHeader}>
                        <span className={styles.eventStatLabel}>Регистрация</span>
                        <span className={styles.eventStatNums}>
                          {ev.registered}/{ev.max} мест · <b>{regPct}%</b>
                        </span>
                      </div>
                      <ProgressBar pct={regPct} color="var(--accent-primary)" />
                    </div>

                    <div className={styles.eventStat}>
                      <div className={styles.eventStatHeader}>
                        <span className={styles.eventStatLabel}>Явка из зарегистрированных</span>
                        <span className={styles.eventStatNums}>
                          {ev.attended}/{ev.registered} чел. · <b>{attPct}%</b>
                        </span>
                      </div>
                      <ProgressBar pct={attPct} color="#10b981" />
                    </div>

                    <div className={styles.eventFreeRow}>
                      <span className={styles.eventFreeNum}>{free}</span>
                      <span className={styles.eventFreeLabel}>
                        {free === 0 ? 'свободных мест не осталось' : 'свободных мест осталось'}
                      </span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

      </div>
    </Layout>
  );
}
