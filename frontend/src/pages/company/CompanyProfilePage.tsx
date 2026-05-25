import { useState, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { Camera, Trash2 } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Avatar from '../../components/ui/Avatar';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import Card from '../../components/ui/Card';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import { companyApi } from '../../api/company';
import { authApi } from '../../api/auth';
import styles from '../profile/ProfilePage.module.css';

interface CompanyFormValues {
  fullName: string;
  brandName: string;
  website: string;
  email: string;
}

export default function CompanyProfilePage() {
  const qc = useQueryClient();
  const [saved, setSaved] = useState(false);
  const [pwModal, setPwModal] = useState(false);
  const [pwErr, setPwErr] = useState('');
  const fileRef = useRef<HTMLInputElement>(null);

  const { data: company, isLoading } = useQuery({
    queryKey: ['company-me'],
    queryFn: companyApi.getMe,
  });

  const { data: authInfo } = useQuery({
    queryKey: ['auth-me'],
    queryFn: authApi.getMe,
  });

  const { register, handleSubmit, formState: { isSubmitting } } = useForm<CompanyFormValues>({
    values: {
      fullName: company?.fullName ?? '',
      brandName: company?.brandName ?? '',
      website: company?.website ?? '',
      email: authInfo?.email ?? '',
    },
  });

  const updateMutation = useMutation({
    mutationFn: async (data: CompanyFormValues) => {
      const website = data.website
        ? /^https?:\/\//i.test(data.website) ? data.website : `https://${data.website}`
        : undefined;
      await companyApi.updateMe({ fullName: data.fullName, brandName: data.brandName, website });
      if (data.email && data.email !== authInfo?.email) {
        await authApi.changeEmail(data.email);
      }
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['company-me'] });
      qc.invalidateQueries({ queryKey: ['auth-me'] });
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    },
  });

  const logoMutation = useMutation({
    mutationFn: (file: File) => companyApi.uploadLogo(company!.id, file),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['company-me'] }),
  });

  const deleteLogoMutation = useMutation({
    mutationFn: () => companyApi.deleteLogo(company!.id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['company-me'] }),
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
            <Avatar src={company?.logoUrl} name={company?.brandName || company?.fullName} size="xl" />
            <button className={styles.avatarEdit} onClick={() => fileRef.current?.click()} title="Сменить логотип">
              <Camera size={14} />
            </button>
            {company?.logoUrl && (
              <button className={styles.avatarDelete} onClick={() => deleteLogoMutation.mutate()} title="Удалить логотип">
                <Trash2 size={12} />
              </button>
            )}
            <input ref={fileRef} type="file" accept="image/*" style={{ display: 'none' }}
              onChange={e => { const f = e.target.files?.[0]; if (f) logoMutation.mutate(f); }} />
          </div>
          <h2 className={styles.nickname}>{company?.brandName || company?.fullName}</h2>
          <p className={styles.cityLabel} style={{ fontFamily: 'var(--font-mono)', fontSize: 12 }}>ИНН: {company?.tin}</p>
          <Badge status={company?.active ? 'active' : 'rejected'}>
            {company?.active ? 'Активна' : 'Не активна'}
          </Badge>
        </div>

        <Card padding="lg" className={styles.formCard}>
          <h3 className={styles.formTitle}>Редактировать профиль компании</h3>
          <form onSubmit={handleSubmit(data => updateMutation.mutate(data))} className={styles.form}>

            <section className={styles.section}>
              <h4 className={styles.sectionTitle}>Реквизиты</h4>
              <div className={styles.field}>
                <label>Полное наименование</label>
                <input {...register('fullName', { required: true })} placeholder="ООО «Название»" />
              </div>
              <div className={styles.field}>
                <label>Название бренда</label>
                <input {...register('brandName', { required: true })} placeholder="Бренд" />
              </div>
              <div className={styles.field}>
                <label>Веб-сайт</label>
                <input {...register('website')} type="text" placeholder="www.example.com" />
              </div>
            </section>

            <section className={styles.section}>
              <h4 className={styles.sectionTitle}>Аккаунт</h4>
              <div className={styles.field}>
                <label>Email</label>
                <input {...register('email')} type="email" placeholder="company@example.com" />
              </div>
              <div className={styles.field}>
                <label>ИНН <span className={styles.readOnlyHint}>(только чтение)</span></label>
                <input value={company?.tin ?? ''} readOnly className={styles.readOnly} />
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
