import styles from './Badge.module.css';

type BadgeStatus = 'active' | 'pending' | 'approved' | 'rejected' | 'expired' | 'draft' | 'info' | 'warn';

interface BadgeProps {
  status?: BadgeStatus;
  children: React.ReactNode;
  className?: string;
}

export default function Badge({ status = 'info', children, className }: BadgeProps) {
  return (
    <span className={[styles.badge, styles[status], className ?? ''].join(' ')}>
      {children}
    </span>
  );
}
