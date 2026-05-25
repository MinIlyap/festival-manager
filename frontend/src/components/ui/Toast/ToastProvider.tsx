import Toast from './index';
import { useToastStore } from '../../../store/toastStore';

export default function ToastProvider() {
  const { toasts, remove } = useToastStore();
  return <Toast toasts={toasts} onRemove={remove} />;
}
