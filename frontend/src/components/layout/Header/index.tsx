import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Bell, User, History, Ticket, Building2, CalendarDays, LayoutList, ClipboardList, Star, LogOut, ShieldBan } from 'lucide-react';
import { useAuthStore } from '../../../store/authStore';
import { useNotificationStore } from '../../../store/notificationStore';
import { authApi } from '../../../api/auth';
import styles from './Header.module.css';

interface NavItem {
  icon: React.ElementType;
  label: string;
  to: string;
  badge?: number;
}

function Logo() {
  return (
    <Link to="/home" className={styles.logo}>
      <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
        <circle cx="16" cy="16" r="15" fill="var(--accent-primary)" />
        <text x="16" y="22" textAnchor="middle" fontFamily="'Bebas Neue', sans-serif" fontSize="18" fill="#fff" fontWeight="bold">Ф</text>
      </svg>
      <span className={styles.logoText}>ФестХаб</span>
    </Link>
  );
}

export default function Header() {
  const location = useLocation();
  const navigate = useNavigate();
  const { role, logout } = useAuthStore();
  const unread = useNotificationStore(s => s.unreadCount);

  const handleLogout = async () => {
    try { await authApi.logout(); } catch { /* ignore */ }
    logout();
    navigate('/auth/login');
  };

  const navItems: NavItem[] = [];

  if (role === 'USER') {
    navItems.push(
      { icon: User,        label: 'Профиль',       to: '/profile' },
      { icon: History,     label: 'История',        to: '/history' },
      { icon: Ticket,      label: 'Билеты',          to: '/tickets' },
      { icon: Bell,        label: 'Уведомления',    to: '/notifications', badge: unread },
    );
  } else if (role === 'COMPANY') {
    navItems.push(
      { icon: Building2,     label: 'Профиль',       to: '/company/profile' },
      { icon: CalendarDays,  label: 'Фестивали',     to: '/company/festivals' },
      { icon: LayoutList,    label: 'Мероприятия',   to: '/company/events' },
      { icon: ClipboardList, label: 'Заявки',         to: '/company/applications' },
      { icon: Bell,          label: 'Уведомления',   to: '/notifications', badge: unread },
    );
  } else if (role === 'ADMIN') {
    navItems.push(
      { icon: User,          label: 'Профиль',       to: '/admin/profile' },
      { icon: ClipboardList, label: 'Заявки',         to: '/admin/applications' },
      { icon: History,       label: 'История',        to: '/admin/history' },
      { icon: Bell,          label: 'Уведомления',   to: '/notifications', badge: unread },
    );
  } else if (role === 'CURATOR') {
    navItems.push(
      { icon: User,      label: 'Профиль',         to: '/curator/profile' },
      { icon: Star,      label: 'Фестивали',       to: '/curator/festivals' },
      { icon: History,   label: 'История',          to: '/curator/history' },
      { icon: Ticket,    label: 'Билеты',           to: '/curator/tickets' },
      { icon: ShieldBan, label: 'Чёрный список',   to: '/curator/blacklist' },
      { icon: Bell,      label: 'Уведомления',      to: '/notifications', badge: unread },
    );
  }

  return (
    <header className={styles.header}>
      <div className={styles.inner}>
        <Logo />
        <nav className={styles.nav}>
          {navItems.map(item => {
            const Icon = item.icon;
            const isActive = location.pathname === item.to;
            return (
              <Link
                key={item.to}
                to={item.to}
                className={[styles.navItem, isActive ? styles.navActive : ''].join(' ')}
                title={item.label}
              >
                <div className={styles.iconWrap}>
                  <Icon size={20} />
                  {item.badge ? <span className={styles.badge}>{item.badge > 99 ? '99+' : item.badge}</span> : null}
                </div>
              </Link>
            );
          })}
          <button className={styles.navItem} onClick={handleLogout} title="Выйти">
            <div className={styles.iconWrap}>
              <LogOut size={20} />
            </div>
          </button>
        </nav>
      </div>
    </header>
  );
}
