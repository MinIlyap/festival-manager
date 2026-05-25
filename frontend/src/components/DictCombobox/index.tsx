import { useState, useRef, useEffect, useCallback } from 'react';
import { ChevronDown, Plus } from 'lucide-react';
import Button from '../ui/Button';
import styles from './DictCombobox.module.css';

export interface DictItem {
  id: number;
  name: string;
}

interface DictComboboxProps {
  items: DictItem[];
  value: DictItem | null;
  onChange: (item: DictItem | null) => void;
  onCreateNew?: (name: string) => Promise<number>;
  placeholder?: string;
  error?: string;
}

export default function DictCombobox({
  items,
  value,
  onChange,
  onCreateNew,
  placeholder = 'Выберите...',
  error,
}: DictComboboxProps) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState(value?.name ?? '');
  const [highlighted, setHighlighted] = useState(0);
  const [showCreate, setShowCreate] = useState(false);
  const [createName, setCreateName] = useState('');
  const [creating, setCreating] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const filtered = items.filter(i => i.name.toLowerCase().includes(query.toLowerCase()));

  useEffect(() => { if (value) setQuery(value.name); }, [value]);

  const handleSelect = useCallback((item: DictItem) => {
    onChange(item);
    setQuery(item.name);
    setOpen(false);
    setShowCreate(false);
  }, [onChange]);

  const handleCreate = async () => {
    if (!onCreateNew || !createName.trim()) return;
    setCreating(true);
    try {
      const newId = await onCreateNew(createName.trim());
      const newItem = { id: newId, name: createName.trim() };
      handleSelect(newItem);
      setCreateName('');
      setShowCreate(false);
    } finally {
      setCreating(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (!open) { if (e.key === 'ArrowDown') setOpen(true); return; }
    if (e.key === 'ArrowDown') { setHighlighted(h => Math.min(h + 1, filtered.length - 1)); e.preventDefault(); }
    if (e.key === 'ArrowUp')   { setHighlighted(h => Math.max(h - 1, 0)); e.preventDefault(); }
    if (e.key === 'Enter' && filtered[highlighted]) { handleSelect(filtered[highlighted]); }
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
        <input
          value={query}
          placeholder={placeholder}
          onChange={e => {
            setQuery(e.target.value);
            setOpen(true);
            setHighlighted(0);
            if (!e.target.value) onChange(null);
          }}
          onFocus={() => setOpen(true)}
          onKeyDown={handleKeyDown}
          autoComplete="off"
          style={{ paddingRight: 32 }}
        />
        <ChevronDown size={16} className={[styles.chevron, open ? styles.chevronOpen : ''].join(' ')} />
      </div>
      {error && <p className={styles.error}>{error}</p>}

      {open && (
        <ul className={styles.dropdown}>
          {filtered.map((item, i) => (
            <li
              key={item.id}
              className={[styles.option, highlighted === i ? styles.optionHighlighted : ''].join(' ')}
              onMouseDown={() => handleSelect(item)}
              onMouseEnter={() => setHighlighted(i)}
            >
              {item.name}
            </li>
          ))}
          {onCreateNew && (
            <li className={styles.createOption} onMouseDown={() => { setShowCreate(true); setOpen(false); }}>
              <Plus size={14} /> Создать новый
            </li>
          )}
        </ul>
      )}

      {showCreate && onCreateNew && (
        <div className={styles.createRow}>
          <input
            placeholder="Название *"
            value={createName}
            onChange={e => setCreateName(e.target.value)}
            autoFocus
          />
          <Button type="button" size="sm" disabled={!createName.trim()} loading={creating} onClick={handleCreate}>
            Создать
          </Button>
          <Button type="button" size="sm" variant="secondary" onClick={() => { setShowCreate(false); setCreateName(''); }}>
            ✕
          </Button>
        </div>
      )}
    </div>
  );
}
