import { useRef } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Image, Trash2 } from 'lucide-react';
import Card from '../../../components/ui/Card';
import Button from '../../../components/ui/Button';
import { festivalApi } from '../../../api/festival';
import styles from '../CompanyPages.module.css';

interface Props {
  festivalId: string;
}

export function FestivalPhotosTab({ festivalId }: Props) {
  const qc = useQueryClient();
  const photoFileRef = useRef<HTMLInputElement>(null);

  const photoUploadMutation = useMutation({
    mutationFn: (file: File) => festivalApi.uploadPhoto(festivalId, file),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit', festivalId] }),
  });

  const photoDeleteMutation = useMutation({
    mutationFn: () => festivalApi.deletePhoto(festivalId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit', festivalId] }),
  });

  return (
    <Card padding="lg">
      <h3 className={styles.sectionTitle}>Обложка фестиваля</h3>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 12, maxWidth: 480 }}>
        <img
          src={`/api/files/FESTIVAL_LOCATION/${festivalId}`}
          alt="Обложка"
          onError={e => { (e.target as HTMLImageElement).style.display = 'none'; }}
          style={{ width: '100%', aspectRatio: '16/9', objectFit: 'cover', borderRadius: 8, background: 'var(--surface-secondary)' }}
        />
        <div style={{ display: 'flex', gap: 8 }}>
          <Button fullWidth loading={photoUploadMutation.isPending} onClick={() => photoFileRef.current?.click()}>
            <Image size={14} /> Загрузить обложку
          </Button>
          <input ref={photoFileRef} type="file" accept="image/*" style={{ display: 'none' }}
            onChange={e => { const f = e.target.files?.[0]; if (f) photoUploadMutation.mutate(f); }} />
          <Button variant="danger" loading={photoDeleteMutation.isPending} onClick={() => photoDeleteMutation.mutate()}>
            <Trash2 size={14} />
          </Button>
        </div>
      </div>
    </Card>
  );
}
