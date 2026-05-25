import { useState, useRef, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { Camera, Trash2 } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Avatar from '../../components/ui/Avatar';
import Badge from '../../components/ui/Badge';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import CityCombobox from '../../components/CityCombobox';
import { userApi } from '../../api/user';
import { authApi } from '../../api/auth';
import type { City } from '../../types';
import styles from '../profile/ProfilePage.module.css';

interface ProfileFormValues {
  firstName: string;
  lastName: string;
  middleName: string;
  phoneNumber: string;
  passportSeries: string;
  passportNumber: string;
  email: string;
}

export default function CuratorProfilePage() {
  const qc = useQueryClient();
  const fileRef = useRef<HTMLInputElement>(null);
  const [city, setCity] = useState<City | null>(null);
  const [pwModal, setPwModal] = useState(false);
  const [saved, setSaved] = useState(false);
  const [pwErr, setPwErr] = useState('');

  const { data: user, isLoading } = useQuery({ queryKey: ['me'], queryFn: userApi.getMe });
  const { data: authInfo } = useQuery({ queryKey: ['auth-me'], queryFn: authApi.getMe });

  useEffect(() => {
    if (user?.cityId && !city) {
      setCity({ id: user.cityId, name: user.cityName ?? '' });
    }
  }, [user]);

  const { register, handleSubmit, formState: { isSubmitting } } = useForm<ProfileFormValues>({
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
      await userApi.updateMe({
        firstName: data.firstName || undefined,
        lastName: data.lastName || undefined,
        middleName: data.middleName || undefined,
        phoneNumber: data.phoneNumber || undefined,
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
              <button className={styles.avatarDelete} onClick={() => deleteAvatarMutation.mutate()} title="Удалить аватар">
                <Trash2 size={12} />
              </button>
            )}
            <input ref={fileRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={e => { if (e.target.files?.[0]) avatarMutation.mutate(e.target.files[0]); }} />
          </div>
          <h2 className={styles.nickname}>{user?.nickname}</h2>
          <p className={styles.cityLabel}>{city?.name || user?.cityName}</p>
          <Badge status="info">Куратор</Badge>
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
                  <input {...register('phoneNumber')} placeholder="+7 999 123-45-67" />
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
