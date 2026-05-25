import styles from './Tag.module.css';

interface TagProps {
  children: React.ReactNode;
  color?: 'default' | 'gold' | 'blue';
}

export default function Tag({ children, color = 'default' }: TagProps) {
  return <span className={[styles.tag, styles[color]].join(' ')}>{children}</span>;
}
