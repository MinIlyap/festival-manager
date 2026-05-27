import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import Button from '../../components/ui/Button';
import CityCombobox from '../../components/CityCombobox';
import { authApi } from '../../api/auth';
import { useAuthStore } from '../../store/authStore';
import type { City } from '../../types';
import styles from './AuthPage.module.css';

const userSchema = z.object({
  nickname: z.string().min(3, 'Минимум 3 символа').max(70, 'Максимум 70 символов'),
  email: z.string().email('Некорректный email'),
  password: z.string().min(8, 'Минимум 8 символов').regex(/[0-9]/, 'Нужна цифра').regex(/[A-Z]/, 'Нужна заглавная буква'),
  confirmPassword: z.string(),
  birthDate: z.string().min(1, 'Дата рождения обязательна'),
}).refine(d => d.password === d.confirmPassword, { message: 'Пароли не совпадают', path: ['confirmPassword'] });

const companySchema = z.object({
  email: z.string().email('Некорректный email'),
  fullName: z.string().min(2, 'Укажите наименование'),
  brandName: z.string().min(1, 'Укажите бренд'),
  tin: z.string().regex(/^\d{10}$|^\d{12}$/, 'ИНН — 10 или 12 цифр'),
  website: z.string().url('Некорректный URL').optional().or(z.literal('')),
});

type UserForm = z.infer<typeof userSchema>;
type CompanyForm = z.infer<typeof companySchema>;

