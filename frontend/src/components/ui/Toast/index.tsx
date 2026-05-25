import { useEffect } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { CheckCircle, XCircle, Info, X } from 'lucide-react';
import styles from './Toast.module.css';

export type ToastType = 'success' | 'error' | 'info';

export interface ToastItem {
  id: string;
  type: ToastType;
  message: string;
}

interface ToastProps {
  toasts: ToastItem[];
  onRemove: (id: string) => void;
}

const icons = {
  success: CheckCircle,
  error: XCircle,
  info: Info,
};

function ToastEntry({ toast, onRemove }: { toast: ToastItem; onRemove: (id: string) => void }) {
  const Icon = icons[toast.type];

  useEffect(() => {
    const t = setTimeout(() => onRemove(toast.id), 4000);
    return () => clearTimeout(t);
  }, [toast.id, onRemove]);

  return (
    <motion.div
      className={[styles.toast, styles[toast.type]].join(' ')}
      initial={{ opacity: 0, x: 60, scale: 0.95 }}
      animate={{ opacity: 1, x: 0, scale: 1 }}
      exit={{ opacity: 0, x: 60, scale: 0.95 }}
      transition={{ duration: 0.25 }}
    >
      <Icon size={18} className={styles.icon} />
      <span className={styles.msg}>{toast.message}</span>
      <button className={styles.close} onClick={() => onRemove(toast.id)}><X size={14} /></button>
    </motion.div>
  );
}

export default function Toast({ toasts, onRemove }: ToastProps) {
  return (
    <div className={styles.container}>
      <AnimatePresence>
        {toasts.map(t => (
          <ToastEntry key={t.id} toast={t} onRemove={onRemove} />
        ))}
      </AnimatePresence>
    </div>
  );
}
