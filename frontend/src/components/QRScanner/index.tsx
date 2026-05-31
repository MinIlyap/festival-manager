import { useEffect, useRef, useState } from 'react';
import { Html5Qrcode } from 'html5-qrcode';
import { CameraOff, Loader2 } from 'lucide-react';
import styles from './QRScanner.module.css';

interface QRScannerProps {
  onScan: (result: string) => void;
}

let idCounter = 0;

export default function QRScanner({ onScan }: QRScannerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const onScanRef    = useRef(onScan);
  const activeRef    = useRef(true);
  const doneRef      = useRef(false);
  const [status, setStatus]     = useState<'loading' | 'scanning' | 'error'>('loading');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => { onScanRef.current = onScan; });

  useEffect(() => {
    activeRef.current = true;
    doneRef.current   = false;
    setStatus('loading');
    setErrorMsg('');

    const container = containerRef.current;
    if (!container) return;

    container.innerHTML = '';
    const divId = `qr-scanner-${++idCounter}`;
    container.id = divId;

    let scanner: Html5Qrcode;
    try {
      scanner = new Html5Qrcode(divId, false as never);
    } catch {
      if (activeRef.current) {
        setStatus('error');
        setErrorMsg('Не удалось инициализировать сканер');
      }
      return;
    }

    const onSuccess = (text: string) => {
      if (!activeRef.current || doneRef.current) return;
      doneRef.current = true;
      onScanRef.current(text);
    };

    const config = { fps: 10, qrbox: { width: 220, height: 220 } };

    scanner
      .start({ facingMode: 'environment' }, config, onSuccess, undefined)
      .then(() => { if (activeRef.current) setStatus('scanning'); })
      .catch(() =>
        scanner
          .start({ facingMode: 'user' }, config, onSuccess, undefined)
          .then(() => { if (activeRef.current) setStatus('scanning'); })
          .catch(() => {
            if (activeRef.current) {
              setStatus('error');
              setErrorMsg('Нет доступа к камере. Разрешите доступ в настройках браузера и перезагрузите страницу.');
            }
          })
      );

    return () => {
      activeRef.current = false;
      scanner.stop().catch(() => {}).finally(() => scanner.clear().catch(() => {}));
    };
  }, []);

  if (status === 'error') {
    return (
      <div className={styles.errorState}>
        <CameraOff size={36} className={styles.errorIcon} />
        <p className={styles.errorText}>{errorMsg}</p>
      </div>
    );
  }

  return (
    <div className={styles.wrap}>
      {status === 'loading' && (
        <div className={styles.loadingOverlay}>
          <Loader2 size={28} className={styles.loadingIcon} />
          <p className={styles.loadingText}>Инициализация камеры…</p>
        </div>
      )}
      <div ref={containerRef} className={styles.scanner} />
    </div>
  );
}
