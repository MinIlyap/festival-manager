import { useEffect, useRef } from 'react';
import { Html5Qrcode } from 'html5-qrcode';
import styles from './QRScanner.module.css';

interface QRScannerProps {
  onScan: (result: string) => void;
}

let idCounter = 0;

export default function QRScanner({ onScan }: QRScannerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const scannerRef = useRef<Html5Qrcode | null>(null);
  const onScanRef = useRef(onScan);

  useEffect(() => { onScanRef.current = onScan; });

  useEffect(() => {
    const divId = `qr-scanner-${++idCounter}`;
    const container = containerRef.current;
    if (!container) return;
    container.id = divId;

    const scanner = new Html5Qrcode(divId);
    scannerRef.current = scanner;

    scanner
      .start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: { width: 240, height: 240 } },
        (text) => {
          onScanRef.current(text);
          scanner.pause();
        },
        undefined,
      )
      .catch(err => console.warn('QR scanner start failed:', err));

    return () => {
      scanner
        .stop()
        .catch(() => {})
        .finally(() => { scanner.clear().catch(() => {}); });
      scannerRef.current = null;
    };
  }, []);

  return (
    <div className={styles.wrap}>
      <div ref={containerRef} className={styles.scanner} />
    </div>
  );
}
