import styles from './Footer.module.css';

export default function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={styles.inner}>
        <span>© 2026 FestivalHub. Все права защищены.</span>
      </div>
    </footer>
  );
}
