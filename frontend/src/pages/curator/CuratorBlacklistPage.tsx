import { useState, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ShieldBan, QrCode, ChevronLeft, AlertTriangle, Search, CheckCircle, XCircle, UserX, MapPin, Calendar, Trophy } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import EmptyState from '../../components/ui/EmptyState';
import QRScanner from '../../components/QRScanner';
import { festivalApi } from '../../api/festival';
import { userApi } from '../../api/user';
import type { Festival, FestivalParticipant } from '../../types';
import styles from './CuratorBlacklistPage.module.css';

const BLACKLIST_REASONS = [
  'Агрессивное поведение',
  'Порча имущества фестиваля',
  'Нахождение в состоянии опьянения',
  'Кража или мошенничество',
  'Несанкционированный доступ в закрытые зоны',
  'Другое',
];

type ScanFeedback = 'found' | 'not-found' | 'error' | null;

function fmtDate(iso: string) {
  return new Date(iso).toLocaleDateString('ru-RU', { day: 'numeric', month: 'short', year: 'numeric' });
}

function FestivalCard({ festival, onClick }: { festival: Festival; onClick: () => void }) {
  const [imgErr, setImgErr] = useState(false);
  const location = [festival.cityName, festival.platformName].filter(Boolean).join(' · ');

  return (
    <div className={styles.festivalCard} onClick={onClick} role="button" tabIndex={0}
         onKeyDown={e => e.key === 'Enter' && onClick()}>
      <div className={styles.festivalCover}>
        {!imgErr ? (
          <img src={`/api/files/FESTIVAL_LOCATION/${festival.id}`} alt={festival.name} onError={() => setImgErr(true)} />
        ) : (
          <div className={styles.festivalCoverPlaceholder}><Trophy size={32} /></div>
        )}
      </div>
      <div className={styles.festivalCardBody}>
        <p className={styles.festivalCardName}>{festival.name}</p>
        <div className={styles.festivalCardMeta}>
          {location && (
            <span className={styles.festivalCardMetaRow}><MapPin size={11} /> {location}</span>
          )}
          <span className={styles.festivalCardMetaRow}>
            <Calendar size={11} /> {fmtDate(festival.startDate)} — {fmtDate(festival.endDate)}
          </span>
        </div>
      </div>
    </div>
  );
}

interface BlacklistFormState {
  reasonIndex: number;
  customReason: string;
  days: number;
}

