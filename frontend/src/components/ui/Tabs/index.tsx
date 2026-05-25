import { useState, useRef, useEffect } from 'react';
import { motion } from 'framer-motion';
import styles from './Tabs.module.css';

interface Tab {
  key: string;
  label: React.ReactNode;
}

interface TabsProps {
  tabs: Tab[];
  activeKey: string;
  onChange: (key: string) => void;
  className?: string;
}

export default function Tabs({ tabs, activeKey, onChange, className }: TabsProps) {
  const [indicatorStyle, setIndicatorStyle] = useState({ left: 0, width: 0 });
  const tabRefs = useRef<(HTMLButtonElement | null)[]>([]);

  useEffect(() => {
    const idx = tabs.findIndex(t => t.key === activeKey);
    const el = tabRefs.current[idx];
    if (el) {
      setIndicatorStyle({ left: el.offsetLeft, width: el.offsetWidth });
    }
  }, [activeKey, tabs]);

  return (
    <div className={[styles.tabs, className ?? ''].join(' ')}>
      <div className={styles.inner}>
        {tabs.map((tab, i) => (
          <button
            key={tab.key}
            ref={el => { tabRefs.current[i] = el; }}
            className={[styles.tab, activeKey === tab.key ? styles.active : ''].join(' ')}
            onClick={() => onChange(tab.key)}
          >
            {tab.label}
          </button>
        ))}
        <motion.div
          className={styles.indicator}
          animate={indicatorStyle}
          transition={{ type: 'spring', stiffness: 400, damping: 30 }}
        />
      </div>
    </div>
  );
}
