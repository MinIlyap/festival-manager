import styles from './Spinner.module.css';

interface SpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  centered?: boolean;
}

export default function Spinner({ size = 'md', centered = false }: SpinnerProps) {
  const el = <span className={[styles.spinner, styles[size]].join(' ')} />;
  if (centered) return <div className={styles.centered}>{el}</div>;
  return el;
}
