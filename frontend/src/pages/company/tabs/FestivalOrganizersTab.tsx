import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Search, Plus, Trash2 } from 'lucide-react';
import Card from '../../../components/ui/Card';
import Button from '../../../components/ui/Button';
import Spinner from '../../../components/ui/Spinner';
import { festivalApi, type FestivalOrganizer, type CompanyInfo } from '../../../api/festival';
import { CompanyCard } from './shared';
import styles from '../CompanyPages.module.css';

function OrganizerRow({ organizer, canRemove, onRemove, removing }: {
  organizer: FestivalOrganizer;
  canRemove: boolean;
  onRemove: () => void;
  removing: boolean;
}) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 16px', background: 'var(--bg-elevated)', borderRadius: 'var(--radius-sm)' }}>
      <div>
        <p style={{ fontSize: 14, fontWeight: 600 }}>{organizer.brand_name || organizer.full_name || `Компания #${organizer.company_id}`}</p>
        <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>ИНН: {organizer.tin}{organizer.email ? ` · ${organizer.email}` : ''}</p>
      </div>
      {canRemove && (
        <Button variant="danger" size="sm" loading={removing} onClick={onRemove}>
          <Trash2 size={14} />
        </Button>
      )}
    </div>
  );
}

interface Props {
  festivalId: string;
  isPublished: boolean;
}

export function FestivalOrganizersTab({ festivalId, isPublished }: Props) {
  const qc = useQueryClient();
  const [tinSearch, setTinSearch] = useState('');
  const [foundCompany, setFoundCompany] = useState<CompanyInfo | null | 'not-found'>(null);
  const [showCreateCompany, setShowCreateCompany] = useState(false);
  const [newCompanyTin, setNewCompanyTin] = useState('');
  const [newCompanyFullName, setNewCompanyFullName] = useState('');
  const [newCompanyBrand, setNewCompanyBrand] = useState('');
  const [newCompanyEmail, setNewCompanyEmail] = useState('');

  const { data: organizers = [], isLoading: organizersLoading } = useQuery({
    queryKey: ['festival-organizers', festivalId],
    queryFn: () => festivalApi.getOrganizers(festivalId),
    enabled: !!festivalId,
  });

  const searchCompanyMutation = useMutation({
    mutationFn: (tin: string) => festivalApi.searchCompanyByTin(tin),
    onSuccess: (data) => { setFoundCompany(data); setShowCreateCompany(false); },
    onError: () => { setFoundCompany('not-found'); setShowCreateCompany(false); },
  });

  const addOrganizerMutation = useMutation({
    mutationFn: (tin: string) => festivalApi.addOrganizer(festivalId, tin),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-organizers', festivalId] });
      setTinSearch(''); setFoundCompany(null); setShowCreateCompany(false);
    },
  });

  const createCompanyMutation = useMutation({
    mutationFn: () => festivalApi.createCompany({
      tin: newCompanyTin, full_name: newCompanyFullName, brand_name: newCompanyBrand, email: newCompanyEmail,
    }),
    onSuccess: async (company) => {
      await addOrganizerMutation.mutateAsync(company.tin);
      setNewCompanyTin(''); setNewCompanyFullName(''); setNewCompanyBrand(''); setNewCompanyEmail('');
    },
  });

  const removeOrganizerMutation = useMutation({
    mutationFn: (companyId: number) => festivalApi.removeOrganizer(festivalId, companyId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-organizers', festivalId] }),
  });

  return (
    <Card padding="lg">
      <h3 className={styles.sectionTitle}>Компании-организаторы</h3>

      {!isPublished && (
        <div style={{ marginBottom: 16 }}>
          <div className={styles.addBar}>
            <input
              placeholder="ИНН компании"
              value={tinSearch}
              onChange={e => { setTinSearch(e.target.value); setFoundCompany(null); setShowCreateCompany(false); }}
              style={{ flex: 1 }}
            />
            <Button
              size="sm"
              disabled={!tinSearch.trim()}
              loading={searchCompanyMutation.isPending}
              onClick={() => searchCompanyMutation.mutate(tinSearch.trim())}
            >
              <Search size={14} /> Найти
            </Button>
          </div>

          {foundCompany === 'not-found' && (
            <div style={{ marginTop: 8 }}>
              <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 8 }}>Компания не найдена.</p>
              <Button size="sm" variant="secondary" onClick={() => {
                setShowCreateCompany(true); setNewCompanyTin(tinSearch); setFoundCompany(null);
              }}>
                <Plus size={14} /> Создать компанию
              </Button>
            </div>
          )}

          {foundCompany && foundCompany !== 'not-found' && (
            <CompanyCard
              company={foundCompany}
              action={
                <Button size="sm" loading={addOrganizerMutation.isPending}
                  onClick={() => addOrganizerMutation.mutate(foundCompany.tin)}>
                  Добавить
                </Button>
              }
            />
          )}

          {showCreateCompany && (
            <div className={styles.form} style={{ marginTop: 12, padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)' }}>
              <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)' }}>Создать новую компанию</p>
              <div className={styles.row}>
                <input placeholder="ИНН *" value={newCompanyTin} onChange={e => setNewCompanyTin(e.target.value)} />
                <input placeholder="Email *" value={newCompanyEmail} onChange={e => setNewCompanyEmail(e.target.value)} />
              </div>
              <input placeholder="Полное название *" value={newCompanyFullName} onChange={e => setNewCompanyFullName(e.target.value)} />
              <input placeholder="Бренд / торговое название *" value={newCompanyBrand} onChange={e => setNewCompanyBrand(e.target.value)} />
              <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                <Button size="sm" variant="secondary" onClick={() => setShowCreateCompany(false)}>Отмена</Button>
                <Button
                  size="sm"
                  loading={createCompanyMutation.isPending || addOrganizerMutation.isPending}
                  disabled={!newCompanyTin || !newCompanyFullName || !newCompanyBrand || !newCompanyEmail}
                  onClick={() => createCompanyMutation.mutate()}
                >
                  Создать и добавить
                </Button>
              </div>
            </div>
          )}
        </div>
      )}

      {organizersLoading ? <Spinner /> : (
        <div className={styles.list}>
          {organizers.length === 0
            ? <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Нет организаторов</p>
            : organizers.map(org => (
              <OrganizerRow key={org.company_id} organizer={org} canRemove={!isPublished}
                onRemove={() => removeOrganizerMutation.mutate(org.company_id)}
                removing={removeOrganizerMutation.isPending} />
            ))
          }
        </div>
      )}
    </Card>
  );
}