export default function CuratorBlacklistPage() {
  const qc = useQueryClient();

  const [selectedFestival, setSelectedFestival] = useState<Festival | null>(null);
  const [showScanner, setShowScanner] = useState(false);
  const [scanFeedback, setScanFeedback] = useState<ScanFeedback>(null);
  const [targetUser, setTargetUser] = useState<FestivalParticipant | null>(null);
  const [form, setForm] = useState<BlacklistFormState>({ reasonIndex: 0, customReason: '', days: 1 });
  const [search, setSearch] = useState('');

  const { data: festivals, isLoading: festivalsLoading } = useQuery({
    queryKey: ['curatorFestivals'],
    queryFn: festivalApi.myCuratorFestivals,
  });

  const { data: participants, isLoading: participantsLoading } = useQuery({
    queryKey: ['participants', selectedFestival?.id],
    queryFn: () => festivalApi.getParticipants(selectedFestival!.id),
    enabled: !!selectedFestival,
  });

  const addMutation = useMutation({
    mutationFn: ({ userId, reason, days }: { userId: number; reason: string; days: number }) =>
      userApi.addToBlacklist(String(userId), reason, days),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['participants', selectedFestival?.id] });
      setTargetUser(null);
    },
  });

  const handleScan = useCallback((token: string) => {
    if (!selectedFestival) return;
    setShowScanner(false);
    setScanFeedback(null);
    festivalApi.verifyTicketQr(token, selectedFestival.id)
      .then(res => {
        if (res.status === 'SUCCESS' && res.user_id) {
          const found = (participants ?? []).find(p => p.userId === res.user_id);
          if (found) {
            setTargetUser(found);
            setForm({ reasonIndex: 0, customReason: '', days: 1 });
            setScanFeedback('found');
          } else {
            setScanFeedback('not-found');
          }
        } else {
          setScanFeedback('error');
        }
      })
      .catch(() => setScanFeedback('error'));
  }, [selectedFestival, participants]);

  const handleSubmit = () => {
    if (!targetUser) return;
    const reason = form.reasonIndex === BLACKLIST_REASONS.length - 1
      ? form.customReason.trim()
      : BLACKLIST_REASONS[form.reasonIndex];
    if (!reason || form.days < 1) return;
    addMutation.mutate({ userId: targetUser.userId, reason, days: form.days });
  };

  const filtered = (participants ?? []).filter(p =>
    !search || p.nickname.toLowerCase().includes(search.toLowerCase()),
  );
  const bannedCount = (participants ?? []).filter(p => p.inBlacklist).length;

  if (festivalsLoading) return <Layout><Spinner centered /></Layout>;

  return (
    <Layout>
      <div className={styles.page}>
        {!selectedFestival ? (
          <>
            <h1 className={styles.title}>Чёрный список</h1>
            <p className={styles.subtitle}>Выберите фестиваль для управления участниками</p>
            {!festivals?.filter(f => f.status === 'APPROVED').length ? (
              <EmptyState icon={ShieldBan} message="Нет фестивалей" description="Вы не являетесь куратором ни одного опубликованного фестиваля" />
            ) : (
              <div className={styles.festivalGrid}>
                {(festivals ?? []).filter(f => f.status === 'APPROVED').map(f => (
                  <FestivalCard key={f.id} festival={f} onClick={() => { setSelectedFestival(f); setScanFeedback(null); setSearch(''); }} />
                ))}
              </div>
            )}
          </>
        ) : (
          <>
            <div className={styles.header}>
              <button className={styles.back} onClick={() => { setSelectedFestival(null); setShowScanner(false); setScanFeedback(null); }}>
                <ChevronLeft size={18} /> Назад
              </button>
              <div className={styles.headerMain}>
                <div>
                  <h1 className={styles.title}>{selectedFestival.name}</h1>
                  {bannedCount > 0 && (
                    <p className={styles.bannedSummary}><UserX size={14} /> {bannedCount} в чёрном списке</p>
                  )}
                </div>
                <Button variant={showScanner ? 'danger' : 'secondary'} size="sm" onClick={() => { setShowScanner(s => !s); setScanFeedback(null); }}>
                  <QrCode size={16} /> {showScanner ? 'Скрыть сканер' : 'Сканировать QR'}
                </Button>
              </div>
            </div>

            {showScanner && (
              <Card padding="md" className={styles.scannerCard}>
                <p className={styles.scannerHint}>Наведите камеру на QR-код билета участника</p>
                <QRScanner onScan={handleScan} />
              </Card>
            )}

            {scanFeedback === 'not-found' && (
              <div className={[styles.scanAlert, styles.scanAlertWarn].join(' ')}>
                <AlertTriangle size={16} /> Участник не найден в списке этого фестиваля
              </div>
            )}
            {scanFeedback === 'error' && (
              <div className={[styles.scanAlert, styles.scanAlertError].join(' ')}>
                <XCircle size={16} /> Недействительный или нераспознанный QR-код
              </div>
            )}
            {scanFeedback === 'found' && (
              <div className={[styles.scanAlert, styles.scanAlertOk].join(' ')}>
                <CheckCircle size={16} /> Участник найден — заполните форму ниже
              </div>
            )}

            {participantsLoading ? (
              <Spinner centered />
            ) : !participants?.length ? (
              <EmptyState icon={ShieldBan} message="Нет участников" description="На этот фестиваль пока не куплено ни одного билета" />
            ) : (
              <>
                <div className={styles.searchRow}>
                  <div className={styles.searchWrap}>
                    <Search size={15} className={styles.searchIcon} />
                    <input
                      className={styles.searchInput}
                      placeholder="Поиск по никнейму..."
                      value={search}
                      onChange={e => setSearch(e.target.value)}
                    />
                  </div>
                  <span className={styles.countLabel}>{participants.length} участников</span>
                </div>

                <div className={styles.participantList}>
                  {filtered.map(p => (
                    <div key={p.userId} className={[styles.participantRow, p.inBlacklist ? styles.banned : ''].join(' ')}>
                      <div className={styles.participantLeft}>
                        <div className={[styles.avatar, p.inBlacklist ? styles.avatarBanned : ''].join(' ')}>
                          {p.nickname[0]?.toUpperCase() ?? '?'}
                        </div>
                        <div className={styles.participantInfo}>
                          <span className={styles.participantNickname}>{p.nickname}</span>
                          {p.inBlacklist && (
                            <div className={styles.banDetails}>
                              <span className={styles.bannedBadge}>ЧС</span>
                              {p.blacklistReason && (
                                <span className={styles.bannedReason}>{p.blacklistReason}</span>
                              )}
                              {p.blacklistUntil && (
                                <span className={styles.bannedUntil}>
                                  до {new Date(p.blacklistUntil).toLocaleDateString('ru-RU')}
                                </span>
                              )}
                            </div>
                          )}
                        </div>
                      </div>
                      {!p.inBlacklist && (
                        <Button size="sm" variant="danger" onClick={() => { setTargetUser(p); setForm({ reasonIndex: 0, customReason: '', days: 1 }); setScanFeedback(null); }}>
                          <ShieldBan size={13} /> В ЧС
                        </Button>
                      )}
                    </div>
                  ))}
                  {filtered.length === 0 && (
                    <p className={styles.noResults}>Ничего не найдено</p>
                  )}
                </div>
              </>
            )}
          </>
        )}
      </div>

      <Modal
        open={!!targetUser}
        onClose={() => setTargetUser(null)}
        title={`Чёрный список: ${targetUser?.nickname}`}
        size="md"
      >
        <div className={styles.modalBody}>
          <div className={styles.warningBox}>
            <AlertTriangle size={16} />
            <span>Пользователь не сможет посещать фестивали в указанный период</span>
          </div>

          <p className={styles.fieldLabel}>Причина</p>
          <div className={styles.reasonList}>
            {BLACKLIST_REASONS.map((r, i) => (
              <label key={i} className={[styles.reasonOption, form.reasonIndex === i ? styles.reasonSelected : ''].join(' ')}>
                <input
                  type="radio"
                  name="reason"
                  checked={form.reasonIndex === i}
                  onChange={() => setForm(f => ({ ...f, reasonIndex: i }))}
                />
                <span>{r}</span>
              </label>
            ))}
          </div>

          {form.reasonIndex === BLACKLIST_REASONS.length - 1 && (
            <textarea
              className={styles.customReason}
              placeholder="Опишите причину..."
              value={form.customReason}
              onChange={e => setForm(f => ({ ...f, customReason: e.target.value }))}
              rows={3}
            />
          )}

          <p className={styles.fieldLabel}>Количество дней</p>
          <input
            type="number"
            className={styles.daysInput}
            min={1}
            max={3650}
            value={form.days}
            onChange={e => setForm(f => ({ ...f, days: Math.max(1, Number(e.target.value)) }))}
          />

          <div className={styles.modalActions}>
            <Button variant="ghost" onClick={() => setTargetUser(null)}>Отмена</Button>
            <Button
              variant="danger"
              loading={addMutation.isPending}
              onClick={handleSubmit}
              disabled={form.reasonIndex === BLACKLIST_REASONS.length - 1 && !form.customReason.trim()}
            >
              <ShieldBan size={16} /> Занести в ЧС
            </Button>
          </div>
        </div>
      </Modal>
    </Layout>
  );
}
