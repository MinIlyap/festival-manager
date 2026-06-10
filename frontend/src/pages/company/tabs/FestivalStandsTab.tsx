import { useState, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Pencil, Trash2, Image, X, Plus, Package } from 'lucide-react';
import Card from '../../../components/ui/Card';
import Button from '../../../components/ui/Button';
import { festivalApi, type CompanyInfo } from '../../../api/festival';
import { eventApi, type StandProduct } from '../../../api/event';
import DictCombobox, { type DictItem } from '../../../components/DictCombobox';
import { PhotoImg, OrganizerBlock } from './shared';
import styles from '../CompanyPages.module.css';

interface Props {
  festivalId: string;
  isPublished: boolean;
}

export function FestivalStandsTab({ festivalId, isPublished }: Props) {
  const qc = useQueryClient();

  const { data: festivalStands = [] } = useQuery({
    queryKey: ['festival-edit-stands', festivalId],
    queryFn: () => eventApi.getStandsByFestival(festivalId),
    enabled: !!festivalId,
  });

  const { data: dictStands = [] } = useQuery({
    queryKey: ['dict-stands'],
    queryFn: eventApi.getDictionaryStands,
  });

  const [selectedStandDict, setSelectedStandDict] = useState<DictItem | null>(null);
  const [newStandType, setNewStandType] = useState<'COMMERCIAL' | 'DEMONSTRATION'>('COMMERCIAL');
  const [newStandDesc, setNewStandDesc] = useState('');
  const [newStandDate, setNewStandDate] = useState('');

  const [newStandOrgTin, setNewStandOrgTin] = useState('');
  const [newStandOrgFound, setNewStandOrgFound] = useState<CompanyInfo | null | 'not-found'>(null);
  const [newStandOrgSelected, setNewStandOrgSelected] = useState<CompanyInfo | null>(null);

  const searchNewStandOrgMutation = useMutation({
    mutationFn: (tin: string) => festivalApi.searchCompanyByTin(tin),
    onSuccess: (data) => setNewStandOrgFound(data),
    onError: () => setNewStandOrgFound('not-found'),
  });

  const addStandMutation = useMutation({
    mutationFn: () => eventApi.createStand({
      festivalId: festivalId,
      standId: selectedStandDict!.id,
      type: newStandType,
      description: newStandDesc || undefined,
      startDate: newStandDate,
      organizerCompanyId: newStandOrgSelected?.id ?? undefined,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-edit-stands', festivalId] });
      qc.invalidateQueries({ queryKey: ['dict-stands'] });
      setSelectedStandDict(null); setNewStandType('COMMERCIAL'); setNewStandDesc(''); setNewStandDate('');
      setNewStandOrgTin(''); setNewStandOrgFound(null); setNewStandOrgSelected(null);
    },
  });

  const deleteStandMutation = useMutation({
    mutationFn: (standId: string) => eventApi.deleteStand(standId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit-stands', festivalId] }),
  });

  const [editingStandId, setEditingStandId] = useState<string | null>(null);
  const [editStandType, setEditStandType] = useState<'COMMERCIAL' | 'DEMONSTRATION'>('COMMERCIAL');
  const [editStandDesc, setEditStandDesc] = useState('');
  const [editStandDate, setEditStandDate] = useState('');

  const [editStandOrgTin, setEditStandOrgTin] = useState('');
  const [editStandOrgFound, setEditStandOrgFound] = useState<CompanyInfo | null | 'not-found'>(null);
  const [editStandOrgSelected, setEditStandOrgSelected] = useState<CompanyInfo | null>(null);

  const searchEditStandOrgMutation = useMutation({
    mutationFn: (tin: string) => festivalApi.searchCompanyByTin(tin),
    onSuccess: (data) => setEditStandOrgFound(data),
    onError: () => setEditStandOrgFound('not-found'),
  });

  const openEditStand = async (st: { id: string; type: string; description?: string; startDate?: string; organizerCompanyId?: number }) => {
    setEditingStandId(st.id);
    setEditStandType(st.type as 'COMMERCIAL' | 'DEMONSTRATION');
    setEditStandDesc(st.description ?? '');
    setEditStandDate(st.startDate ?? '');
    setEditStandOrgTin('');
    setEditStandOrgFound(null);
    if (st.organizerCompanyId) {
      try {
        const company = await festivalApi.getCompanyById(st.organizerCompanyId);
        setEditStandOrgSelected(company);
      } catch {
        setEditStandOrgSelected({ id: st.organizerCompanyId, tin: '', full_name: `Компания #${st.organizerCompanyId}`, brand_name: '', email: '', is_active: true });
      }
    } else {
      setEditStandOrgSelected(null);
    }
  };

  const updateStandMutation = useMutation({
    mutationFn: () => eventApi.updateStand(editingStandId!, {
      type: editStandType,
      description: editStandDesc || undefined,
      startDate: editStandDate,
      organizerCompanyId: editStandOrgSelected?.id ?? undefined,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-edit-stands', festivalId] });
      setEditingStandId(null);
    },
  });

  const [productStandId, setProductStandId] = useState<string | null>(null);
  const [selectedProductDict, setSelectedProductDict] = useState<DictItem | null>(null);
  const [productPrice, setProductPrice] = useState('');
  const [productQuantity, setProductQuantity] = useState('');
  const [productDesc, setProductDesc] = useState('');

  const { data: dictProducts = [] } = useQuery({
    queryKey: ['dict-products'],
    queryFn: eventApi.getDictionaryProducts,
    enabled: !!productStandId,
  });

  const { data: standProducts = [], refetch: refetchStandProducts } = useQuery({
    queryKey: ['stand-products', productStandId],
    queryFn: () => eventApi.getProductsByStand(productStandId!),
    enabled: !!productStandId,
  });

  const [editingProductId, setEditingProductId] = useState<number | null>(null);
  const [editProductDictItem, setEditProductDictItem] = useState<DictItem | null>(null);
  const [editProductPrice, setEditProductPrice] = useState('');
  const [editProductQuantity, setEditProductQuantity] = useState('');
  const [editProductDesc, setEditProductDesc] = useState('');

  const addProductMutation = useMutation({
    mutationFn: () => eventApi.addProduct(productStandId!, {
      productId: selectedProductDict!.id,
      description: productDesc || undefined,
      price: Number(productPrice),
      quantity: Number(productQuantity),
    }),
    onSuccess: () => {
      setSelectedProductDict(null); setProductPrice(''); setProductQuantity(''); setProductDesc('');
      refetchStandProducts();
    },
  });

  const deleteProductMutation = useMutation({
    mutationFn: (productInstanceId: number) => eventApi.deleteProduct(productStandId!, productInstanceId),
    onSuccess: () => refetchStandProducts(),
  });

  const updateProductMutation = useMutation({
    mutationFn: () => eventApi.updateProduct(productStandId!, editingProductId!, {
      productId: editProductDictItem!.id,
      description: editProductDesc || undefined,
      price: Number(editProductPrice),
      quantity: Number(editProductQuantity),
    }),
    onSuccess: () => {
      setEditingProductId(null);
      refetchStandProducts();
    },
  });

  const [photoStandRowId, setPhotoStandRowId] = useState<string | null>(null);
  const [photoKey, setPhotoKey] = useState(0);
  const standPhotoFileRef = useRef<HTMLInputElement>(null);

  const uploadStandPhotoMutation = useMutation({
    mutationFn: (file: File) => eventApi.uploadStandPhoto(photoStandRowId!, file),
    onSuccess: () => setPhotoKey(k => k + 1),
  });

  const deleteStandPhotoMutation = useMutation({
    mutationFn: () => eventApi.deleteStandPhoto(photoStandRowId!),
    onSuccess: () => setPhotoKey(k => k + 1),
  });

  const [photoProductId, setPhotoProductId] = useState<number | null>(null);
  const [productPhotoKey, setProductPhotoKey] = useState(0);
  const productPhotoFileRef = useRef<HTMLInputElement>(null);

  const uploadProductPhotoMutation = useMutation({
    mutationFn: (file: File) => eventApi.uploadProductPhoto(photoProductId!, file),
    onSuccess: () => setProductPhotoKey(k => k + 1),
  });

  const deleteProductPhotoMutation = useMutation({
    mutationFn: () => eventApi.deleteProductPhoto(photoProductId!),
    onSuccess: () => setProductPhotoKey(k => k + 1),
  });

  return (
    <Card padding="lg">
      <h3 className={styles.sectionTitle}>Стенды</h3>

      {!isPublished && (
        <div className={styles.form} style={{ marginBottom: 24, padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)' }}>
          <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)' }}>Добавить стенд</p>

          <div className={styles.row}>
            <div className={styles.field}>
              <label>Название стенда *</label>
              <DictCombobox
                items={dictStands}
                value={selectedStandDict}
                onChange={setSelectedStandDict}
                onCreateNew={name => eventApi.createDictionaryStand(name).then(newId => {
                  qc.invalidateQueries({ queryKey: ['dict-stands'] });
                  return newId;
                })}
                placeholder="Выберите или создайте..."
              />
            </div>
            <div className={styles.field}>
              <label>Тип</label>
              <select value={newStandType} onChange={e => setNewStandType(e.target.value as 'COMMERCIAL' | 'DEMONSTRATION')}>
                <option value="COMMERCIAL">Коммерческий</option>
                <option value="DEMONSTRATION">Демонстрационный</option>
              </select>
            </div>
          </div>

          <div className={styles.row}>
            <div className={styles.field}>
              <label>Дата участия *</label>
              <input type="date" value={newStandDate} onChange={e => setNewStandDate(e.target.value)} />
            </div>
            <div className={styles.field}>
              <label>Описание *</label>
              <textarea value={newStandDesc} onChange={e => setNewStandDesc(e.target.value)} placeholder="Описание стенда" rows={3} />
            </div>
          </div>

          <OrganizerBlock
            label="Компания-организатор стенда"
            tin={newStandOrgTin} onTinChange={tin => { setNewStandOrgTin(tin); setNewStandOrgFound(null); }}
            found={newStandOrgFound} searching={searchNewStandOrgMutation.isPending}
            onSearch={() => searchNewStandOrgMutation.mutate(newStandOrgTin.trim())}
            selected={newStandOrgSelected}
            onSelect={c => { setNewStandOrgSelected(c); setNewStandOrgFound(null); }}
            onClear={() => { setNewStandOrgSelected(null); setNewStandOrgFound(null); setNewStandOrgTin(''); }}
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Button size="sm" loading={addStandMutation.isPending}
              disabled={!selectedStandDict || !newStandDate || !newStandDesc.trim()}
              onClick={() => addStandMutation.mutate()}>
              Добавить
            </Button>
          </div>
        </div>
      )}

      <div className={styles.list}>
        {festivalStands.length === 0
          ? <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Нет стендов</p>
          : festivalStands.map(st => (
            <div key={st.id}>
              <div className={styles.listItem}>
                <div style={{ flex: 1 }}>
                  <p className={styles.listItemName}>{st.name}</p>
                  <p className={styles.listItemSub}>{st.type === 'COMMERCIAL' ? 'Коммерческий' : 'Демонстрационный'}{st.description ? ` · ${st.description}` : ''}</p>
                </div>
                <div style={{ display: 'flex', gap: 6 }}>
                  {st.type === 'COMMERCIAL' && (
                    <Button size="sm" variant="secondary"
                      onClick={() => { setProductStandId(productStandId === st.id ? null : st.id); setSelectedProductDict(null); setProductPrice(''); setProductQuantity(''); setProductDesc(''); }}>
                      <Package size={14} /> Товары
                    </Button>
                  )}
                  <Button size="sm" variant="secondary"
                    onClick={() => setPhotoStandRowId(photoStandRowId === st.id ? null : st.id)}>
                    <Image size={14} />
                  </Button>
                  {!isPublished && (
                    <>
                      <Button size="sm" variant="secondary"
                        onClick={() => editingStandId === st.id ? setEditingStandId(null) : openEditStand(st)}>
                        <Pencil size={14} />
                      </Button>
                      <Button variant="danger" size="sm" loading={deleteStandMutation.isPending}
                        onClick={() => deleteStandMutation.mutate(st.id)}>
                        <Trash2 size={14} />
                      </Button>
                    </>
                  )}
                </div>
              </div>

              {editingStandId === st.id && !isPublished && (
                <div className={styles.form} style={{ margin: '8px 0 4px', padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
                  <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)', marginBottom: 8 }}>Редактировать стенд</p>
                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Тип</label>
                      <select value={editStandType} onChange={e => setEditStandType(e.target.value as 'COMMERCIAL' | 'DEMONSTRATION')}>
                        <option value="COMMERCIAL">Коммерческий</option>
                        <option value="DEMONSTRATION">Демонстрационный</option>
                      </select>
                    </div>
                    <div className={styles.field}>
                      <label>Дата участия *</label>
                      <input type="date" value={editStandDate} onChange={e => setEditStandDate(e.target.value)} />
                    </div>
                  </div>
                  <div className={styles.field}>
                    <label>Описание *</label>
                    <textarea value={editStandDesc} onChange={e => setEditStandDesc(e.target.value)} placeholder="Описание стенда" rows={3} />
                  </div>

                  <OrganizerBlock
                    label="Компания-организатор стенда"
                    tin={editStandOrgTin} onTinChange={tin => { setEditStandOrgTin(tin); setEditStandOrgFound(null); }}
                    found={editStandOrgFound} searching={searchEditStandOrgMutation.isPending}
                    onSearch={() => searchEditStandOrgMutation.mutate(editStandOrgTin.trim())}
                    selected={editStandOrgSelected}
                    onSelect={c => { setEditStandOrgSelected(c); setEditStandOrgFound(null); }}
                    onClear={() => { setEditStandOrgSelected(null); setEditStandOrgFound(null); setEditStandOrgTin(''); }}
                  />

                  <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                    <Button size="sm" variant="secondary" onClick={() => setEditingStandId(null)}>Отмена</Button>
                    <Button size="sm" loading={updateStandMutation.isPending}
                      disabled={!editStandDate || !editStandDesc.trim()}
                      onClick={() => updateStandMutation.mutate()}>
                      Сохранить
                    </Button>
                  </div>
                </div>
              )}

              {productStandId === st.id && (
                <div className={styles.form} style={{ margin: '8px 0 4px', padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                    <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)' }}>
                      <Package size={14} style={{ display: 'inline', marginRight: 6 }} />
                      Товары стенда
                    </p>
                    <button style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }} onClick={() => { setProductStandId(null); setEditingProductId(null); }}>
                      <X size={16} />
                    </button>
                  </div>

                  {standProducts.length > 0 && (
                    <div style={{ marginBottom: 16 }}>
                      <p style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-muted)', marginBottom: 8, fontFamily: 'var(--font-heading)' }}>Добавленные товары</p>
                      {standProducts.map((sp: StandProduct) => (
                        <div key={sp.productInstanceId}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '8px 0', borderBottom: '1px solid var(--border-subtle)' }}>
                            <div style={{ flex: 1 }}>
                              <p style={{ fontSize: 13, fontWeight: 600 }}>{sp.productName}</p>
                              <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                                {sp.price.toLocaleString('ru-RU')} ₽ · {sp.quantity} шт.
                                {sp.description ? ` · ${sp.description}` : ''}
                              </p>
                            </div>
                            <Button size="sm" variant="secondary"
                              onClick={() => setPhotoProductId(photoProductId === sp.productInstanceId ? null : sp.productInstanceId)}>
                              <Image size={13} />
                            </Button>
                            <Button size="sm" variant="secondary" onClick={() => {
                              if (editingProductId === sp.productInstanceId) {
                                setEditingProductId(null);
                              } else {
                                setEditingProductId(sp.productInstanceId);
                                setEditProductDictItem({ id: sp.productId, name: sp.productName });
                                setEditProductPrice(String(sp.price));
                                setEditProductQuantity(String(sp.quantity));
                                setEditProductDesc(sp.description ?? '');
                              }
                            }}>
                              <Pencil size={13} />
                            </Button>
                            <Button size="sm" variant="danger" loading={deleteProductMutation.isPending}
                              onClick={() => deleteProductMutation.mutate(sp.productInstanceId)}>
                              <Trash2 size={13} />
                            </Button>
                          </div>
                          {photoProductId === sp.productInstanceId && (
                            <div style={{ padding: '8px 0 10px', borderBottom: '1px solid var(--border-subtle)' }}>
                              <PhotoImg
                                key={productPhotoKey}
                                src={`/api/files/PRODUCT_INSTANCE/${sp.productInstanceId}?k=${productPhotoKey}`}
                              />
                              <div style={{ display: 'flex', gap: 8 }}>
                                <Button size="sm" loading={uploadProductPhotoMutation.isPending} onClick={() => productPhotoFileRef.current?.click()}>
                                  <Image size={13} /> Загрузить фото
                                </Button>
                                <input ref={productPhotoFileRef} type="file" accept="image/*" style={{ display: 'none' }}
                                  onChange={e => { const f = e.target.files?.[0]; if (f) { uploadProductPhotoMutation.mutate(f); e.target.value = ''; } }} />
                                <Button size="sm" variant="danger" loading={deleteProductPhotoMutation.isPending} onClick={() => deleteProductPhotoMutation.mutate()}>
                                  <Trash2 size={13} />
                                </Button>
                              </div>
                            </div>
                          )}
                          {editingProductId === sp.productInstanceId && (
                            <div className={styles.form} style={{ padding: '12px 0 4px' }}>
                              <div className={styles.row}>
                                <div className={styles.field}>
                                  <label>Товар *</label>
                                  <DictCombobox
                                    items={dictProducts}
                                    value={editProductDictItem}
                                    onChange={setEditProductDictItem}
                                    onCreateNew={name => eventApi.createDictionaryProduct(name).then(newId => {
                                      qc.invalidateQueries({ queryKey: ['dict-products'] });
                                      return newId;
                                    })}
                                    placeholder="Выберите или создайте..."
                                  />
                                </div>
                              </div>
                              <div className={styles.row}>
                                <div className={styles.field}>
                                  <label>Цена (₽) *</label>
                                  <input type="number" min={0} value={editProductPrice} onChange={e => setEditProductPrice(e.target.value)} />
                                </div>
                                <div className={styles.field}>
                                  <label>Количество *</label>
                                  <input type="number" min={1} value={editProductQuantity} onChange={e => setEditProductQuantity(e.target.value)} />
                                </div>
                              </div>
                              <div className={styles.field}>
                                <label>Описание *</label>
                                <textarea value={editProductDesc} onChange={e => setEditProductDesc(e.target.value)} placeholder="Описание товара" rows={3} />
                              </div>
                              <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                                <Button size="sm" variant="secondary" onClick={() => setEditingProductId(null)}>Отмена</Button>
                                <Button size="sm" loading={updateProductMutation.isPending}
                                  disabled={!editProductDictItem || !editProductPrice || !editProductQuantity || !editProductDesc.trim()}
                                  onClick={() => updateProductMutation.mutate()}>
                                  Сохранить
                                </Button>
                              </div>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}

                  <p style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-muted)', marginBottom: 8, fontFamily: 'var(--font-heading)' }}>Добавить товар</p>
                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Товар *</label>
                      <DictCombobox
                        items={dictProducts}
                        value={selectedProductDict}
                        onChange={setSelectedProductDict}
                        onCreateNew={name => eventApi.createDictionaryProduct(name).then(newId => {
                          qc.invalidateQueries({ queryKey: ['dict-products'] });
                          return newId;
                        })}
                        placeholder="Выберите или создайте..."
                      />
                    </div>
                  </div>
                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Цена (₽) *</label>
                      <input type="number" min={0} value={productPrice} onChange={e => setProductPrice(e.target.value)} placeholder="500" />
                    </div>
                    <div className={styles.field}>
                      <label>Количество *</label>
                      <input type="number" min={1} value={productQuantity} onChange={e => setProductQuantity(e.target.value)} placeholder="10" />
                    </div>
                  </div>
                  <div className={styles.field}>
                    <label>Описание *</label>
                    <textarea value={productDesc} onChange={e => setProductDesc(e.target.value)} placeholder="Описание товара" rows={3} />
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                    <Button size="sm" loading={addProductMutation.isPending}
                      disabled={!selectedProductDict || !productPrice || !productQuantity || !productDesc.trim()}
                      onClick={() => addProductMutation.mutate()}>
                      <Plus size={13} /> Добавить
                    </Button>
                  </div>
                </div>
              )}

              {photoStandRowId === st.id && (
                <div className={styles.form} style={{ margin: '8px 0 4px', padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                    <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)' }}>
                      <Image size={14} style={{ display: 'inline', marginRight: 6 }} />
                      Фотография стенда
                    </p>
                    <button style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }} onClick={() => setPhotoStandRowId(null)}>
                      <X size={16} />
                    </button>
                  </div>
                  <img
                    key={photoKey}
                    src={`/api/files/FESTIVAL_STAND/${st.id}?k=${photoKey}`}
                    alt="Фото стенда"
                    onError={e => { (e.target as HTMLImageElement).style.display = 'none'; }}
                    onLoad={e => { (e.target as HTMLImageElement).style.display = 'block'; }}
                    style={{ width: '100%', maxHeight: 200, objectFit: 'cover', borderRadius: 8, background: 'var(--surface-secondary)', display: 'none' }}
                  />
                  <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                    <Button loading={uploadStandPhotoMutation.isPending} onClick={() => standPhotoFileRef.current?.click()}>
                      <Image size={14} /> Загрузить фото
                    </Button>
                    <input ref={standPhotoFileRef} type="file" accept="image/*" style={{ display: 'none' }}
                      onChange={e => { const f = e.target.files?.[0]; if (f) { uploadStandPhotoMutation.mutate(f); e.target.value = ''; } }} />
                    <Button variant="danger" loading={deleteStandPhotoMutation.isPending} onClick={() => deleteStandPhotoMutation.mutate()}>
                      <Trash2 size={14} />
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
