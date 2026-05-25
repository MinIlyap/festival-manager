import { useState, useRef, useEffect, useCallback } from 'react';
import { ChevronDown, MapPin } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { locationApi } from '../../api/location';
import type { City } from '../../types';
import styles from './CityCombobox.module.css';

interface CityComboboxProps {
  value?: City | null;
  onChange: (city: City | null) => void;
  placeholder?: string;
  error?: string;
}

function useDebounce<T>(value: T, delay: number): T {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const t = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(t);
  }, [value, delay]);
  return debounced;
}

export default function CityCombobox({ value, onChange, placeholder = 'Выберите город', error }: CityComboboxProps) {
  const [open, setOpen] = useState(false);
  const [inputVal, setInputVal] = useState(value?.name ?? '');
  const [highlighted, setHighlighted] = useState(0);
  const debouncedQ = useDebounce(inputVal, 300);
  const containerRef = useRef<HTMLDivElement>(null);

  const trimmedQ = debouncedQ.trim();
  const { data: cities = [] } = useQuery({
    queryKey: ['cities', trimmedQ],
    queryFn: () => locationApi.getCities(trimmedQ),
    enabled: trimmedQ.length >= 2,
  });

  useEffect(() => {
    if (value) setInputVal(value.name);
  }, [value]);

  const handleSelect = useCallback((city: City) => {
    onChange(city);
    setInputVal(city.name);
    setOpen(false);
  }, [onChange]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (!open) { if (e.key === 'ArrowDown') setOpen(true); return; }
    if (e.key === 'ArrowDown') { setHighlighted(h => Math.min(h + 1, cities.length - 1)); e.preventDefault(); }
    if (e.key === 'ArrowUp')   { setHighlighted(h => Math.max(h - 1, 0)); e.preventDefault(); }
    if (e.key === 'Enter' && cities[highlighted]) { handleSelect(cities[highlighted]); }
    if (e.key === 'Escape') setOpen(false);
  };

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  return (
    <div ref={containerRef} className={[styles.wrap, error ? styles.hasError : ''].join(' ')}>
      <div className={styles.inputWrap}>
        <MapPin size={16} className={styles.inputIcon} />
        <input
          className={styles.input}
          value={inputVal}
          placeholder={placeholder}
          onChange={e => { setInputVal(e.target.value); setOpen(true); setHighlighted(0); if (!e.target.value) onChange(null); }}
          onFocus={() => { if (inputVal.length >= 2) setOpen(true); }}
          onKeyDown={handleKeyDown}
          autoComplete="off"
        />
        <ChevronDown size={16} className={[styles.chevron, open ? styles.chevronOpen : ''].join(' ')} />
      </div>
      {error && <p className={styles.error}>{error}</p>}
      {open && cities.length > 0 && (
        <ul className={styles.dropdown}>
          {cities.map((city, i) => (
            <li
              key={city.id}
              className={[styles.option, highlighted === i ? styles.optionHighlighted : ''].join(' ')}
              onMouseDown={() => handleSelect(city)}
              onMouseEnter={() => setHighlighted(i)}
            >
              {city.name}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
