import { HTMLAttributes } from 'react';
import { motion } from 'framer-motion';
import styles from './Card.module.css';

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  hoverable?: boolean;
  padding?: 'none' | 'sm' | 'md' | 'lg';
}

export default function Card({ hoverable = false, padding = 'md', className, children, ...props }: CardProps) {
  const cls = [styles.card, hoverable ? styles.hoverable : '', styles[`p-${padding}`], className ?? ''].join(' ');
  if (hoverable) {
    return (
      <motion.div className={cls} whileHover={{ y: -2, boxShadow: '0 8px 32px rgba(0,0,0,0.5)' }} {...(props as Record<string, unknown>)}>
        {children}
      </motion.div>
    );
  }
  return <div className={cls} {...props}>{children}</div>;
}
