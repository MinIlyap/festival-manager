import { LucideIcon } from 'lucide-react';
import Button from '../Button';
import styles from './EmptyState.module.css';

interface EmptyStateProps {
  icon: LucideIcon;
  message: string;
  description?: string;
  action?: { label: string; onClick: () => void };
}

export default function EmptyState({ icon: Icon, message, description, action }: EmptyStateProps) {
  return (
    <div className={styles.wrap}>
      <div className={styles.iconWrap}>
        <Icon size={40} className={styles.icon} />
      </div>
      <p className={styles.message}>{message}</p>
      {description && <p className={styles.desc}>{description}</p>}
      {action && (
        <Button variant="secondary" onClick={action.onClick}>
          {action.label}
        </Button>
      )}
    </div>
  );
}
