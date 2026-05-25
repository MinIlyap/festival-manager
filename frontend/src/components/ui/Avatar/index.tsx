import { useState, useEffect } from 'react';
import styles from './Avatar.module.css';

interface AvatarProps {
  src?: string;
  name?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  onClick?: () => void;
}

export default function Avatar({ src, name, size = 'md', onClick }: AvatarProps) {
  const [imgError, setImgError] = useState(false);

  useEffect(() => {
    setImgError(false);
  }, [src]);

  const initials = name
    ? name.split(' ').map(w => w[0]).filter(Boolean).join('').toUpperCase().slice(0, 2)
    : '?';

  const showImage = src && !imgError;

  return (
    <div
      className={[styles.avatar, styles[size], onClick ? styles.clickable : ''].join(' ')}
      onClick={onClick}
      title={name}
    >
      {showImage
        ? <img src={src} alt={name} onError={() => setImgError(true)} />
        : <span>{initials}</span>
      }
    </div>
  );
}
