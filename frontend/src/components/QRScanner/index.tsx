import { useEffect, useRef } from 'react';
import { Html5QrcodeScanner } from 'html5-qrcode';
import styles from './QRScanner.module.css';

interface QRScannerProps {
  onScan: (result: string) => void;
  onError?: (err: string) => void;
}

export default function QRScanner({ onScan, onError }: QRScannerProps) {
  const ref = useRef<Html5QrcodeScanner | null>(null);

  useEffect(() => {
    const scanner = new Html5QrcodeScanner(
      'qr-scanner-region',
      { fps: 10, qrbox: { width: 250, height: 250 }, rememberLastUsedCamera: true },
      false
    );
    ref.current = scanner;

    scanner.render(
      (text) => { onScan(text); scanner.pause(); },
      (err) => { onError?.(err); }
    );

    return () => {
      scanner.clear().catch(() => {});
    };
  }, [onScan, onError]);

  return (
    <div className={styles.wrap}>
      <div id="qr-scanner-region" className={styles.scanner} />
    </div>
  );
}
