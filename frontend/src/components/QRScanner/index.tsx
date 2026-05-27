import { useEffect, useRef, useState } from 'react';
import { Html5Qrcode } from 'html5-qrcode';
import { CameraOff } from 'lucide-react';
import styles from './QRScanner.module.css';

interface QRScannerProps {
  onScan: (result: string) => void;
}

let idCounter = 0;

export default function QRScanner({ onScan }: QRScannerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const onScanRef = useRef(onScan);
  const activeRef = useRef(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => { onScanRef.current = onScan; });

  useEffect(() => {
    activeRef.current = true;
    setError(null);

    const container = containerRef.current;
    if (!container) return;

    // Clear any DOM injected by a previous mount (React Strict Mode double-invoke)
    container.innerHTML = '';
    const divId = `qr-scanner-${++idCounter}`;
    container.id = divId;

    let scanner: Html5Qrcode;
    try {
      scanner = new Html5Qrcode(divId, { verbose: false } as never);
    } catch {
      if (activeRef.current) setError('Не удалось инициализировать сканер');
      return;
    }

    scanner
      .start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: { width: 240, height: 240 } },
        (text) => {
          // Guard: don't fire after cleanup
          if (activeRef.current) onScanRef.current(text);
        },
        undefined,
      )
      .catch(() => {
        if (activeRef.current) {
          setError('Нет доступа к камере. Разрешите доступ в настройках браузера и перезагрузите страницу.');
        }
      });

    return () => {
      activeRef.current = false;
      scanner.stop().catch(() => {}).finally(() => scanner.clear().catch(() => {}));
    };
  }, []);

  if (error) {
    return (
      <div className={styles.errorState}>
        <CameraOff size={36} className={styles.errorIcon} />
        <p className={styles.errorText}>{error}</p>
      </div>
    );
  }

  return (
    <div className={styles.wrap}>
      <div ref={containerRef} className={styles.scanner} />
    </div>
  );
}