function UserRegister() {
  const navigate = useNavigate();
  const { setTokens } = useAuthStore();
  const [showPass, setShowPass] = useState(false);
  const [city, setCity] = useState<City | null>(null);
  const [cityErr, setCityErr] = useState('');
  const [err, setErr] = useState('');

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<UserForm>({
    resolver: zodResolver(userSchema),
  });

  const onSubmit = async (data: UserForm) => {
    if (!city) { setCityErr('Выберите город'); return; }
    setCityErr(''); setErr('');
    try {
      const res = await authApi.register({
        nickname: data.nickname,
        email: data.email,
        password: data.password,
        birth_date: data.birthDate,
        city_id: Number(city.id),
      });
      setTokens(res.accessToken, res.refreshToken);
      navigate('/home');
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setErr(msg ?? 'Ошибка регистрации');
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className={styles.formBody}>
      <div className={styles.field}>
        <label className={styles.label}>Псевдоним *</label>
        <input {...register('nickname')} placeholder="gamernick42" />
        {errors.nickname && <span className={styles.fieldError}>{errors.nickname.message}</span>}
      </div>
      <div className={styles.field}>
        <label className={styles.label}>Email *</label>
        <input {...register('email')} type="email" placeholder="you@example.com" />
        {errors.email && <span className={styles.fieldError}>{errors.email.message}</span>}
      </div>
      <div className={styles.field}>
        <label className={styles.label}>Пароль *</label>
        <div className={styles.passWrap}>
          <input {...register('password')} type={showPass ? 'text' : 'password'} placeholder="••••••••" />
          <button type="button" className={styles.eyeBtn} onClick={() => setShowPass(v => !v)}>
            {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
        </div>
        {errors.password && <span className={styles.fieldError}>{errors.password.message}</span>}
      </div>
      <div className={styles.field}>
        <label className={styles.label}>Подтверждение пароля *</label>
        <input {...register('confirmPassword')} type="password" placeholder="••••••••" />
        {errors.confirmPassword && <span className={styles.fieldError}>{errors.confirmPassword.message}</span>}
      </div>
      <div className={styles.field}>
        <label className={styles.label}>Дата рождения *</label>
        <input {...register('birthDate')} type="date" />
        {errors.birthDate && <span className={styles.fieldError}>{errors.birthDate.message}</span>}
      </div>
      <div className={styles.field}>
        <label className={styles.label}>Город *</label>
        <CityCombobox value={city} onChange={setCity} error={cityErr} />
      </div>
      {err && <p className={styles.globalError}>{err}</p>}
      <Button type="submit" fullWidth loading={isSubmitting}>Зарегистрироваться</Button>
    </form>
  );
}

function CompanyRegister() {
  const [err, setErr] = useState('');
  const [success, setSuccess] = useState(false);

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<CompanyForm>({
    resolver: zodResolver(companySchema),
  });

  const onSubmit = async (data: CompanyForm) => {
    setErr('');
    try {
      await authApi.registerCompany({
        email: data.email,
        tin: data.tin,
        full_name: data.fullName,
        brand_name: data.brandName,
        website: data.website || undefined,
      });
      setSuccess(true);
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setErr(msg ?? 'Ошибка подачи заявки');
    }
  };

  if (success) {
    return (
      <div className={styles.successBox}>
        <span className={styles.successIcon}>✅</span>
        <p className={styles.successTitle}>Заявка отправлена!</p>
        <p className={styles.successDesc}>Ожидайте решения на указанный email. Администратор рассмотрит заявку в ближайшее время.</p>
        <Link to="/auth/login"><Button variant="secondary">Перейти ко входу</Button></Link>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className={styles.formBody}>
      <div className={styles.infoBox}>
        ⚠️ Вы подаёте заявку на регистрацию компании. После проверки администратором вы получите уведомление.
      </div>
      <div className={styles.field}>
        <label className={styles.label}>Email компании *</label>
        <input {...register('email')} type="email" placeholder="company@example.com" />
        {errors.email && <span className={styles.fieldError}>{errors.email.message}</span>}
      </div>
      <div className={styles.field}>
        <label className={styles.label}>Полное наименование (юр.) *</label>
        <input {...register('fullName')} placeholder='ООО "Игровые миры"' />
        {errors.fullName && <span className={styles.fieldError}>{errors.fullName.message}</span>}
      </div>
      <div className={styles.field}>
        <label className={styles.label}>Название бренда *</label>
        <input {...register('brandName')} placeholder="GameWorld" />
        {errors.brandName && <span className={styles.fieldError}>{errors.brandName.message}</span>}
      </div>
      <div className={styles.field}>
        <label className={styles.label}>ИНН *</label>
        <input {...register('tin')} placeholder="1234567890" />
        {errors.tin && <span className={styles.fieldError}>{errors.tin.message}</span>}
      </div>
      <div className={styles.field}>
        <label className={styles.label}>Сайт</label>
        <input {...register('website')} placeholder="https://example.com" />
        {errors.website && <span className={styles.fieldError}>{errors.website.message}</span>}
      </div>
      {err && <p className={styles.globalError}>{err}</p>}
      <Button type="submit" fullWidth loading={isSubmitting}>Подать заявку</Button>
    </form>
  );
}

export default function RegisterPage() {
  const [tab, setTab] = useState<'user' | 'company'>('user');

  return (
    <div className={styles.page}>
      <div className={styles.left}>
        <motion.div className={styles.leftContent} initial={{ opacity: 0, x: -30 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.6 }}>
          <div className={styles.logoBig}>
            <svg width="64" height="64" viewBox="0 0 32 32" fill="none">
              <circle cx="16" cy="16" r="15" fill="var(--accent-primary)" />
              <text x="16" y="22" textAnchor="middle" fontFamily="'Bebas Neue', sans-serif" fontSize="18" fill="#fff" fontWeight="bold">F</text>
            </svg>
            <span className={styles.logoName}>FestivalHub</span>
          </div>
          <h1 className={styles.slogan}>Присоединяйся<br />к сообществу</h1>
          <p className={styles.sloganSub}>Участвуй в фестивалях, организуй события, находи команду</p>
        </motion.div>
        <div className={styles.meshGradient} />
      </div>

      <div className={styles.right}>
        <motion.div className={styles.form} initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4 }}>
          <h2 className={styles.formTitle}>Регистрация</h2>

          <div className={styles.switcher}>
            <button className={[styles.switchBtn, tab === 'user' ? styles.switchBtnActive : ''].join(' ')} onClick={() => setTab('user')}>
              Пользователь
            </button>
            <button className={[styles.switchBtn, tab === 'company' ? styles.switchBtnActive : ''].join(' ')} onClick={() => setTab('company')}>
              Компания
            </button>
          </div>

          <AnimatePresence mode="wait">
            <motion.div
              key={tab}
              initial={{ opacity: 0, x: 10 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -10 }}
              transition={{ duration: 0.2 }}
            >
              {tab === 'user' ? <UserRegister /> : <CompanyRegister />}
            </motion.div>
          </AnimatePresence>

          <p className={styles.altLink}>
            Уже есть аккаунт? <Link to="/auth/login">Войти</Link>
          </p>
        </motion.div>
      </div>
    </div>
  );
}
