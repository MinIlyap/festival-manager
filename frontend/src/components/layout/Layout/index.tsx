import { motion } from 'framer-motion';
import Header from '../Header';
import Footer from '../Footer';
import styles from './Layout.module.css';

interface LayoutProps {
  children: React.ReactNode;
  noFooter?: boolean;
}

export default function Layout({ children, noFooter = false }: LayoutProps) {
  return (
    <div className={styles.layout}>
      <Header />
      <motion.main
        className={styles.main}
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
      >
        {children}
      </motion.main>
      {!noFooter && <Footer />}
    </div>
  );
}
