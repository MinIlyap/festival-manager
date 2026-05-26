import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  ArrowLeft, Calendar, Clock, Users, Trophy, Building2,
  QrCode, UserPlus, ChevronRight, Medal,
} from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import QRModal from '../../components/QRModal';
import { eventApi } from '../../api/event';
import { useAuthStore } from '../../store/authStore';
import styles from './EventDetailPage.module.css';

const TYPE_LABEL: Record<string, string> = {
  TOURNAMENT:    'Турнир',
  QUEST:         'Квест',
  MASTER_CLASS:  'Мастер-класс',
  MASTERCLASS:   'Мастер-класс',
  DEMONSTRATION: 'Демонстрация',
  DEMO:          'Демонстрация',
};

const PLACE_LABELS: Record<number, string> = {
  1: '🥇 1 место',
  2: '🥈 2 место',
  3: '🥉 3 место',
};

function fmtDate(iso: string) {
  return new Date(iso + 'T00:00:00').toLocaleDateString('ru-RU', {
    day: 'numeric', month: 'long', year: 'numeric',
  });
}

function fmtTime(t?: string) {
  return t ? t.substring(0, 5) : '';
}

export default function EventDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const { accessToken, role } = useAuthStore();

  const [qrValue, setQrValue] = useState<string | null>(null);
  const [teamModal, setTeamModal] = useState(false);
  const [inviteModal, setInviteModal] = useState(false);
  const [teamName, setTeamName] = useState('');
  const [inviteNickname, setInviteNickname] = useState('');
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

  const myReg = (myRegs ?? []).find(r => r.eventId === id);
  const hasCommandId = !!(myReg as unknown as { commandId?: string })?.commandId;

  const registerMutation = useMutation({
    mutationFn: () => eventApi.register(id!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['my-registrations'] }),
  });

  const createTeamMutation = useMutation({
    mutationFn: () => eventApi.createTeam(myReg!.id, teamName),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['my-registrations'] });
      setTeamModal(false);
      setTeamName('');
    },
  });

  const inviteMutation = useMutation({
    mutationFn: async () => {
      const user = await eventApi.searchUserByNickname(inviteNickname);
      const commandId = (myReg as unknown as { commandId?: string })?.commandId;
      if (!commandId) throw new Error('Нет команды');
      return eventApi.inviteToTeam(commandId, user.nickname ?? inviteNickname);
    },
    onSuccess: () => {
      setInviteModal(false);
      setInviteNickname('');
    },
  });

  if (isLoading) return <Layout><Spinner centered /></Layout>;
  if (!event) return <Layout><p style={{ color: 'var(--text-muted)' }}>Мероприятие не найдено</p></Layout>;

  const isTeam = event.format === 'TEAM';
  const isPast = new Date(event.endDate) < new Date();
  const canRegister = !!accessToken && !myReg && !isPast && (role === 'USER' || role === 'CURATOR');

  return (
    <Layout>
      {/* Back */}
      <button className={styles.backBtn} onClick={() => navigate(-1)}>
        <ArrowLeft size={16} /> Назад
      </button>

      {/* Hero photo */}
      {!imgErr && (
        <div className={styles.heroWrap}>
          <img
            src={`/api/files/FESTIVAL_EVENT/${id}`}
            alt={event.name}
            className={styles.heroImg}
            onError={() => setImgErr(true)}
          />
          <div className={styles.heroOverlay} />
        </div>
      )}

      {/* Header */}
      <div className={styles.header}>
        <div className={styles.headerLeft}>
          <div className={styles.badgeRow}>
            <Badge status="info">{TYPE_LABEL[event.type] ?? event.type}</Badge>
            <Badge status={isTeam ? 'pending' : 'active'}>
              {isTeam
                ? `Командное${event.teamSize ? ` · до ${event.teamSize} чел.` : ''}`
                : 'Личное'}
            </Badge>
          </div>
          <h1 className={styles.title}>{event.name}</h1>
          {event.festivalName && (
            <p className={styles.festivalRef}>
              <ChevronRight size={13} style={{ opacity: 0.5 }} />
              {event.festivalName}
            </p>
          )}
        </div>
        <div className={styles.headerActions}>
          {myReg && (
            <Button variant="secondary" onClick={() => setQrValue(myReg.qrToken ?? '')}>
              <QrCode size={15} /> Мой QR
            </Button>
          )}
          {canRegister && (
            <Button loading={registerMutation.isPending} onClick={() => registerMutation.mutate()}>
              Зарегистрироваться
            </Button>
          )}
        </div>
      </div>

      {/* Main grid */}
      <div className={styles.grid}>
        {/* ── Info card ── */}
        <Card padding="lg" className={styles.infoCard}>
          <h3 className={styles.cardTitle}>О мероприятии</h3>

          {event.description && (
            <p className={styles.description}>{event.description}</p>
          )}

          <div className={styles.metaList}>
            {event.startDate && (
              <div className={styles.metaRow}>
                <Calendar size={15} className={styles.metaIcon} />
                <div>
                  <span className={styles.metaLabel}>Дата</span>
                  <span className={styles.metaValue}>{fmtDate(event.startDate)}</span>
                </div>
              </div>
            )}
            {event.startTime && (
              <div className={styles.metaRow}>
                <Clock size={15} className={styles.metaIcon} />
                <div>
                  <span className={styles.metaLabel}>Начало</span>
                  <span className={styles.metaValue}>
                    {fmtTime(event.startTime)}
                    {event.approximateDuration
                      ? ` · ~${event.approximateDuration} мин`
                      : ''}
                  </span>
                </div>
              </div>
            )}
            {event.maxParticipants && (
              <div className={styles.metaRow}>
                <Users size={15} className={styles.metaIcon} />
                <div>
                  <span className={styles.metaLabel}>Участников</span>
                  <span className={styles.metaValue}>до {event.maxParticipants}</span>
                </div>
              </div>
            )}
            {event.companyName && (
              <div className={styles.metaRow}>
                <Building2 size={15} className={styles.metaIcon} />
                <div>
                  <span className={styles.metaLabel}>Организатор</span>
                  <span className={styles.metaValue}>{event.companyName}</span>
                </div>
              </div>
            )}
          </div>
        </Card>

        {/* ── Team card (only for team format when registered) ── */}
        {isTeam && myReg && (
          <Card padding="lg">
            <h3 className={styles.cardTitle}>Команда</h3>
            {myReg.teamName ? (
              <div className={styles.teamInfo}>
                <p className={styles.teamName}>{myReg.teamName}</p>
                {hasCommandId && (
                  <Button variant="secondary" onClick={() => setInviteModal(true)}>
                    <UserPlus size={14} /> Пригласить участника
                  </Button>
                )}
              </div>
            ) : (
              <div className={styles.teamEmpty}>
                <p className={styles.teamEmptyText}>Вы не в команде</p>
                <Button onClick={() => setTeamModal(true)}>Создать команду</Button>
              </div>
            )}
          </Card>
        )}

        {/* ── Rewards card ── */}
        {event.rewards.length > 0 && (
          <Card padding="lg" className={styles.rewardsCard}>
            <h3 className={styles.cardTitle}><Trophy size={15} /> Призы</h3>
            <div className={styles.rewardsList}>
              {[...event.rewards]
                .sort((a, b) => a.place - b.place)
                .map(r => (
                  <div key={r.id} className={styles.rewardRow}>
                    <div className={styles.rewardPlace}>
                      <Medal size={16} className={styles.rewardMedalIcon} />
                      <span className={styles.rewardPlaceLabel}>
                        {PLACE_LABELS[r.place] ?? `${r.place} место`}
                      </span>
                    </div>
                    <div className={styles.rewardInfo}>
                      <p className={styles.rewardName}>{r.name}</p>
                      {r.description && (
                        <p className={styles.rewardDesc}>{r.description}</p>
                      )}
                    </div>
                    {r.photoUrl && (
                      <img src={r.photoUrl} alt={r.name} className={styles.rewardPhoto} />
                    )}
                  </div>
                ))}
            </div>
          </Card>
        )}
      </div>

      {/* Modals */}
      <QRModal
        open={!!qrValue}
        onClose={() => setQrValue(null)}
        value={qrValue ?? ''}
        title="QR-код регистрации"
      />

      <Modal open={teamModal} onClose={() => setTeamModal(false)} title="Создать команду">
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div>
            <label style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: 6 }}>
              Название команды
            </label>
            <input
              value={teamName}
              onChange={e => setTeamName(e.target.value)}
              placeholder="Введите название"
            />
          </div>
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <Button variant="secondary" onClick={() => setTeamModal(false)}>Отмена</Button>
            <Button
              disabled={!teamName.trim()}
              loading={createTeamMutation.isPending}
              onClick={() => createTeamMutation.mutate()}
            >
              Создать
            </Button>
          </div>
        </div>
      </Modal>

      <Modal open={inviteModal} onClose={() => setInviteModal(false)} title="Пригласить в команду">
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div>
            <label style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: 6 }}>
              Псевдоним участника
            </label>
            <input
              value={inviteNickname}
              onChange={e => setInviteNickname(e.target.value)}
              placeholder="nickname"
            />
          </div>
          {inviteMutation.isError && (
            <p style={{ fontSize: 13, color: 'var(--accent-danger)' }}>Ошибка при отправке приглашения</p>
          )}
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <Button variant="secondary" onClick={() => setInviteModal(false)}>Отмена</Button>
            <Button
              disabled={!inviteNickname.trim()}
              loading={inviteMutation.isPending}
              onClick={() => inviteMutation.mutate()}
            >
              Пригласить
            </Button>
          </div>
        </div>
      </Modal>
    </Layout>
  );
}
