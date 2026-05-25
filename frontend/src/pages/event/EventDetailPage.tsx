import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { CalendarDays, Users, Trophy, UserPlus, QrCode, ArrowLeft } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import QRModal from '../../components/QRModal';
import { eventApi } from '../../api/event';
import { useAuthStore } from '../../store/authStore';
import styles from './EventDetailPage.module.css';

const typeLabel: Record<string, string> = {
  TOURNAMENT: 'Турнир',
  QUEST: 'Квест',
  MASTERCLASS: 'Мастер-класс',
  DEMO: 'Демонстрация',
};

export default function EventDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const { accessToken } = useAuthStore();

  const [qrValue, setQrValue] = useState<string | null>(null);
  const [teamModal, setTeamModal] = useState(false);
  const [inviteModal, setInviteModal] = useState(false);
  const [teamName, setTeamName] = useState('');
  const [inviteNickname, setInviteNickname] = useState('');

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

  const registerMutation = useMutation({
    mutationFn: () => eventApi.register(id!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['my-registrations'] }),
  });

  const createTeamMutation = useMutation({
    mutationFn: () => eventApi.createTeam(myReg!.id, teamName),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['my-registrations'] }); setTeamModal(false); setTeamName(''); },
  });

  const inviteMutation = useMutation({
    mutationFn: async () => {
      const user = await eventApi.searchUserByNickname(inviteNickname);
      const commandId = (myReg as unknown as { commandId?: string })?.commandId;
      if (!commandId) throw new Error('Нет команды');
      return eventApi.inviteToTeam(commandId, user.nickname ?? inviteNickname);
    },
    onSuccess: () => { setInviteModal(false); setInviteNickname(''); },
  });

  if (isLoading) return <Layout><Spinner centered /></Layout>;
  if (!event) return <Layout><p>Мероприятие не найдено</p></Layout>;

  const isTeam = event.format === 'TEAM';
  const now = new Date();
  const isPast = new Date(event.endDate) < now;
  const hasCommandId = !!(myReg as unknown as { commandId?: string })?.commandId;

  return (
    <Layout>
      <button
        onClick={() => navigate(-1)}
        style={{ display: 'flex', alignItems: 'center', gap: 6, background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', fontSize: 14, marginBottom: 16, padding: 0 }}
      >
        <ArrowLeft size={16} /> Назад
      </button>

      {event.photoUrl && (
        <div className={styles.hero}>
          <img src={event.photoUrl} alt={event.name} className={styles.heroImg} />
          <div className={styles.heroOverlay} />
        </div>
      )}

      <div className={styles.content}>
        <div className={styles.header}>
          <div>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 8 }}>
              <Badge status="info">{typeLabel[event.type] ?? event.type}</Badge>
              <Badge status={isTeam ? 'active' : 'pending'}>{isTeam ? `Командный (до ${event.teamSize} чел.)` : 'Соло'}</Badge>
            </div>
            <h1 className={styles.title}>{event.name}</h1>
            <p style={{ color: 'var(--text-muted)', marginTop: 4 }}>{event.festivalName}</p>
          </div>

          <div style={{ display: 'flex', gap: 8 }}>
            {myReg && (
              <Button variant="secondary" size="sm" onClick={() => setQrValue(myReg.qrToken ?? '')}>
                <QrCode size={16} /> Мой QR
              </Button>
            )}
            {!myReg && !isPast && accessToken && (
              <Button loading={registerMutation.isPending} onClick={() => registerMutation.mutate()}>
                Зарегистрироваться
              </Button>
            )}
          </div>
        </div>

        <div className={styles.grid}>
          <Card padding="lg">
            <h3 className={styles.sectionTitle}>О мероприятии</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10, fontSize: 14 }}>
              {event.description && <p style={{ color: 'var(--text-muted)', lineHeight: 1.6 }}>{event.description}</p>}
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <CalendarDays size={16} style={{ color: 'var(--accent-primary)' }} />
                <span>{new Date(event.startDate).toLocaleString('ru-RU')} — {new Date(event.endDate).toLocaleString('ru-RU')}</span>
              </div>
              {event.maxParticipants && (
                <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                  <Users size={16} style={{ color: 'var(--accent-primary)' }} />
                  <span>Макс. участников: {event.maxParticipants}</span>
                </div>
              )}
              {event.companyName && <p><b>Организатор:</b> {event.companyName}</p>}
            </div>
          </Card>

          {isTeam && myReg && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}>Команда</h3>
              {myReg.teamName ? (
                <div>
                  <p style={{ fontWeight: 700, marginBottom: 12 }}>{myReg.teamName}</p>
                  {hasCommandId && (
                    <Button size="sm" variant="secondary" onClick={() => setInviteModal(true)}>
                      <UserPlus size={14} /> Пригласить участника
                    </Button>
                  )}
                </div>
              ) : (
                <div>
                  <p style={{ fontSize: 14, color: 'var(--text-muted)', marginBottom: 12 }}>Вы не в команде</p>
                  <Button size="sm" onClick={() => setTeamModal(true)}>Создать команду</Button>
                </div>
              )}
            </Card>
          )}

          {event.rewards.length > 0 && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}><Trophy size={16} style={{ verticalAlign: 'middle', marginRight: 6 }} />Награды</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {event.rewards.map(r => (
                  <div key={r.id} style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    {r.photoUrl && <img src={r.photoUrl} alt={r.name} style={{ width: 40, height: 40, borderRadius: 'var(--radius-sm)', objectFit: 'cover' }} />}
                    <div>
                      <p style={{ fontWeight: 600 }}>{r.place} место — {r.name}</p>
                      {r.description && <p style={{ fontSize: 13, color: 'var(--text-muted)' }}>{r.description}</p>}
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          )}
        </div>
      </div>

      {/* QR modal */}
      <QRModal open={!!qrValue} onClose={() => setQrValue(null)} value={qrValue ?? ''} title="QR-код регистрации" />

      {/* Create team modal */}
      <Modal open={teamModal} onClose={() => setTeamModal(false)} title="Создать команду" size="sm">
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div>
            <label style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: 6 }}>Название команды</label>
            <input value={teamName} onChange={e => setTeamName(e.target.value)} placeholder="Введите название" />
          </div>
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <Button variant="secondary" onClick={() => setTeamModal(false)}>Отмена</Button>
            <Button disabled={!teamName.trim()} loading={createTeamMutation.isPending} onClick={() => createTeamMutation.mutate()}>
              Создать
            </Button>
          </div>
        </div>
      </Modal>

      {/* Invite to team modal */}
      <Modal open={inviteModal} onClose={() => setInviteModal(false)} title="Пригласить в команду" size="sm">
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div>
            <label style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: 6 }}>Псевдоним участника</label>
            <input value={inviteNickname} onChange={e => setInviteNickname(e.target.value)} placeholder="nickname" />
          </div>
          {inviteMutation.isError && (
            <p style={{ fontSize: 13, color: 'var(--accent-danger)' }}>Ошибка при отправке приглашения</p>
          )}
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <Button variant="secondary" onClick={() => setInviteModal(false)}>Отмена</Button>
            <Button disabled={!inviteNickname.trim()} loading={inviteMutation.isPending} onClick={() => inviteMutation.mutate()}>
              Пригласить
            </Button>
          </div>
        </div>
      </Modal>
    </Layout>
  );
}
