import { QRCodeSVG } from 'qrcode.react';
import Modal from '../ui/Modal';
import styles from './QRModal.module.css';

interface QRModalProps {
  open: boolean;
  onClose: () => void;
  value: string;
  title?: string;
  subtitle?: string;
}

export default function QRModal({ open, onClose, value, title, subtitle }: QRModalProps) {
  return (
    <Modal open={open} onClose={onClose} title={title} size="sm">
      <div className={styles.content}>
        <div className={styles.qrWrap}>
          <QRCodeSVG
            value={value}
            size={240}
            bgColor="transparent"
            fgColor="var(--text-primary)"
            level="H"
          />
        </div>
        {subtitle && <p className={styles.subtitle}>{subtitle}</p>}
      </div>
    </Modal>
  );
}
