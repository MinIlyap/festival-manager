import styles from './Footer.module.css';

export default function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={styles.inner}>
        <span>© 2025 FestivalHub. Все права защищены.</span>
        <div className={styles.links}>
          <a href="#">О проекте</a>
          <a href="#">Поддержка</a>
          <a href="#">Политика конфиденциальности</a>
        </div>
      </div>
    </footer>
  );
}
