import { useState } from 'react';
import Button from '../../../components/ui/Button';
import { Search } from 'lucide-react';
import type { CompanyInfo } from '../../../api/festival';

export const fmtDate = (iso?: string) => {
  if (!iso) return '';
  const [y, m, d] = iso.split('-');
  return d && m && y ? `${d}.${m}.${y}` : iso;
};

export function PhotoImg({ src }: { src: string }) {
  const [show, setShow] = useState(false);
  const [err, setErr] = useState(false);
  if (err) return null;
  return (
    <img
      src={src}
      onLoad={() => setShow(true)}
      onError={() => setErr(true)}
      style={{ width: '100%', maxHeight: 140, objectFit: 'cover', borderRadius: 8, marginBottom: 8, display: show ? 'block' : 'none' }}
    />
  );
}

export function CompanyCard({ company, action }: { company: CompanyInfo; action?: React.ReactNode }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 16px', background: 'var(--bg-elevated)', borderRadius: 'var(--radius-sm)', marginTop: 8 }}>
      <div>
        <p style={{ fontSize: 14, fontWeight: 600 }}>{company.brand_name || company.full_name}</p>
        <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>ИНН: {company.tin}{company.email ? ` · ${company.email}` : ''}</p>
        {!company.is_active && <p style={{ fontSize: 11, color: 'var(--accent-warning)' }}>Не активирована</p>}
      </div>
      {action}
    </div>
  );
}

export function OrganizerBlock({
  label, tin, onTinChange, found, searching, onSearch, selected, onSelect, onClear,
}: {
  label: string;
  tin: string;
  onTinChange: (v: string) => void;
  found: CompanyInfo | null | 'not-found';
  searching: boolean;
  onSearch: () => void;
  selected: CompanyInfo | null;
  onSelect: (c: CompanyInfo) => void;
  onClear: () => void;
}) {
  return (
    <div style={{ padding: '12px', background: 'var(--bg-page)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
      <p style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-muted)', marginBottom: 8, fontFamily: 'var(--font-heading)' }}>{label}</p>
      {selected ? (
        <CompanyCard
          company={selected}
          action={<Button size="sm" variant="secondary" onClick={onClear}>Изменить</Button>}
        />
      ) : (
        <>
          <div style={{ display: 'flex', gap: 8 }}>
            <input placeholder="ИНН компании" value={tin} onChange={e => onTinChange(e.target.value)} style={{ flex: 1 }} />
            <Button size="sm" disabled={!tin.trim()} loading={searching} onClick={onSearch}>
              <Search size={14} /> Найти
            </Button>
          </div>
          {found && found !== 'not-found' && (
            <CompanyCard
              company={found as CompanyInfo}
              action={<Button size="sm" onClick={() => onSelect(found as CompanyInfo)}>Выбрать</Button>}
            />
          )}
          {found === 'not-found' && (
            <p style={{ fontSize: 12, color: 'var(--accent-danger)', marginTop: 6 }}>Компания не найдена</p>
          )}
        </>
      )}
    </div>
  );
}
