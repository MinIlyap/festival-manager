import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  ChevronLeft, Trophy, Award, CheckCircle, AlertCircle,
  GripVertical, Users, Star,
} from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import { eventApi, type EventReward } from '../../api/event';
import { userApi } from '../../api/user';
import styles from './CuratorAwardDistributionPage.module.css';

// ── types ────────────────────────────────────────────────────

type DragItem = {
  key: string;
  label: string;
  commandId?: number;
  userId?: number;
};

// ── helpers ──────────────────────────────────────────────────

const TYPE_LABEL: Record<string, string> = {
  TOURNAMENT:    'Турнир',
  QUEST:         'Квест',
  MASTER_CLASS:  'Мастер-класс',
  MASTERCLASS:   'Мастер-класс',
  DEMONSTRATION: 'Демонстрация',
  DEMO:          'Демонстрация',
};

function medalEmoji(place: number) {
  if (place === 1) return '🥇';
  if (place === 2) return '🥈';
  if (place === 3) return '🥉';
  return '🏅';
}

function placeOrdinal(place: number) {
  if (place === 1) return '1-е место';
  if (place === 2) return '2-е место';
  if (place === 3) return '3-е место';
  return `${place}-е место`;
}

// ── main ─────────────────────────────────────────────────────

export default function CuratorAwardDistributionPage() {
  const { eventId } = useParams<{ eventId: string }>();
  const navigate = useNavigate();

  // ── remote data ───────────────────────────────────────────

  const { data: event, isLoading: eventLoading } = useQuery({
    queryKey: ['event', eventId],
    queryFn: () => eventApi.getEventById(eventId!),
    enabled: !!eventId,
  });

  const { data: rewards = [], isLoading: rewardsLoading } = useQuery({
    queryKey: ['event-rewards-raw', eventId],
    queryFn: () => eventApi.getRewardsByEvent(eventId!),
    enabled: !!eventId,
  });

  const isSolo = event?.format === 'SOLO';
  const isTeam = event?.format === 'TEAM';

  const { data: participantIds, isLoading: pidLoading } = useQuery({
    queryKey: ['event-participants', eventId],
    queryFn: () => eventApi.getEventParticipants(eventId!),
    enabled: !!eventId && isSolo,
  });

  const { data: commands = [], isLoading: commandsLoading } = useQuery({
    queryKey: ['event-commands', eventId],
    queryFn: () => eventApi.getEventCommands(eventId!),
    enabled: !!eventId && isTeam,
  });

  const { data: participantProfiles = [], isLoading: profilesLoading } = useQuery({
    queryKey: ['participant-profiles', participantIds],
    queryFn: () =>
      participantIds!.map
        ? Promise.all(
            participantIds!.map(id =>
              userApi.getUserById(id).catch(() => ({ id, nickname: `#${id}` }))
            )
          )
        : Promise.resolve([]),
    enabled: !!(participantIds && participantIds.length > 0),
  });

  // ── local DnD state ───────────────────────────────────────

  const [slots, setSlots] = useState<{ [place: number]: DragItem | null }>({});
  const [unplaced, setUnplaced] = useState<DragItem[]>([]);
  const [initialized, setInitialized] = useState(false);

  // what is being dragged right now
  const [draggingKey, setDraggingKey] = useState<string | null>(null);
  // null = came from list; N = came from slot N
  const [draggingFromSlot, setDraggingFromSlot] = useState<number | null>(null);
  const [dragOverSlot, setDragOverSlot] = useState<number | null>(null);
  const [dragOverList, setDragOverList] = useState(false);

  // ── distribution state ────────────────────────────────────

  const [distributing, setDistributing] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // ── initialise slots once data is ready ───────────────────

  useEffect(() => {
    if (initialized || !event) return;

    let items: DragItem[] = [];

    if (isSolo && participantProfiles.length > 0) {
      items = participantProfiles.map(p => ({
        key: `user-${p.id}`,
        label: p.nickname,
        userId: p.id,
      }));
    } else if (isTeam && commands.length > 0) {
      items = commands.map(c => ({
        key: `team-${c.commandId}`,
        label: c.commandName,
        commandId: c.commandId,
      }));
    }

    if (items.length === 0) return;

    const initSlots: { [place: number]: DragItem | null } = {};
    for (let i = 1; i <= items.length; i++) initSlots[i] = null;

    setSlots(initSlots);
    setUnplaced(items);
    setInitialized(true);
  }, [initialized, event, isSolo, isTeam, participantProfiles, commands]);

  // ── DnD logic ─────────────────────────────────────────────

  function dropOnSlot(targetPlace: number) {
    if (!draggingKey) return;
    setDragOverSlot(null);

    const newSlots = { ...slots };
    const newUnplaced = [...unplaced];

    if (draggingFromSlot !== null) {
      // Slot → Slot: swap contents
      const moved = newSlots[draggingFromSlot]!;
      const displaced = newSlots[targetPlace];
      newSlots[targetPlace] = moved;
      newSlots[draggingFromSlot] = displaced ?? null;
    } else {
      // List → Slot
      const idx = newUnplaced.findIndex(u => u.key === draggingKey);
      if (idx === -1) return;
      const moved = newUnplaced.splice(idx, 1)[0];
      const displaced = newSlots[targetPlace];
      newSlots[targetPlace] = moved;
      if (displaced) newUnplaced.push(displaced);
    }

    setSlots(newSlots);
    setUnplaced(newUnplaced);
    setDraggingKey(null);
    setDraggingFromSlot(null);
  }

  function dropOnList(e: React.DragEvent) {
    e.preventDefault();
    setDragOverList(false);
    if (!draggingKey || draggingFromSlot === null) {
      setDraggingKey(null);
      return;
    }
    const item = slots[draggingFromSlot];
    if (item) {
      setSlots({ ...slots, [draggingFromSlot]: null });
      setUnplaced([...unplaced, item]);
    }
    setDraggingKey(null);
    setDraggingFromSlot(null);
  }

  // ── distribute ────────────────────────────────────────────

  async function distribute() {
    setDistributing(true);
    setError(null);
    try {
      for (const reward of rewards) {
        if (!reward.place || reward.place <= 0) continue;
        const item = slots[reward.place];
        if (!item) continue;

        if (isTeam && item.commandId != null) {
          await eventApi.distributeRewardToCommandCurator(String(reward.eventRewardId), item.commandId);
        } else if (item.userId != null) {
          await eventApi.distributeRewardToUsersCurator(String(reward.eventRewardId), [item.userId]);
        }
      }
      setDone(true);
    } catch {
      setError('Не удалось распределить награды. Попробуйте ещё раз.');
    } finally {
      setDistributing(false);
    }
  }

  // ── computed ──────────────────────────────────────────────

  const slotPlaces = Object.keys(slots).map(Number).sort((a, b) => a - b);
  const totalSlots = slotPlaces.length;
  const filledCount = slotPlaces.filter(p => slots[p] !== null).length;
  const allFilled = totalSlots > 0 && filledCount === totalSlots;

  const prizeByPlace: { [place: number]: EventReward } = {};
  rewards.forEach(r => { if (r.place != null && r.place > 0) prizeByPlace[r.place] = r; });
  const hasPrizes = Object.keys(prizeByPlace).length > 0;

  const isDataLoading =
    eventLoading || rewardsLoading ||
    (isSolo && (pidLoading || profilesLoading)) ||
    (isTeam && commandsLoading);

  // ── render: loading ───────────────────────────────────────

  if (isDataLoading && !initialized) return <Layout><Spinner centered /></Layout>;

  // ── render: no participants ───────────────────────────────

  if (!initialized && !isDataLoading) {
    return (
      <Layout>
        <div className={styles.page}>
          <button className={styles.back} onClick={() => navigate(-1)}>
            <ChevronLeft size={18} /> Назад
          </button>
          <h1 className={styles.title}>{event?.name ?? 'Мероприятие'}</h1>
          <div className={styles.emptyState}>
            <Users size={40} className={styles.emptyIcon} />
            <p className={styles.emptyText}>Нет зарегистрированных участников</p>
            <p className={styles.emptySubtext}>На это мероприятие пока никто не зарегистрировался</p>
          </div>
        </div>
      </Layout>
    );
  }

  // ── render: success ───────────────────────────────────────

  if (done) {
    return (
      <Layout>
        <div className={styles.page}>
          <button className={styles.back} onClick={() => navigate(-1)}>
            <ChevronLeft size={18} /> Назад
          </button>
          <div className={styles.successState}>
            <CheckCircle size={56} className={styles.successIcon} />
            <h2 className={styles.successTitle}>Награды распределены!</h2>
            <p className={styles.successText}>Победители получили свои награды</p>
            <Button variant="secondary" onClick={() => navigate(-1)}>Вернуться</Button>
          </div>
        </div>
      </Layout>
    );
  }

  // ── render: main ──────────────────────────────────────────

  return (
    <Layout>
      <div className={styles.page}>

        {/* Header */}
        <button className={styles.back} onClick={() => navigate(-1)}>
          <ChevronLeft size={18} /> Назад
        </button>

        <div className={styles.header}>
          <div>
            <h1 className={styles.title}>{event?.name}</h1>
            <div className={styles.headerBadges}>
              {event?.type && <Badge status="info">{TYPE_LABEL[event.type] ?? event.type}</Badge>}
              <Badge status={isTeam ? 'pending' : 'active'}>
                {isTeam ? 'Командное' : 'Одиночное'}
              </Badge>
            </div>
          </div>
          <div className={styles.counter}>
            <span className={allFilled ? styles.counterDone : styles.counterPending}>
              {filledCount}/{totalSlots}
            </span>
            <span className={styles.counterLabel}>мест заполнено</span>
          </div>
        </div>

        {/* Hint */}
        <p className={styles.hint}>
          Перетащите {isTeam ? 'команды' : 'участников'} из списка слева на нужные места в таблице
        </p>

        {/* Two-column layout */}
        <div className={styles.layout}>

          {/* ── Left: participants ─────────────── */}
          <div
            className={[styles.panel, dragOverList && styles.panelOver].join(' ')}
            onDragOver={e => { e.preventDefault(); setDragOverList(true); }}
            onDragLeave={() => setDragOverList(false)}
            onDrop={dropOnList}
          >
            <div className={styles.panelHead}>
              <h2 className={styles.panelTitle}>
                {isTeam ? 'Команды' : 'Участники'}
              </h2>
              <span className={styles.panelBadge}>{unplaced.length}</span>
            </div>

            <div className={styles.itemList}>
              {unplaced.length === 0 ? (
                <div className={styles.listEmpty}>
                  <Star size={18} />
                  <span>Все распределены по местам</span>
                </div>
              ) : (
                unplaced.map(item => (
                  <div
                    key={item.key}
                    className={[styles.dragItem, draggingKey === item.key && styles.dragging].filter(Boolean).join(' ')}
                    draggable
                    onDragStart={() => { setDraggingKey(item.key); setDraggingFromSlot(null); }}
                    onDragEnd={() => { setDraggingKey(null); setDraggingFromSlot(null); }}
                  >
                    <GripVertical size={14} className={styles.grip} />
                    <span>{item.label}</span>
                    {isTeam && <Users size={12} className={styles.teamIcon} />}
                  </div>
                ))
              )}
            </div>
          </div>

          {/* ── Right: slots table ─────────────── */}
          <div className={styles.slotsPanel}>
            <div className={styles.panelHead}>
              <h2 className={styles.panelTitle}>Таблица мест</h2>
              <span className={styles.panelBadge}>{totalSlots}</span>
            </div>

            <div className={styles.slotList}>
              {slotPlaces.map(place => {
                const item = slots[place];
                const prize = prizeByPlace[place];
                const isOver = dragOverSlot === place;

                return (
                  <div
                    key={place}
                    className={[
                      styles.slot,
                      prize && styles.slotPrize,
                      place === 1 && styles.slotGold,
                      place === 2 && styles.slotSilver,
                      place === 3 && styles.slotBronze,
                      isOver && styles.slotOver,
                    ].filter(Boolean).join(' ')}
                    onDragOver={e => { e.preventDefault(); setDragOverSlot(place); }}
                    onDragLeave={() => setDragOverSlot(null)}
                    onDrop={e => { e.preventDefault(); dropOnSlot(place); }}
                  >
                    {/* Place label */}
                    <div className={styles.slotLabel}>
                      {prize
                        ? <span className={styles.medal}>{medalEmoji(place)}</span>
                        : <span className={styles.placeNum}>{place}</span>
                      }
                      <div className={styles.slotInfo}>
                        <span className={styles.slotOrdinal}>{placeOrdinal(place)}</span>
                        {prize && (
                          <span className={styles.slotReward}>
                            <Trophy size={10} /> {prize.rewardName}
                          </span>
                        )}
                      </div>
                    </div>

                    {/* Drop zone */}
                    <div className={styles.dropZone}>
                      {item ? (
                        <div
                          className={[styles.placedItem, draggingKey === item.key && styles.dragging].filter(Boolean).join(' ')}
                          draggable
                          onDragStart={e => { e.stopPropagation(); setDraggingKey(item.key); setDraggingFromSlot(place); }}
                          onDragEnd={() => { setDraggingKey(null); setDraggingFromSlot(null); }}
                        >
                          <GripVertical size={12} className={styles.grip} />
                          <span>{item.label}</span>
                        </div>
                      ) : (
                        <span className={[styles.dropHint, isOver && styles.dropHintOver].filter(Boolean).join(' ')}>
                          {isOver ? '↓ Отпустите' : 'Перетащите сюда'}
                        </span>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className={styles.footer}>
          {error && (
            <div className={styles.errorMsg}>
              <AlertCircle size={15} /> {error}
            </div>
          )}

          {!hasPrizes && (
            <p className={styles.noPrizesMsg}>
              <Award size={14} /> Призовые места с наградами не определены — распределение недоступно
            </p>
          )}

          {hasPrizes && !allFilled && (
            <p className={styles.hintMsg}>
              Заполните все {totalSlots} мест, чтобы продолжить
            </p>
          )}

          <Button
            variant="primary"
            disabled={!allFilled || !hasPrizes || distributing}
            loading={distributing}
            onClick={distribute}
          >
            <Award size={16} /> Распределить награды
          </Button>
        </div>

      </div>
    </Layout>
  );
}
