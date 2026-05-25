import { ButtonHTMLAttributes, forwardRef } from 'react';
import { motion } from 'framer-motion';
import styles from './Button.module.css';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  loading?: boolean;
  fullWidth?: boolean;
}

const Button = forwardRef<HTMLButtonElement, ButtonProps>(({
  variant = 'primary',
  size = 'md',
  loading = false,
  fullWidth = false,
  children,
  disabled,
  className,
  ...props
}, ref) => {
  return (
    <motion.button
      ref={ref}
      className={[
        styles.btn,
        styles[variant],
        styles[size],
        fullWidth ? styles.fullWidth : '',
        className ?? '',
      ].join(' ')}
      disabled={disabled || loading}
      whileTap={{ scale: 0.97 }}
      {...(props as Record<string, unknown>)}
    >
      {loading ? <span className={styles.spinner} /> : null}
      {children}
    </motion.button>
  );
});

Button.displayName = 'Button';
export default Button;
