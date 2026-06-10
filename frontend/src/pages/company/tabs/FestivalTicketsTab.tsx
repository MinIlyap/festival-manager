import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Pencil, Trash2 } from 'lucide-react';
import Card from '../../../components/ui/Card';
import Button from '../../../components/ui/Button';
import { festivalApi } from '../../../api/festival';
import type { Festival } from '../../../types';
import DictCombobox, { type DictItem } from '../../../components/DictCombobox';
import styles from '../CompanyPages.module.css';

interface Props {
  festivalId: string;
  festival: Festival | undefined;
  isPublished: boolean;
}

export function FestivalTicketsTab({ festivalId, festival, isPublished }: Props) {
  const qc = useQueryClient();
  const [selectedTicketType, setSelectedTicketType] = useState<DictItem | null>(null);
  const [ticketPrice, setTicketPrice] = useState('');
  const [ticketDesc, setTicketDesc] = useState('');

  const [editingTicketId, setEditingTicketId] = useState<string | null>(null);
  const [editTicketPrice, setEditTicketPrice] = useState('');
  const [editTicketDesc, setEditTicketDesc] = useState('');

  const { data: ticketTypes = [] } = useQuery({
    queryKey: ['ticket-types'],
    queryFn: festivalApi.getTicketTypes,
  });

  const addTicketMutation = useMutation({
    mutationFn: () => festivalApi.createTicketType({
      ticketTypeId: selectedTicketType!.id,
      festivalLocationId: festivalId,
      description: ticketDesc || undefined,
      price: Number(ticketPrice),
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-edit', festivalId] });
      setSelectedTicketType(null); setTicketPrice(''); setTicketDesc('');
    },
  });

  const updateTicketMutation = useMutation({
    mutationFn: () => festivalApi.updateTicketType(editingTicketId!, {
      price: Number(editTicketPrice),
      description: editTicketDesc || undefined,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-edit', festivalId] });
      setEditingTicketId(null);
    },
  });

  const deleteTicketMutation = useMutation({
    mutationFn: (ticketId: string) => festivalApi.deleteTicketType(ticketId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit', festivalId] }),
  });

  return (
    <Card padding="lg">
      <h3 className={styles.sectionTitle}>Типы билетов</h3>

      {!isPublished && (
        <div className={styles.form} style={{ marginBottom: 16, padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)' }}>
          <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)' }}>Добавить тип билета</p>
          <div className={styles.row}>
            <div className={styles.field}>
              <label>Тип билета *</label>
              <DictCombobox
                items={ticketTypes}
                value={selectedTicketType}
                onChange={setSelectedTicketType}
                onCreateNew={name => festivalApi.createGlobalTicketType(name).then(t => {
                  qc.invalidateQueries({ queryKey: ['ticket-types'] });
                  return t.id;
                })}
                placeholder="Выберите или создайте..."
              />
            </div>
            <div className={styles.field}>
              <label>Цена (₽) *</label>
              <input placeholder="500" value={ticketPrice} onChange={e => setTicketPrice(e.target.value)} type="number" min={0} />
            </div>
          </div>
          <div className={styles.field}>
            <label>Описание *</label>
            <textarea placeholder="Описание типа билета" value={ticketDesc} onChange={e => setTicketDesc(e.target.value)} rows={3} />
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Button size="sm" loading={addTicketMutation.isPending}
              disabled={!selectedTicketType || !ticketPrice || !ticketDesc.trim()}
              onClick={() => addTicketMutation.mutate()}>
              Добавить
            </Button>
          </div>
        </div>
      )}

      <div className={styles.list}>
        {(festival?.ticketTypes ?? []).length === 0
          ? <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Нет типов билетов</p>
          : (festival?.ticketTypes ?? []).map(tt => (
            <div key={tt.id}>
              <div className={styles.listItem}>
                <div>
                  <p className={styles.listItemName}>{tt.name}</p>
                  <p className={styles.listItemSub}>
                    {tt.price.toLocaleString('ru-RU')} ₽{tt.description ? ` · ${tt.description}` : ''}
                  </p>
                </div>
                {!isPublished && (
                  <div style={{ display: 'flex', gap: 6 }}>
                    <Button size="sm" variant="secondary" onClick={() => {
                      if (editingTicketId === tt.id) { setEditingTicketId(null); } else {
                        setEditingTicketId(tt.id); setEditTicketPrice(String(tt.price)); setEditTicketDesc(tt.description ?? '');
                      }
                    }}>
                      <Pencil size={14} />
                    </Button>
                    <Button variant="danger" size="sm" loading={deleteTicketMutation.isPending}
                      onClick={() => deleteTicketMutation.mutate(tt.id)}>
                      <Trash2 size={14} />
                    </Button>
                  </div>
                )}
              </div>
              {editingTicketId === tt.id && !isPublished && (
                <div className={styles.form} style={{ margin: '8px 0 4px', padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Цена (₽) *</label>
                      <input type="number" min={0} value={editTicketPrice} onChange={e => setEditTicketPrice(e.target.value)} />
                    </div>
                    <div className={styles.field}>
                      <label>Описание *</label>
                      <textarea value={editTicketDesc} onChange={e => setEditTicketDesc(e.target.value)} placeholder="Описание типа билета" rows={3} />
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                    <Button size="sm" variant="secondary" onClick={() => setEditingTicketId(null)}>Отмена</Button>
                    <Button size="sm" loading={updateTicketMutation.isPending}
                      disabled={!editTicketPrice || !editTicketDesc.trim()}
                      onClick={() => updateTicketMutation.mutate()}>
                      Сохранить
                    </Button>
                  </div>
                </div>
              )}
            </div>
          ))
        }
      </div>
    </Card>
  );
}
