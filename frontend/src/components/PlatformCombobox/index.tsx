import { useState, useRef, useEffect, useCallback } from 'react';
import { Building2, ChevronDown, Plus } from 'lucide-react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { locationApi, type PlatformInstanceResponse } from '../../api/location';
import CityCombobox from '../CityCombobox';
import Button from '../ui/Button';
import type { City } from '../../types';
import styles from './PlatformCombobox.module.css';

interface PlatformComboboxProps {
  value?: PlatformInstanceResponse | null;
  onChange: (platform: PlatformInstanceResponse | null) => void;
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

function platformLabel(p: PlatformInstanceResponse) {
  return `${p.platform_name}, г. ${p.city.name}`;
}

function platformSub(p: PlatformInstanceResponse) {
  return `${p.street}, ${p.house}`;
}

export default function PlatformCombobox({ value, onChange, error }: PlatformComboboxProps) {
  const [open, setOpen] = useState(false);
  const [inputVal, setInputVal] = useState(value ? platformLabel(value) : '');
  const [highlighted, setHighlighted] = useState(0);
  const [showCreate, setShowCreate] = useState(false);
  const [createName, setCreateName] = useState('');
  const [createCity, setCreateCity] = useState<City | null>(null);
  const [createStreet, setCreateStreet] = useState('');
  const [createHouse, setCreateHouse] = useState('');
  const debouncedQ = useDebounce(inputVal, 300);
  const containerRef = useRef<HTMLDivElement>(null);

  const { data: platforms = [] } = useQuery({
    queryKey: ['platform-instances', debouncedQ],
    queryFn: () => locationApi.searchPlatformInstances(debouncedQ),
    enabled: !value,
  });

  const createMutation = useMutation({
    mutationFn: () => locationApi.createPlatformInstance({
      platformName: createName.trim(),
      cityId: Number(createCity!.id),
      street: createStreet.trim(),
      house: createHouse.trim(),
    }),
    onSuccess: (created) => {
      onChange(created);
      setInputVal(platformLabel(created));
      setShowCreate(false);
      setOpen(false);
    },
  });

  useEffect(() => {
    if (value) setInputVal(platformLabel(value));
  }, [value]);

  const handleSelect = useCallback((p: PlatformInstanceResponse) => {
    onChange(p);
    setInputVal(platformLabel(p));
    setOpen(false);
  }, [onChange]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (!open) { if (e.key === 'ArrowDown') setOpen(true); return; }
    if (e.key === 'ArrowDown') { setHighlighted(h => Math.min(h + 1, platforms.length - 1)); e.preventDefault(); }
    if (e.key === 'ArrowUp')   { setHighlighted(h => Math.max(h - 1, 0)); e.preventDefault(); }
    if (e.key === 'Enter' && platforms[highlighted]) { handleSelect(platforms[highlighted]); }
    if (e.key === 'Escape') setOpen(false);
  };

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const canCreate = createName.trim() && createCity && createStreet.trim() && createHouse.trim();

  return (
    <div ref={containerRef} className={[styles.wrap, error ? styles.hasError : ''].join(' ')}>
      <div className={styles.inputWrap}>
        <Building2 size={16} className={styles.inputIcon} />
        <input
          className={styles.input}
          value={inputVal}
          placeholder="Название площадки или город..."
          onChange={e => {
            setInputVal(e.target.value);
            setOpen(true);
            setHighlighted(0);
            if (!e.target.value) onChange(null);
          }}
          onFocus={() => setOpen(true)}
          onKeyDown={handleKeyDown}
          autoComplete="off"
        />
        <ChevronDown size={16} className={[styles.chevron, open ? styles.chevronOpen : ''].join(' ')} />
      </div>
      {error && <p className={styles.error}>{error}</p>}

      {open && !value && (
        <ul className={styles.dropdown}>
          {platforms.map((p, i) => (
            <li
              key={p.id}
              className={[styles.option, highlighted === i ? styles.optionHighlighted : ''].join(' ')}
              onMouseDown={() => handleSelect(p)}
              onMouseEnter={() => setHighlighted(i)}
            >
              <div>{platformLabel(p)}</div>
              <div className={styles.optionSub}>{platformSub(p)}</div>
            </li>
          ))}
          <li
            className={styles.createOption}
            onMouseDown={() => { setShowCreate(true); setOpen(false); }}
          >
            <Plus size={14} /> Создать площадку
          </li>
        </ul>
      )}

      {showCreate && (
        <div className={styles.createForm}>
          <p className={styles.createFormTitle}>Новая площадка</p>
          <input
            placeholder="Название площадки *"
            value={createName}
            onChange={e => setCreateName(e.target.value)}
          />
          <CityCombobox value={createCity} onChange={setCreateCity} placeholder="Город *" />
          <div className={styles.createFormRow}>
            <input placeholder="Улица *" value={createStreet} onChange={e => setCreateStreet(e.target.value)} />
            <input placeholder="Дом *" value={createHouse} onChange={e => setCreateHouse(e.target.value)} />
          </div>
          <div className={styles.createFormActions}>
            <Button type="button" variant="secondary" size="sm" onClick={() => setShowCreate(false)}>Отмена</Button>
            <Button
              type="button"
              size="sm"
              disabled={!canCreate}
              loading={createMutation.isPending}
              onClick={() => createMutation.mutate()}
            >
              Создать
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
