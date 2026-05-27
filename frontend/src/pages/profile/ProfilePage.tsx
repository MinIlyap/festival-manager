import { useState, useRef, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm, Controller } from 'react-hook-form';
import { Camera, Trash2, AlertCircle } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Avatar from '../../components/ui/Avatar';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import Card from '../../components/ui/Card';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import CityCombobox from '../../components/CityCombobox';
import { userApi } from '../../api/user';
import { authApi } from '../../api/auth';
import { useAuthStore } from '../../store/authStore';
import type { City } from '../../types';
import styles from './ProfilePage.module.css';

function formatPhoneDisplay(raw: string): string {
  const d = raw.replace(/\D/g, '');
  if (!d) return '';
  const n = d.startsWith('8') ? '7' + d.slice(1) : d;
  if (n.length <= 1) return `+${n}`;
  if (n.length <= 4) return `+${n[0]} (${n.slice(1)}`;
  if (n.length <= 7) return `+${n[0]} (${n.slice(1, 4)}) ${n.slice(4)}`;
  if (n.length <= 9) return `+${n[0]} (${n.slice(1, 4)}) ${n.slice(4, 7)}-${n.slice(7)}`;
  return `+${n[0]} (${n.slice(1, 4)}) ${n.slice(4, 7)}-${n.slice(7, 9)}-${n.slice(9, 11)}`;
}

interface ProfileFormValues {
  firstName: string;
  lastName: string;
  middleName: string;
  phoneNumber: string;
  passportSeries: string;
  passportNumber: string;
  email: string;
}

