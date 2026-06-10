import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  ArrowLeft, Calendar, Clock, Users, Trophy, Building2,
  QrCode, ChevronRight, Medal, UserPlus, LogOut, Trash2, Crown,
} from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import QRModal from '../../components/QRModal';
import { eventApi } from '../../api/event';
import { festivalApi } from '../../api/festival';
import { userApi } from '../../api/user';
import { useAuthStore } from '../../store/authStore';
import styles from './EventDetailPage.module.css';


function RewardPhoto({ url, name }: { url?: string; name: string }) {
  const [err, setErr] = useState(false);
  if (!url || err) return <Medal size={22} className={styles.rewardMedalIcon} />;
  return <img src={url} alt={name} className={styles.rewardPhotoThumb} onError={() => setErr(true)} />;
}

function UserNicknameTag({ userId, onClick, canInvite }: { userId: number; onClick?: () => void; canInvite?: boolean }) {
  const { data } = useQuery({
    queryKey: ['user-public', userId],
    queryFn: () => userApi.getUserById(userId),
    staleTime: 5 * 60 * 1000,
  });
  const nickname = data?.nickname ?? `#${userId}`;
  return (
    <div
      className={[styles.participantCard, canInvite ? styles.participantClickable : ''].join(' ')}
      onClick={canInvite ? onClick : undefined}
      title={canInvite ? 'Пригласить в команду' : undefined}
    >
      <div className={styles.participantAvatar}>{nickname[0]?.toUpperCase() ?? '?'}</div>
      <span className={styles.participantNick}>{nickname}</span>
      {canInvite && <UserPlus size={13} className={styles.participantInviteIcon} />}
    </div>
  );
}

const TYPE_LABEL: Record<string, string> = {
  TOURNAMENT: 'Турнир', QUEST: 'Квест',
  MASTER_CLASS: 'Мастер-класс', MASTERCLASS: 'Мастер-класс',
  DEMONSTRATION: 'Демонстрация', DEMO: 'Демонстрация',
};

const PLACE_LABELS: Record<number, string> = { 1: '🥇 1 место', 2: '🥈 2 место', 3: '🥉 3 место' };

function fmtDate(iso: string) {
  return new Date(iso + 'T00:00:00').toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric' });
}
function fmtTime(t?: string) { return t ? t.substring(0, 5) : ''; }

