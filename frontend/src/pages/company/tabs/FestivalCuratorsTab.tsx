import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Trash2 } from 'lucide-react';
import Card from '../../../components/ui/Card';
import Button from '../../../components/ui/Button';
import Spinner from '../../../components/ui/Spinner';
import { festivalApi, type FestivalCurator } from '../../../api/festival';
import styles from '../CompanyPages.module.css';

function CuratorRow({ curator, canRemove, onRemove, removing }: {
  curator: FestivalCurator;
  canRemove: boolean;
  onRemove: () => void;
  removing: boolean;
}) {
  const primary = curator.nickname ?? `Куратор #${curator.user_id}`;
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 16px', background: 'var(--bg-elevated)', borderRadius: 'var(--radius-sm)' }}>
      <div>
        <p style={{ fontSize: 14, fontWeight: 600, color: 'var(--text-primary)' }}>{primary}</p>
        {curator.email && (
          <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>{curator.email}</p>
        )}
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

export function FestivalCuratorsTab({ festivalId, isPublished }: Props) {
  const qc = useQueryClient();
  const [emailSearch, setEmailSearch] = useState('');
  const [foundCurator, setFoundCurator] = useState<FestivalCurator | null | 'not-found'>(null);

  const { data: curators = [], isLoading: curatorsLoading } = useQuery({
    queryKey: ['festival-curators', festivalId],
    queryFn: () => festivalApi.getCurators(festivalId),
    enabled: !!festivalId,
  });

  const addCuratorMutation = useMutation({
    mutationFn: (email: string) => festivalApi.addCurator(festivalId, email),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-curators', festivalId] });
      setEmailSearch(''); setFoundCurator(null);
    },
    onError: () => setFoundCurator('not-found'),
  });

  const removeCuratorMutation = useMutation({
    mutationFn: (userId: number) => festivalApi.removeCurator(festivalId, userId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-curators', festivalId] }),
  });

  return (
    <Card padding="lg">
      <h3 className={styles.sectionTitle}>Кураторы</h3>

      {!isPublished && (
        <div style={{ marginBottom: 16 }}>
          <div className={styles.addBar}>
            <input type="email" placeholder="Email пользователя" value={emailSearch}
              onChange={e => { setEmailSearch(e.target.value); setFoundCurator(null); }} style={{ flex: 1 }} />
            <Button size="sm" disabled={!emailSearch.trim()} loading={addCuratorMutation.isPending}
              onClick={() => addCuratorMutation.mutate(emailSearch.trim())}>
              <Plus size={14} /> Добавить
            </Button>
          </div>
          {foundCurator === 'not-found' && (
            <p style={{ fontSize: 13, color: 'var(--accent-danger)', marginTop: 6 }}>
              Пользователь не найден или уже является куратором.
            </p>
          )}
        </div>
      )}

      {curatorsLoading ? <Spinner /> : (
        <div className={styles.list}>
          {curators.length === 0
            ? <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Нет кураторов</p>
            : curators.map(c => (
              <CuratorRow key={c.user_id} curator={c} canRemove={!isPublished}
                onRemove={() => removeCuratorMutation.mutate(c.user_id)}
                removing={removeCuratorMutation.isPending} />
            ))
          }
        </div>
      )}
    </Card>
  );
}