export default function ProfilePage() {
  const qc = useQueryClient();
  const { role } = useAuthStore();
  const [city, setCity] = useState<City | null>(null);
  const [pwModal, setPwModal] = useState(false);
  const [saved, setSaved] = useState(false);
  const [pwErr, setPwErr] = useState('');
  const fileRef = useRef<HTMLInputElement>(null);

  const { data: user, isLoading } = useQuery({
    queryKey: ['me'],
    queryFn: userApi.getMe,
  });

  useEffect(() => {
    if (user?.cityId && !city) {
      setCity({ id: user.cityId, name: user.cityName ?? '' });
    }
  }, [user]);

  const { data: authInfo } = useQuery({
    queryKey: ['auth-me'],
    queryFn: authApi.getMe,
  });

  const { data: bl } = useQuery({
    queryKey: ['blacklist-check'],
    queryFn: userApi.checkBlacklist,
  });

  const { register, control, handleSubmit, formState: { isSubmitting } } = useForm<ProfileFormValues>({
    values: {
      firstName: user?.firstName ?? '',
      lastName: user?.lastName ?? '',
      middleName: user?.middleName ?? '',
      phoneNumber: user?.phoneNumber ?? '',
      passportSeries: user?.passportSeries ?? '',
      passportNumber: user?.passportNumber ?? '',
      email: authInfo?.email ?? '',
    },
  });

  const updateMutation = useMutation({
    mutationFn: async (data: ProfileFormValues) => {
      const rawPhone = data.phoneNumber.replace(/\D/g, '') || undefined;
      await userApi.updateMe({
        firstName: data.firstName || undefined,
        lastName: data.lastName || undefined,
        middleName: data.middleName || undefined,
        phoneNumber: rawPhone,
        passportSeries: data.passportSeries || undefined,
        passportNumber: data.passportNumber || undefined,
        cityId: city?.id,
      });
      if (data.email && data.email !== authInfo?.email) {
        await authApi.changeEmail(data.email);
      }
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['me'] });
      qc.invalidateQueries({ queryKey: ['auth-me'] });
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    },
  });

  const avatarMutation = useMutation({
    mutationFn: (file: File) => userApi.uploadAvatar(file),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['me'] }),
  });

  const deleteAvatarMutation = useMutation({
    mutationFn: () => userApi.deleteAvatar(),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['me'] }),
  });

  const handleAvatarChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) avatarMutation.mutate(file);
  };

  const { register: regPw, handleSubmit: handlePwSubmit, formState: { isSubmitting: pwSubmitting }, reset: resetPw } = useForm<{ oldPassword: string; newPassword: string; confirmPassword: string }>();

  const handlePwChange = async (data: { oldPassword: string; newPassword: string; confirmPassword: string }) => {
    setPwErr('');
    if (data.newPassword !== data.confirmPassword) { setPwErr('Пароли не совпадают'); return; }
    try {
      await authApi.changePassword({ oldPassword: data.oldPassword, newPassword: data.newPassword });
      setPwModal(false);
      resetPw();
    } catch {
      setPwErr('Неверный текущий пароль');
    }
  };

  const roleLabels: Record<string, string> = { USER: 'Пользователь', CURATOR: 'Куратор', COMPANY: 'Компания', ADMIN: 'Администратор' };

  if (isLoading) return <Layout><Spinner centered /></Layout>;

  return (
    <Layout>
      <div className={styles.page}>
        <div className={styles.sidebar}>
          <div className={styles.avatarWrap}>
            <Avatar src={user?.avatarUrl} name={user?.nickname} size="xl" />
            <button className={styles.avatarEdit} onClick={() => fileRef.current?.click()} title="Сменить аватар">
              <Camera size={14} />
            </button>
            {user?.avatarUrl && (
              <button
                className={styles.avatarDelete}
                onClick={() => deleteAvatarMutation.mutate()}
                title="Удалить аватар"
              >
                <Trash2 size={12} />
              </button>
            )}
            <input ref={fileRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={handleAvatarChange} />
          </div>
          <h2 className={styles.nickname}>{user?.nickname}</h2>
          <p className={styles.cityLabel}>{city?.name || user?.cityName}</p>
          {role && <Badge status="info">{roleLabels[role]}</Badge>}

          {bl?.inBlacklist && (
            <div className={styles.blBanner}>
              <AlertCircle size={16} />
              <span>Вы в чёрном списке{bl.until ? ` до ${new Date(bl.until).toLocaleDateString('ru-RU')}` : ''}{bl.reason ? `. Причина: ${bl.reason}` : ''}</span>
            </div>
          )}
        </div>

        <Card padding="lg" className={styles.formCard}>
          <h3 className={styles.formTitle}>Редактировать профиль</h3>
          <form onSubmit={handleSubmit(data => updateMutation.mutate(data))} className={styles.form}>

            <section className={styles.section}>
              <h4 className={styles.sectionTitle}>Персональные данные</h4>
              <div className={styles.row}>
                <div className={styles.field}>
                  <label>Фамилия</label>
                  <input {...register('lastName')} placeholder="Фамилия" />
                </div>
                <div className={styles.field}>
                  <label>Имя</label>
                  <input {...register('firstName')} placeholder="Имя" />
                </div>
              </div>
              <div className={styles.field}>
                <label>Отчество</label>
                <input {...register('middleName')} placeholder="Отчество" />
              </div>
            </section>

            <section className={styles.section}>
              <h4 className={styles.sectionTitle}>Контакты</h4>
              <div className={styles.row}>
                <div className={styles.field}>
                  <label>Телефон</label>
                  <Controller
                    name="phoneNumber"
                    control={control}
                    render={({ field }) => (
                      <input
                        value={formatPhoneDisplay(field.value)}
                        onChange={e => field.onChange(e.target.value.replace(/\D/g, '').slice(0, 11))}
                        onBlur={field.onBlur}
                        placeholder="+7 (999) 123-45-67"
                        inputMode="tel"
                      />
                    )}
                  />
                </div>
                <div className={styles.field}>
                  <label>Город</label>
                  <CityCombobox value={city} onChange={setCity} />
                </div>
              </div>
              <div className={styles.field}>
                <label>Email</label>
                <input {...register('email')} type="email" placeholder="email@example.com" />
              </div>
            </section>

            <section className={styles.section}>
              <h4 className={styles.sectionTitle}>Документы</h4>
              <div className={styles.row}>
                <div className={styles.field}>
                  <label>Серия паспорта</label>
                  <input {...register('passportSeries')} placeholder="1234" maxLength={4} />
                </div>
                <div className={styles.field}>
                  <label>Номер паспорта</label>
                  <input {...register('passportNumber')} placeholder="567890" maxLength={6} />
                </div>
              </div>
            </section>

            <div className={styles.actions}>
              <Button variant="secondary" type="button" onClick={() => setPwModal(true)}>Изменить пароль</Button>
              <Button type="submit" loading={isSubmitting || updateMutation.isPending}>
                {saved ? '✓ Сохранено' : 'Сохранить изменения'}
              </Button>
            </div>
          </form>
        </Card>
      </div>

      <Modal open={pwModal} onClose={() => setPwModal(false)} title="Изменить пароль" size="sm">
        <form onSubmit={handlePwSubmit(handlePwChange)} className={styles.form}>
          <div className={styles.field}><label>Текущий пароль</label><input {...regPw('oldPassword')} type="password" required /></div>
          <div className={styles.field}><label>Новый пароль</label><input {...regPw('newPassword')} type="password" required minLength={6} /></div>
          <div className={styles.field}><label>Подтверждение</label><input {...regPw('confirmPassword')} type="password" required /></div>
          {pwErr && <p className={styles.error}>{pwErr}</p>}
          <Button type="submit" fullWidth loading={pwSubmitting}>Сохранить</Button>
        </form>
      </Modal>
    </Layout>
  );
}
