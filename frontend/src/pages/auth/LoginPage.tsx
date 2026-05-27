import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff } from 'lucide-react';
import { motion } from 'framer-motion';
import Button from '../../components/ui/Button';
import { authApi } from '../../api/auth';
import { useAuthStore } from '../../store/authStore';
import styles from './AuthPage.module.css';

const schema = z.object({
  email: z.string().email('Некорректный email'),
  password: z.string().min(1, 'Введите пароль'),
});

type FormData = z.infer<typeof schema>;

export default function LoginPage() {
  const navigate = useNavigate();
  const { setTokens } = useAuthStore();
  const [showPass, setShowPass] = useState(false);
  const [error, setError] = useState('');

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data: FormData) => {
    setError('');
    try {
      const res = await authApi.login(data);
      setTokens(res.accessToken, res.refreshToken);
      navigate('/home');
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg ?? 'Неверный email или пароль');
    }
  };

  const handleYandex = async () => {
    try {
      const { url } = await authApi.getYandexAuthUrl();
      window.location.href = url;
    } catch {
      setError('Ошибка подключения к Яндекс ID');
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.left}>
        <motion.div
          className={styles.leftContent}
          initial={{ opacity: 0, x: -30 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.6 }}
        >
          <div className={styles.logoBig}>
            <svg width="64" height="64" viewBox="0 0 32 32" fill="none">
              <circle cx="16" cy="16" r="15" fill="var(--accent-primary)" />
              <text x="16" y="22" textAnchor="middle" fontFamily="'Bebas Neue', sans-serif" fontSize="18" fill="#fff" fontWeight="bold">Ф</text>
            </svg>
            <span className={styles.logoName}>ФестХаб</span>
          </div>
          <h1 className={styles.slogan}>Управляй фестивалями<br />легко и эффективно</h1>
          <p className={styles.sloganSub}>Единая платформа для организаторов, участников и кураторов игровых фестивалей</p>
        </motion.div>
        <div className={styles.meshGradient} />
      </div>

      <div className={styles.right}>
        <motion.div
          className={styles.form}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, delay: 0.1 }}
        >
          <h2 className={styles.formTitle}>Войти в аккаунт</h2>

          <form onSubmit={handleSubmit(onSubmit)} className={styles.formBody}>
            <div className={styles.field}>
              <label className={styles.label}>Email</label>
              <input {...register('email')} type="email" placeholder="example@mail.ru" />
              {errors.email && <span className={styles.fieldError}>{errors.email.message}</span>}
            </div>

            <div className={styles.field}>
              <label className={styles.label}>Пароль</label>
              <div className={styles.passWrap}>
                <input {...register('password')} type={showPass ? 'text' : 'password'} placeholder="••••••••" />
                <button type="button" className={styles.eyeBtn} onClick={() => setShowPass(v => !v)}>
                  {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              {errors.password && <span className={styles.fieldError}>{errors.password.message}</span>}
            </div>

            {error && <p className={styles.globalError}>{error}</p>}

            <Button type="submit" fullWidth loading={isSubmitting}>
              Войти
            </Button>
          </form>

          <div className={styles.divider}><span>или</span></div>

          <Button variant="secondary" fullWidth onClick={handleYandex}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
              <path d="M2.04 12c0-5.523 4.476-10 9.999-10C17.523 2 22 6.477 22 12s-4.477 10-10.001 10C6.516 22 2.04 17.523 2.04 12Z" fill="#FC3F1D"/>
              <path d="M13.32 7.666h-.924c-1.694 0-2.585.858-2.585 2.123 0 1.43.616 2.1 1.893 2.959l1.045.704-3.003 4.548H7.817l2.771-4.156c-1.598-1.109-2.504-2.2-2.504-4.017 0-2.29 1.595-3.838 4.682-3.838h3.045v12H13.32V7.666Z" fill="#fff"/>
            </svg>
            Войти через Yandex ID
          </Button>

          <p className={styles.altLink}>
            Нет аккаунта? <Link to="/auth/register">Зарегистрироваться</Link>
          </p>
        </motion.div>
      </div>
    </div>
  );
}