export default function EventDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const { accessToken, role } = useAuthStore();

  const [qrToken, setQrToken] = useState<string | null>(null);
  const [qrOpen, setQrOpen] = useState(false);
  const [teamModal, setTeamModal] = useState(false);
  const [teamName, setTeamName] = useState('');
  const [imgErr, setImgErr] = useState(false);


  const { data: event, isLoading } = useQuery({
    queryKey: ['event', id],
    queryFn: () => eventApi.getEventById(id!),
    enabled: !!id,
  });

  const { data: myRegs } = useQuery({
    queryKey: ['my-registrations'],
    queryFn: eventApi.myRegistrations,
    enabled: !!accessToken,
  });

  const { data: myTickets } = useQuery({
    queryKey: ['my-tickets'],
    queryFn: festivalApi.myTickets,
    enabled: !!accessToken && (role === 'USER' || role === 'CURATOR'),
  });

  const { data: me } = useQuery({
    queryKey: ['me'],
    queryFn: userApi.getMe,
    enabled: !!accessToken,
  });

  const isTeam = event?.format === 'TEAM';
  const myReg = (myRegs ?? []).find(r => r.eventId === id);
  const myUserId = me?.id;

  // participants (only when registered and event is loaded)
  const { data: soloParticipants } = useQuery({
    queryKey: ['event-participants', id],
    queryFn: () => eventApi.getEventParticipants(id!),
    enabled: !!myReg && !isTeam && !!id,
  });

  const { data: commands, refetch: refetchCommands } = useQuery({
    queryKey: ['event-commands', id],
    queryFn: () => eventApi.getEventCommands(id!),
    enabled: !!myReg && isTeam && !!id,
  });

  const { data: unaffiliated } = useQuery({
    queryKey: ['event-unaffiliated', id],
    queryFn: () => eventApi.getEventParticipantsWithoutCommand(id!),
    enabled: !!myReg && isTeam && !!id,
  });

  const myCommand = (commands ?? []).find(c => myUserId && c.memberUserIds.includes(myUserId));
  const amCreator = myCommand?.creatorId === myUserId;

  const hasValidTicket = (myTickets ?? []).some(
    t => t.status === 'ACTIVE' && event && t.festivalId === event.festivalId,
  );

  const registerMutation = useMutation({
    mutationFn: () => eventApi.register(id!),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['my-registrations'] });
      qc.invalidateQueries({ queryKey: ['event-participants', id] });
    },
  });

  const { mutate: generateQr, isPending: qrLoading } = useMutation({
    mutationFn: () => eventApi.generateEventQr(id!),
    onSuccess: (data: { token: string }) => {
      setQrToken(data.token);
      setQrOpen(true);
    },
  });

  const handleQrClick = () => {
    if (qrToken) setQrOpen(true);
    else generateQr();
  };

  const createTeamMutation = useMutation({
    mutationFn: () => eventApi.createTeam(id!, teamName),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['event-commands', id] });
      qc.invalidateQueries({ queryKey: ['event-unaffiliated', id] });
      setTeamModal(false);
      setTeamName('');
    },
  });

  const inviteMutation = useMutation({
    mutationFn: (targetUserId: number) =>
      eventApi.inviteToTeam(String(myCommand!.commandId), targetUserId),
    onSuccess: () => {},
  });

  const leaveTeamMutation = useMutation({
    mutationFn: () => eventApi.leaveTeam(String(myCommand!.commandId)),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['event-commands', id] });
      qc.invalidateQueries({ queryKey: ['event-unaffiliated', id] });
    },
  });

  const disbandTeamMutation = useMutation({
    mutationFn: () => eventApi.disbandTeam(String(myCommand!.commandId)),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['event-commands', id] });
      qc.invalidateQueries({ queryKey: ['event-unaffiliated', id] });
    },
  });

  if (isLoading) return <Layout><Spinner centered /></Layout>;
  if (!event) return <Layout><p style={{ color: 'var(--text-muted)' }}>Мероприятие не найдено</p></Layout>;

  const isPast = new Date(event.endDate) < new Date();
  const canRegister = !!accessToken && !myReg && !isPast && (role === 'USER' || role === 'CURATOR') && hasValidTicket;
  const registerDisabledReason = !accessToken
    ? 'Войдите в аккаунт'
    : isPast ? 'Мероприятие уже завершено'
    : myReg ? null
    : role !== 'USER' && role !== 'CURATOR' ? 'Регистрация доступна только для пользователей'
    : !hasValidTicket ? 'Необходим действующий билет на фестиваль'
    : null;

  const teamFull = !!(event.teamSize && myCommand && myCommand.memberUserIds.length >= event.teamSize);
  const canInvite = !!myCommand && !!myUserId && !teamFull;

  return (
    <Layout>
      <button className={styles.backBtn} onClick={() => navigate(-1)}>
        <ArrowLeft size={16} /> Назад
      </button>

      {!imgErr && (
        <div className={styles.heroWrap}>
          <img src={`/api/files/FESTIVAL_EVENT/${id}`} alt={event.name} className={styles.heroImg} onError={() => setImgErr(true)} />
          <div className={styles.heroOverlay} />
        </div>
      )}

      <div className={styles.header}>
        <div className={styles.headerLeft}>
          <div className={styles.badgeRow}>
            <Badge status="info">{TYPE_LABEL[event.type] ?? event.type}</Badge>
            <Badge status={isTeam ? 'pending' : 'active'}>
              {isTeam ? `Командное${event.teamSize ? ` · до ${event.teamSize} чел.` : ''}` : 'Одиночное'}
            </Badge>
          </div>
          <h1 className={styles.title}>{event.name}</h1>
          {event.festivalName && (
            <p className={styles.festivalRef}>
              <ChevronRight size={13} style={{ opacity: 0.5 }} />{event.festivalName}
            </p>
          )}
        </div>
        <div className={styles.headerActions}>
          {myReg && (
            <Button variant="secondary" onClick={handleQrClick} loading={qrLoading}>
              <QrCode size={15} /> Мой QR
            </Button>
          )}
          {!myReg && !isPast && (
            <div className={styles.registerWrap}>
              <Button loading={registerMutation.isPending} disabled={!canRegister}
                onClick={() => canRegister && registerMutation.mutate()}
                title={registerDisabledReason ?? undefined}>
                Зарегистрироваться
              </Button>
              {registerDisabledReason && <p className={styles.registerHint}>{registerDisabledReason}</p>}
            </div>
          )}
        </div>
      </div>

      <div className={styles.grid}>
        {/* Info */}
        <Card padding="lg" className={styles.infoCard}>
          <h3 className={styles.cardTitle}>О мероприятии</h3>
          {event.description && <p className={styles.description}>{event.description}</p>}
          <div className={styles.metaList}>
            {event.startDate && (
              <div className={styles.metaRow}>
                <Calendar size={15} className={styles.metaIcon} />
                <div><span className={styles.metaLabel}>Дата</span><span className={styles.metaValue}>{fmtDate(event.startDate)}</span></div>
              </div>
            )}
            {event.startTime && (
              <div className={styles.metaRow}>
                <Clock size={15} className={styles.metaIcon} />
                <div>
                  <span className={styles.metaLabel}>Начало</span>
                  <span className={styles.metaValue}>{fmtTime(event.startTime)}{event.approximateDuration ? ` · ~${event.approximateDuration} мин` : ''}</span>
                </div>
              </div>
            )}
            {event.maxParticipants && (
              <div className={styles.metaRow}>
                <Users size={15} className={styles.metaIcon} />
                <div><span className={styles.metaLabel}>Участников</span><span className={styles.metaValue}>до {event.maxParticipants}</span></div>
              </div>
            )}
            {event.companyName && (
              <div className={styles.metaRow}>
                <Building2 size={15} className={styles.metaIcon} />
                <div><span className={styles.metaLabel}>Организатор</span><span className={styles.metaValue}>{event.companyName}</span></div>
              </div>
            )}
          </div>
        </Card>

        {/* Rewards */}
        {event.rewards.length > 0 && (
          <Card padding="lg" className={styles.rewardsCard}>
            <h3 className={styles.cardTitle}><Trophy size={15} /> Призы</h3>
            <div className={styles.rewardsList}>
              {[...event.rewards].sort((a, b) => a.place - b.place).map(r => (
                <div key={r.id} className={styles.rewardRow}>
                  <div className={styles.rewardPlace}>
                    <RewardPhoto url={r.photoUrl} name={r.name} />
                    <span className={styles.rewardPlaceLabel}>{PLACE_LABELS[r.place] ?? `${r.place} место`}</span>
                  </div>
                  <div className={styles.rewardInfo}>
                    <p className={styles.rewardName}>{r.name}</p>
                    {r.description && <p className={styles.rewardDesc}>{r.description}</p>}
                  </div>
                </div>
              ))}
            </div>
          </Card>
        )}

        {/* Solo participants */}
        {myReg && !isTeam && (
          <Card padding="lg" className={styles.participantsCard}>
            <h3 className={styles.cardTitle}><Users size={15} /> Участники</h3>
            {!soloParticipants ? <Spinner /> : soloParticipants.length === 0 ? (
              <p className={styles.emptyHint}>Пока никто не зарегистрирован</p>
            ) : (
              <div className={styles.participantGrid}>
                {soloParticipants.map(uid => (
                  <UserNicknameTag key={uid} userId={uid} />
                ))}
              </div>
            )}
          </Card>
        )}

        {/* Team participants */}
        {myReg && isTeam && (
          <Card padding="lg" className={styles.participantsCard}>
            <h3 className={styles.cardTitle}><Users size={15} /> Участники и команды</h3>

            {/* My team block */}
            {myCommand ? (
              <div className={styles.myTeamBlock}>
                <div className={styles.teamHeader}>
                  <Crown size={14} className={styles.crownIcon} />
                  <span className={styles.teamNameLabel}>{myCommand.commandName}</span>
                  <span className={styles.teamMeta}>{myCommand.memberUserIds.length} чел.</span>
                  {amCreator ? (
                    <Button size="sm" variant="danger" loading={disbandTeamMutation.isPending}
                      onClick={() => disbandTeamMutation.mutate()}>
                      <Trash2 size={12} /> Расформировать
                    </Button>
                  ) : (
                    <Button size="sm" variant="secondary" loading={leaveTeamMutation.isPending}
                      onClick={() => leaveTeamMutation.mutate()}>
                      <LogOut size={12} /> Выйти
                    </Button>
                  )}
                </div>
                <div className={styles.participantGrid}>
                  {myCommand.memberUserIds.map(uid => (
                    <UserNicknameTag key={uid} userId={uid} />
                  ))}
                </div>
              </div>
            ) : (
              <div className={styles.noTeamBanner}>
                <p className={styles.emptyHint}>Вы не в команде</p>
                <Button size="sm" onClick={() => setTeamModal(true)}>Создать команду</Button>
              </div>
            )}

            {/* Other teams */}
            {(commands ?? []).filter(c => c.commandId !== myCommand?.commandId).map(cmd => (
              <div key={cmd.commandId} className={styles.otherTeamBlock}>
                <p className={styles.otherTeamName}>{cmd.commandName} <span className={styles.teamMeta}>· {cmd.memberUserIds.length} чел.</span></p>
                <div className={styles.participantGrid}>
                  {cmd.memberUserIds.map(uid => (
                    <UserNicknameTag key={uid} userId={uid} />
                  ))}
                </div>
              </div>
            ))}

            {/* Participants without team */}
            {(unaffiliated ?? []).length > 0 && (
              <div className={styles.unaffiliatedBlock}>
                <p className={styles.sectionLabel}>Без команды</p>
                <div className={styles.participantGrid}>
                  {(unaffiliated ?? []).filter(uid => uid !== myUserId).map(uid => (
                    <UserNicknameTag
                      key={uid}
                      userId={uid}
                      canInvite={canInvite}
                      onClick={() => inviteMutation.mutate(uid)}
                    />
                  ))}
                  {myUserId && (unaffiliated ?? []).includes(myUserId) && (
                    <UserNicknameTag key={myUserId} userId={myUserId} />
                  )}
                </div>
              </div>
            )}
          </Card>
        )}
      </div>

      {/* QR Modal */}
      <QRModal open={qrOpen} onClose={() => setQrOpen(false)} value={qrToken ?? ''} title="QR-код регистрации" />

      {/* Create team modal */}
      <Modal open={teamModal} onClose={() => setTeamModal(false)} title="Создать команду">
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div>
            <label style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: 6 }}>
              Название команды
            </label>
            <input value={teamName} onChange={e => setTeamName(e.target.value)} placeholder="Введите название" />
          </div>
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <Button variant="secondary" onClick={() => setTeamModal(false)}>Отмена</Button>
            <Button disabled={!teamName.trim()} loading={createTeamMutation.isPending}
              onClick={() => createTeamMutation.mutate()}>
              Создать
            </Button>
          </div>
        </div>
      </Modal>
    </Layout>
  );
}
