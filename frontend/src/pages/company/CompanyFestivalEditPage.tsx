import { useState, useRef, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Info, Building2, Users, CalendarDays, Store, Ticket, Image, Send, Trash2, Search, Plus, Pencil, Trophy, X, Package } from 'lucide-react';
import Layout from '../../components/layout/Layout';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import { festivalApi, type FestivalOrganizer, type FestivalCurator, type CompanyInfo } from '../../api/festival';
import { eventApi, type EventReward, type StandProduct } from '../../api/event';
import DictCombobox, { type DictItem } from '../../components/DictCombobox';
import styles from './CompanyPages.module.css';

function PhotoImg({ src }: { src: string }) {
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

const TABS = [
  { key: 'info',      label: 'Общая информация',      icon: Info },
  { key: 'companies', label: 'Компании-организаторы',  icon: Building2 },
  { key: 'curators',  label: 'Кураторы',               icon: Users },
  { key: 'events',    label: 'Мероприятия',            icon: CalendarDays },
  { key: 'stands',    label: 'Стенды',                 icon: Store },
  { key: 'tickets',   label: 'Типы билетов',           icon: Ticket },
  { key: 'photos',    label: 'Фотографии',             icon: Image },
];

const fmtDate = (iso?: string) => {
  if (!iso) return '';
  const [y, m, d] = iso.split('-');
  return d && m && y ? `${d}.${m}.${y}` : iso;
};

const ORDINALS_RU = [
  'Первое', 'Второе', 'Третье', 'Четвёртое', 'Пятое',
  'Шестое', 'Седьмое', 'Восьмое', 'Девятое', 'Десятое',
  'Одиннадцатое', 'Двенадцатое', 'Тринадцатое', 'Четырнадцатое', 'Пятнадцатое',
  'Шестнадцатое', 'Семнадцатое', 'Восемнадцатое', 'Девятнадцатое', 'Двадцатое',
];
const ordinalRu = (n: number) =>
  ORDINALS_RU[n - 1] ? `${ORDINALS_RU[n - 1]} место` : `Место ${n}`;

const EVENT_TYPES = [
  { value: 'TOURNAMENT',    label: 'Турнир' },
  { value: 'QUEST',         label: 'Квест' },
  { value: 'MASTER_CLASS',  label: 'Мастер-класс' },
  { value: 'DEMONSTRATION', label: 'Демонстрация' },
];

export default function CompanyFestivalEditPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [activeTab, setActiveTab] = useState('info');
  const [submitModal, setSubmitModal] = useState(false);
  const photoFileRef = useRef<HTMLInputElement>(null);

  // ── Festival data ───────────────────────────────────────────────────────────
  const { data: festival, isLoading } = useQuery({
    queryKey: ['festival-edit', id],
    queryFn: () => festivalApi.getById(id!),
    enabled: !!id,
  });

  const isPublished = festival?.status === 'APPROVED';

  // ── General info edit ────────────────────────────────────────────────────────
  const [infoName, setInfoName] = useState('');
  const [infoDesc, setInfoDesc] = useState('');
  const [infoDateStart, setInfoDateStart] = useState('');
  const [infoTimeStart, setInfoTimeStart] = useState('');
  const [infoDateEnd, setInfoDateEnd] = useState('');
  const [infoTimeEnd, setInfoTimeEnd] = useState('');
  const [infoMaxPeople, setInfoMaxPeople] = useState('');
  const [infoAgeLimitId, setInfoAgeLimitId] = useState<number | ''>('');

  useEffect(() => {
    if (festival) {
      setInfoName(festival.name ?? '');
      setInfoDesc(festival.description ?? '');
      setInfoDateStart(festival.startDate ?? '');
      setInfoTimeStart(festival.timeStart ? festival.timeStart.substring(0, 5) : '');
      setInfoDateEnd(festival.endDate ?? '');
      setInfoTimeEnd(festival.timeEnd ? festival.timeEnd.substring(0, 5) : '');
      setInfoMaxPeople(festival.maxPeople != null ? String(festival.maxPeople) : '');
      setInfoAgeLimitId(festival.ageLimitId ?? '');
    }
  }, [festival]);

  const { data: ageLimits = [] } = useQuery({
    queryKey: ['age-limits'],
    queryFn: festivalApi.getAgeLimits,
  });

  const updateInfoMutation = useMutation({
    mutationFn: () => festivalApi.update(id!, {
      name: infoName,
      description: infoDesc || undefined,
      dateStart: infoDateStart,
      timeStart: infoTimeStart,
      dateEnd: infoDateEnd,
      timeEnd: infoTimeEnd,
      maxPeople: Number(infoMaxPeople),
      ageLimitId: Number(infoAgeLimitId),
    }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit', id] }),
  });

  const submitMutation = useMutation({
    mutationFn: () => festivalApi.submitForModeration(id!),
    onSuccess: () => { setSubmitModal(false); navigate('/company/applications'); },
  });

  // ── Organizers ──────────────────────────────────────────────────────────────
  const { data: organizers = [], isLoading: organizersLoading } = useQuery({
    queryKey: ['festival-organizers', id],
    queryFn: () => festivalApi.getOrganizers(id!),
    enabled: !!id && activeTab === 'companies',
  });

  const [tinSearch, setTinSearch] = useState('');
  const [foundCompany, setFoundCompany] = useState<CompanyInfo | null | 'not-found'>(null);
  const [showCreateCompany, setShowCreateCompany] = useState(false);
  const [newCompanyTin, setNewCompanyTin] = useState('');
  const [newCompanyFullName, setNewCompanyFullName] = useState('');
  const [newCompanyBrand, setNewCompanyBrand] = useState('');
  const [newCompanyEmail, setNewCompanyEmail] = useState('');

  const searchCompanyMutation = useMutation({
    mutationFn: (tin: string) => festivalApi.searchCompanyByTin(tin),
    onSuccess: (data) => { setFoundCompany(data); setShowCreateCompany(false); },
    onError: () => { setFoundCompany('not-found'); setShowCreateCompany(false); },
  });

  const addOrganizerMutation = useMutation({
    mutationFn: (tin: string) => festivalApi.addOrganizer(id!, tin),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-organizers', id] });
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
    mutationFn: (companyId: number) => festivalApi.removeOrganizer(id!, companyId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-organizers', id] }),
  });

  // ── Curators ────────────────────────────────────────────────────────────────
  const { data: curators = [], isLoading: curatorsLoading } = useQuery({
    queryKey: ['festival-curators', id],
    queryFn: () => festivalApi.getCurators(id!),
    enabled: !!id && activeTab === 'curators',
  });

  const [emailSearch, setEmailSearch] = useState('');
  const [foundCurator, setFoundCurator] = useState<FestivalCurator | null | 'not-found'>(null);

  const addCuratorMutation = useMutation({
    mutationFn: (email: string) => festivalApi.addCurator(id!, email),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-curators', id] });
      setEmailSearch(''); setFoundCurator(null);
    },
    onError: () => setFoundCurator('not-found'),
  });

  const removeCuratorMutation = useMutation({
    mutationFn: (userId: number) => festivalApi.removeCurator(id!, userId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-curators', id] }),
  });

  // ── Events ──────────────────────────────────────────────────────────────────
  const { data: festivalEvents = [] } = useQuery({
    queryKey: ['festival-edit-events', id],
    queryFn: () => eventApi.getEventsByFestival(id!),
    enabled: !!id && activeTab === 'events',
  });

  const { data: dictEvents = [] } = useQuery({
    queryKey: ['dict-events'],
    queryFn: eventApi.getDictionaryEvents,
    enabled: activeTab === 'events',
  });

  // ── Add-event form state ─────────────────────────────────────────────────
  const [selectedEventDict, setSelectedEventDict] = useState<DictItem | null>(null);
  const [newEventType, setNewEventType] = useState('');
  const [newEventDesc, setNewEventDesc] = useState('');
  const [newEventDate, setNewEventDate] = useState('');
  const [newEventTime, setNewEventTime] = useState('');
  const [newEventDuration, setNewEventDuration] = useState('');
  const [newEventFormat, setNewEventFormat] = useState<'SOLO' | 'TEAM'>('SOLO');
  const [newEventMax, setNewEventMax] = useState('');
  const [newEventTeamSize, setNewEventTeamSize] = useState('');

  // ── Edit-event inline state ───────────────────────────────────────────────
  const [editingEventId, setEditingEventId] = useState<string | null>(null);
  const [editEventType, setEditEventType] = useState('');
  const [editEventDesc, setEditEventDesc] = useState('');
  const [editEventDate, setEditEventDate] = useState('');
  const [editEventTime, setEditEventTime] = useState('');
  const [editEventDuration, setEditEventDuration] = useState('');
  const [editEventFormat, setEditEventFormat] = useState<'SOLO' | 'TEAM'>('SOLO');
  const [editEventMax, setEditEventMax] = useState('');
  const [editEventTeamSize, setEditEventTeamSize] = useState('');

  // ── Organizer company for new event ─────────────────────────────────────────
  const [newEventOrgTin, setNewEventOrgTin] = useState('');
  const [newEventOrgFound, setNewEventOrgFound] = useState<CompanyInfo | null | 'not-found'>(null);
  const [newEventOrgSelected, setNewEventOrgSelected] = useState<CompanyInfo | null>(null);

  const searchNewEventOrgMutation = useMutation({
    mutationFn: (tin: string) => festivalApi.searchCompanyByTin(tin),
    onSuccess: (data) => setNewEventOrgFound(data),
    onError: () => setNewEventOrgFound('not-found'),
  });

  // ── Organizer company for editing event ──────────────────────────────────────
  const [editEventOrgTin, setEditEventOrgTin] = useState('');
  const [editEventOrgFound, setEditEventOrgFound] = useState<CompanyInfo | null | 'not-found'>(null);
  const [editEventOrgSelected, setEditEventOrgSelected] = useState<CompanyInfo | null>(null);

  const searchEditEventOrgMutation = useMutation({
    mutationFn: (tin: string) => festivalApi.searchCompanyByTin(tin),
    onSuccess: (data) => setEditEventOrgFound(data),
    onError: () => setEditEventOrgFound('not-found'),
  });

  const openEditEvent = async (ev: { id: string; type: string; format: string; startDate: string; startTime?: string; approximateDuration?: number; description?: string; maxParticipants?: number; teamSize?: number; organizerCompanyId?: number }) => {
    setEditingEventId(ev.id);
    setEditEventType(ev.type);
    setEditEventDesc(ev.description ?? '');
    setEditEventDate(ev.startDate ?? '');
    setEditEventTime(ev.startTime ? ev.startTime.substring(0, 5) : '');
    setEditEventDuration(ev.approximateDuration ? String(ev.approximateDuration) : '');
    setEditEventFormat(ev.format === 'TEAM' ? 'TEAM' : 'SOLO');
    setEditEventMax(String(ev.maxParticipants ?? ''));
    setEditEventTeamSize(String(ev.teamSize ?? ''));
    setEditEventOrgTin('');
    setEditEventOrgFound(null);
    if (ev.organizerCompanyId) {
      try {
        const company = await festivalApi.getCompanyById(ev.organizerCompanyId);
        setEditEventOrgSelected(company);
      } catch {
        setEditEventOrgSelected({ id: ev.organizerCompanyId, tin: '', full_name: `Компания #${ev.organizerCompanyId}`, brand_name: '', email: '', is_active: true });
      }
    } else {
      setEditEventOrgSelected(null);
    }
    setRewardModalEventId(null);
  };

  // ── Rewards modal state ───────────────────────────────────────────────────
  const [rewardModalEventId, setRewardModalEventId] = useState<string | null>(null);
  const [prizePlacesCount, setPrizePlacesCount] = useState(0);
  const [placeRewards, setPlaceRewards] = useState<Record<number, DictItem | null>>({});
  const [placeDescriptions, setPlaceDescriptions] = useState<Record<number, string>>({});

  const { data: dictRewards = [] } = useQuery({
    queryKey: ['dict-rewards'],
    queryFn: eventApi.getDictionaryRewards,
    enabled: !!rewardModalEventId,
  });

  const { data: existingRewards = [], refetch: refetchRewards } = useQuery({
    queryKey: ['event-rewards', rewardModalEventId],
    queryFn: () => eventApi.getRewardsByEvent(rewardModalEventId!),
    enabled: !!rewardModalEventId,
  });

  const openRewardModal = (eventId: string) => {
    setRewardModalEventId(eventId);
    setEditingEventId(null);
    setPrizePlacesCount(0);
    setPlaceRewards({});
    setPlaceDescriptions({});
  };

  const addRewardMutation = useMutation({
    mutationFn: ({ place, reward, description }: { place: number; reward: DictItem; description?: string }) =>
      eventApi.addRewardToEvent(rewardModalEventId!, { rewardId: reward.id, place, description }),
    onSuccess: (_, { place }) => {
      setPlaceRewards(p => ({ ...p, [place]: null }));
      setPlaceDescriptions(d => { const next = { ...d }; delete next[place]; return next; });
      refetchRewards();
    },
  });

  const deleteRewardMutation = useMutation({
    mutationFn: (eventRewardId: string) => eventApi.deleteReward(eventRewardId),
    onSuccess: () => refetchRewards(),
  });

  const addEventMutation = useMutation({
    mutationFn: () => eventApi.createEvent({
      festivalId: id!,
      eventId: selectedEventDict!.id,
      type: newEventType,
      description: newEventDesc || undefined,
      format: newEventFormat,
      maxParticipants: Number(newEventMax) || 0,
      teamSize: newEventFormat === 'TEAM' ? Number(newEventTeamSize) : undefined,
      startDate: newEventDate,
      startTime: newEventTime || '00:00',
      approximateDuration: newEventDuration ? Number(newEventDuration) : undefined,
      organizerCompanyId: newEventOrgSelected?.id ?? undefined,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-edit-events', id] });
      qc.invalidateQueries({ queryKey: ['dict-events'] });
      setSelectedEventDict(null); setNewEventType(''); setNewEventDesc('');
      setNewEventDate(''); setNewEventTime(''); setNewEventDuration(''); setNewEventMax(''); setNewEventTeamSize('');
      setNewEventOrgTin(''); setNewEventOrgFound(null); setNewEventOrgSelected(null);
    },
  });

  const updateEventMutation = useMutation({
    mutationFn: () => eventApi.updateEvent(editingEventId!, {
      type: editEventType,
      description: editEventDesc || undefined,
      format: editEventFormat,
      maxParticipants: Number(editEventMax) || 0,
      teamSize: editEventFormat === 'TEAM' ? Number(editEventTeamSize) : undefined,
      startDate: editEventDate,
      startTime: editEventTime || '00:00',
      approximateDuration: editEventDuration ? Number(editEventDuration) : undefined,
      organizerCompanyId: editEventOrgSelected?.id ?? undefined,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-edit-events', id] });
      setEditingEventId(null);
    },
  });

  const deleteEventMutation = useMutation({
    mutationFn: (eventId: string) => eventApi.deleteEvent(eventId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit-events', id] }),
  });

  // ── Stands ──────────────────────────────────────────────────────────────────
  const { data: festivalStands = [] } = useQuery({
    queryKey: ['festival-edit-stands', id],
    queryFn: () => eventApi.getStandsByFestival(id!),
    enabled: !!id && activeTab === 'stands',
  });

  const { data: dictStands = [] } = useQuery({
    queryKey: ['dict-stands'],
    queryFn: eventApi.getDictionaryStands,
    enabled: activeTab === 'stands',
  });

  const [selectedStandDict, setSelectedStandDict] = useState<DictItem | null>(null);
  const [newStandType, setNewStandType] = useState<'COMMERCIAL' | 'DEMONSTRATION'>('COMMERCIAL');
  const [newStandDesc, setNewStandDesc] = useState('');
  const [newStandDate, setNewStandDate] = useState('');

  const addStandMutation = useMutation({
    mutationFn: () => eventApi.createStand({
      festivalId: id!,
      standId: selectedStandDict!.id,
      type: newStandType,
      description: newStandDesc || undefined,
      startDate: newStandDate,
      organizerCompanyId: newStandOrgSelected?.id ?? undefined,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-edit-stands', id] });
      qc.invalidateQueries({ queryKey: ['dict-stands'] });
      setSelectedStandDict(null); setNewStandType('COMMERCIAL'); setNewStandDesc(''); setNewStandDate('');
      setNewStandOrgTin(''); setNewStandOrgFound(null); setNewStandOrgSelected(null);
    },
  });

  const deleteStandMutation = useMutation({
    mutationFn: (standId: string) => eventApi.deleteStand(standId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit-stands', id] }),
  });

  // ── Stand inline edit ─────────────────────────────────────────────────────
  const [editingStandId, setEditingStandId] = useState<string | null>(null);
  const [editStandType, setEditStandType] = useState<'COMMERCIAL' | 'DEMONSTRATION'>('COMMERCIAL');
  const [editStandDesc, setEditStandDesc] = useState('');
  const [editStandDate, setEditStandDate] = useState('');

  // ── Organizer company for new stand ──────────────────────────────────────────
  const [newStandOrgTin, setNewStandOrgTin] = useState('');
  const [newStandOrgFound, setNewStandOrgFound] = useState<CompanyInfo | null | 'not-found'>(null);
  const [newStandOrgSelected, setNewStandOrgSelected] = useState<CompanyInfo | null>(null);

  const searchNewStandOrgMutation = useMutation({
    mutationFn: (tin: string) => festivalApi.searchCompanyByTin(tin),
    onSuccess: (data) => setNewStandOrgFound(data),
    onError: () => setNewStandOrgFound('not-found'),
  });

  // ── Organizer company for editing stand ───────────────────────────────────────
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
      qc.invalidateQueries({ queryKey: ['festival-edit-stands', id] });
      setEditingStandId(null);
    },
  });

  // ── Ticket Types ─────────────────────────────────────────────────────────────
  const { data: ticketTypes = [] } = useQuery({
    queryKey: ['ticket-types'],
    queryFn: festivalApi.getTicketTypes,
    enabled: activeTab === 'tickets',
  });

  const [selectedTicketType, setSelectedTicketType] = useState<DictItem | null>(null);
  const [ticketPrice, setTicketPrice] = useState('');
  const [ticketDesc, setTicketDesc] = useState('');

  // ── Ticket Type inline edit ──────────────────────────────────────────────────
  const [editingTicketId, setEditingTicketId] = useState<string | null>(null);
  const [editTicketPrice, setEditTicketPrice] = useState('');
  const [editTicketDesc, setEditTicketDesc] = useState('');

  const updateTicketMutation = useMutation({
    mutationFn: () => festivalApi.updateTicketType(editingTicketId!, {
      price: Number(editTicketPrice),
      description: editTicketDesc || undefined,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-edit', id] });
      setEditingTicketId(null);
    },
  });

  const addTicketMutation = useMutation({
    mutationFn: () => festivalApi.createTicketType({
      ticketTypeId: selectedTicketType!.id,
      festivalLocationId: id,
      description: ticketDesc || undefined,
      price: Number(ticketPrice),
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['festival-edit', id] });
      setSelectedTicketType(null); setTicketPrice(''); setTicketDesc('');
    },
  });

  const deleteTicketMutation = useMutation({
    mutationFn: (ticketId: string) => festivalApi.deleteTicketType(ticketId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit', id] }),
  });

  // ── Products for commercial stands ───────────────────────────────────────────
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

  // ── Event & Stand photo management ─────────────────────────────────────────
  const [photoEventId, setPhotoEventId] = useState<string | null>(null);
  const [photoStandRowId, setPhotoStandRowId] = useState<string | null>(null);
  const [photoKey, setPhotoKey] = useState(0);
  const eventPhotoFileRef = useRef<HTMLInputElement>(null);
  const standPhotoFileRef = useRef<HTMLInputElement>(null);

  const uploadEventPhotoMutation = useMutation({
    mutationFn: (file: File) => eventApi.uploadEventPhoto(photoEventId!, file),
    onSuccess: () => setPhotoKey(k => k + 1),
  });

  const deleteEventPhotoMutation = useMutation({
    mutationFn: () => eventApi.deleteEventPhoto(photoEventId!),
    onSuccess: () => setPhotoKey(k => k + 1),
  });

  const uploadStandPhotoMutation = useMutation({
    mutationFn: (file: File) => eventApi.uploadStandPhoto(photoStandRowId!, file),
    onSuccess: () => setPhotoKey(k => k + 1),
  });

  const deleteStandPhotoMutation = useMutation({
    mutationFn: () => eventApi.deleteStandPhoto(photoStandRowId!),
    onSuccess: () => setPhotoKey(k => k + 1),
  });

  // ── Reward photo management ─────────────────────────────────────────────────
  const [photoRewardId, setPhotoRewardId] = useState<string | null>(null);
  const [rewardPhotoKey, setRewardPhotoKey] = useState(0);
  const rewardPhotoFileRef = useRef<HTMLInputElement>(null);

  const uploadRewardPhotoMutation = useMutation({
    mutationFn: (file: File) => eventApi.uploadRewardPhoto(photoRewardId!, file),
    onSuccess: () => setRewardPhotoKey(k => k + 1),
  });

  const deleteRewardPhotoMutation = useMutation({
    mutationFn: () => eventApi.deleteRewardPhoto(photoRewardId!),
    onSuccess: () => setRewardPhotoKey(k => k + 1),
  });

  // ── Product photo management ────────────────────────────────────────────────
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

  // ── Festival Photos ──────────────────────────────────────────────────────────
  const photoUploadMutation = useMutation({
    mutationFn: (file: File) => festivalApi.uploadPhoto(id!, file),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit', id] }),
  });

  const photoDeleteMutation = useMutation({
    mutationFn: () => festivalApi.deletePhoto(id!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['festival-edit', id] }),
  });

  if (isLoading) return <Layout><Spinner centered /></Layout>;

  return (
    <Layout>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.title}>{festival?.name}</h1>
          <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
            {fmtDate(festival?.startDate)} — {fmtDate(festival?.endDate)}
          </p>
        </div>
        {!isPublished && (
          <Button onClick={() => setSubmitModal(true)}>
            <Send size={16} /> Отправить заявку
          </Button>
        )}
      </div>

      <div className={styles.editLayout}>
        {/* Sidebar */}
        <div className={styles.sidebar}>
          {TABS.map(t => {
            const Icon = t.icon;
            return (
              <button
                key={t.key}
                className={[styles.sidebarItem, activeTab === t.key ? styles.sidebarItemActive : ''].join(' ')}
                onClick={() => setActiveTab(t.key)}
              >
                <Icon size={16} /> {t.label}
              </button>
            );
          })}
        </div>

        {/* Tab content */}
        <div className={styles.tabContent}>

          {/* ── General Info ── */}
          {activeTab === 'info' && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}>Информация о фестивале</h3>
              {isPublished ? (
                <div className={styles.form}>
                  <InfoRow label="Название" value={festival?.name} />
                  <InfoRow label="Описание" value={festival?.description} />
                  <InfoRow label="Статус" value="Опубликован" />
                  <InfoRow label="Дата начала" value={fmtDate(festival?.startDate)} />
                  <InfoRow label="Дата окончания" value={fmtDate(festival?.endDate)} />
                  <InfoRow label="Возрастное ограничение" value={festival?.ageRating != null ? `${festival.ageRating}+` : undefined} />
                  <InfoRow label="Макс. посетителей" value={festival?.maxPeople != null ? String(festival.maxPeople) : undefined} />
                </div>
              ) : (
                <div className={styles.form}>
                  <p style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 4 }}>
                    Статус: <strong>Черновик / На модерации</strong>
                  </p>

                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Название *</label>
                      <input value={infoName} onChange={e => setInfoName(e.target.value)} placeholder="Название фестиваля" />
                    </div>
                  </div>

                  <div className={styles.field}>
                    <label>Описание</label>
                    <input value={infoDesc} onChange={e => setInfoDesc(e.target.value)} placeholder="Описание фестиваля" />
                  </div>

                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Дата начала *</label>
                      <input type="date" value={infoDateStart} onChange={e => setInfoDateStart(e.target.value)} />
                    </div>
                    <div className={styles.field}>
                      <label>Время начала *</label>
                      <input type="time" value={infoTimeStart} onChange={e => setInfoTimeStart(e.target.value)} />
                    </div>
                  </div>

                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Дата окончания *</label>
                      <input type="date" value={infoDateEnd} onChange={e => setInfoDateEnd(e.target.value)} />
                    </div>
                    <div className={styles.field}>
                      <label>Время окончания *</label>
                      <input type="time" value={infoTimeEnd} onChange={e => setInfoTimeEnd(e.target.value)} />
                    </div>
                  </div>

                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Макс. посетителей *</label>
                      <input type="number" min={1} value={infoMaxPeople} onChange={e => setInfoMaxPeople(e.target.value)} placeholder="1000" />
                    </div>
                    <div className={styles.field}>
                      <label>Возрастное ограничение *</label>
                      <select value={infoAgeLimitId} onChange={e => setInfoAgeLimitId(Number(e.target.value))}>
                        <option value="">— Выберите —</option>
                        {ageLimits.map(al => (
                          <option key={al.id} value={al.id}>{al.ageLimitValue}</option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 8 }}>
                    <Button
                      loading={updateInfoMutation.isPending}
                      disabled={!infoName.trim() || !infoDateStart || !infoTimeStart || !infoDateEnd || !infoTimeEnd || !infoMaxPeople || !infoAgeLimitId}
                      onClick={() => updateInfoMutation.mutate()}
                    >
                      Сохранить
                    </Button>
                  </div>
                </div>
              )}
            </Card>
          )}

          {/* ── Companies ── */}
          {activeTab === 'companies' && (
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
          )}

          {/* ── Curators ── */}
          {activeTab === 'curators' && (
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
          )}

          {/* ── Events ── */}
          {activeTab === 'events' && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}>Мероприятия</h3>

              {/* Add-event form */}
              {!isPublished && (
                <div className={styles.form} style={{ marginBottom: 24, padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)' }}>
                  <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)' }}>Добавить мероприятие</p>

                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Название мероприятия *</label>
                      <DictCombobox
                        items={dictEvents}
                        value={selectedEventDict}
                        onChange={setSelectedEventDict}
                        onCreateNew={name => eventApi.createDictionaryEvent(name).then(newId => {
                          qc.invalidateQueries({ queryKey: ['dict-events'] });
                          return newId;
                        })}
                        placeholder="Выберите или создайте..."
                      />
                    </div>
                    <div className={styles.field}>
                      <label>Тип *</label>
                      <select value={newEventType} onChange={e => setNewEventType(e.target.value)}>
                        <option value="">— Тип —</option>
                        {EVENT_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
                      </select>
                    </div>
                  </div>

                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Формат *</label>
                      <select value={newEventFormat} onChange={e => setNewEventFormat(e.target.value as 'SOLO' | 'TEAM')}>
                        <option value="SOLO">Одиночное</option>
                        <option value="TEAM">Командное</option>
                      </select>
                    </div>
                    <div className={styles.field}>
                      <label>Макс. участников *</label>
                      <input type="number" min={1} value={newEventMax} onChange={e => setNewEventMax(e.target.value)} placeholder="100" />
                    </div>
                  </div>

                  {newEventFormat === 'TEAM' && (
                    <div className={styles.field}>
                      <label>Размер команды</label>
                      <input type="number" min={2} value={newEventTeamSize} onChange={e => setNewEventTeamSize(e.target.value)} placeholder="4" />
                    </div>
                  )}

                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Дата *</label>
                      <input type="date" value={newEventDate} onChange={e => setNewEventDate(e.target.value)} />
                    </div>
                    <div className={styles.field}>
                      <label>Время начала *</label>
                      <input type="time" value={newEventTime} onChange={e => setNewEventTime(e.target.value)} />
                    </div>
                  </div>

                  <div className={styles.field}>
                    <label>Описание *</label>
                    <input value={newEventDesc} onChange={e => setNewEventDesc(e.target.value)} placeholder="Описание мероприятия" />
                  </div>

                  <div className={styles.row}>
                    <div className={styles.field}>
                      <label>Длительность (мин.)</label>
                      <input type="number" min={1} value={newEventDuration} onChange={e => setNewEventDuration(e.target.value)} placeholder="60" />
                    </div>
                  </div>

                  {/* Organizer company search */}
                  <OrganizerBlock
                    label="Компания-организатор мероприятия"
                    tin={newEventOrgTin} onTinChange={tin => { setNewEventOrgTin(tin); setNewEventOrgFound(null); }}
                    found={newEventOrgFound} searching={searchNewEventOrgMutation.isPending}
                    onSearch={() => searchNewEventOrgMutation.mutate(newEventOrgTin.trim())}
                    selected={newEventOrgSelected}
                    onSelect={c => { setNewEventOrgSelected(c); setNewEventOrgFound(null); }}
                    onClear={() => { setNewEventOrgSelected(null); setNewEventOrgFound(null); setNewEventOrgTin(''); }}
                  />

                  <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                    <Button size="sm" loading={addEventMutation.isPending}
                      disabled={!selectedEventDict || !newEventType || !newEventDate || !newEventMax || !newEventDesc.trim() || !newEventTime}
                      onClick={() => addEventMutation.mutate()}>
                      Добавить
                    </Button>
                  </div>
                </div>
              )}

              {/* Events list */}
              <div className={styles.list}>
                {festivalEvents.length === 0
                  ? <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>Нет мероприятий</p>
                  : festivalEvents.map(ev => (
                    <div key={ev.id}>
                      {/* Row */}
                      <div className={styles.listItem}>
                        <div style={{ flex: 1 }}>
                          <p className={styles.listItemName}>{ev.name}</p>
                          <p className={styles.listItemSub}>
                            {EVENT_TYPES.find(t => t.value === ev.type)?.label ?? ev.type}
                            {' · '}{ev.format === 'TEAM' ? 'Командное' : 'Одиночное'}
                            {ev.startDate ? ` · ${fmtDate(ev.startDate)}` : ''}
                          </p>
                        </div>
                        <div style={{ display: 'flex', gap: 6 }}>
                          <Button size="sm" variant="secondary"
                            onClick={() => rewardModalEventId === ev.id ? setRewardModalEventId(null) : openRewardModal(ev.id)}>
                            <Trophy size={14} /> Награды
                          </Button>
                          <Button size="sm" variant="secondary"
                            onClick={() => { setPhotoEventId(photoEventId === ev.id ? null : ev.id); setRewardModalEventId(null); }}>
                            <Image size={14} />
                          </Button>
                          {!isPublished && (
                            <>
                              <Button size="sm" variant="secondary"
                                onClick={() => editingEventId === ev.id ? setEditingEventId(null) : openEditEvent(ev)}>
                                <Pencil size={14} />
                              </Button>
                              <Button variant="danger" size="sm" loading={deleteEventMutation.isPending}
                                onClick={() => deleteEventMutation.mutate(ev.id)}>
                                <Trash2 size={14} />
                              </Button>
                            </>
                          )}
                        </div>
                      </div>

                      {/* Inline edit form */}
                      {editingEventId === ev.id && !isPublished && (
                        <div className={styles.form} style={{ margin: '8px 0 4px', padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
                          <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)', marginBottom: 8 }}>Редактировать мероприятие</p>
                          <div className={styles.row}>
                            <div className={styles.field}>
                              <label>Тип *</label>
                              <select value={editEventType} onChange={e => setEditEventType(e.target.value)}>
                                <option value="">— Тип —</option>
                                {EVENT_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
                              </select>
                            </div>
                            <div className={styles.field}>
                              <label>Формат *</label>
                              <select value={editEventFormat} onChange={e => setEditEventFormat(e.target.value as 'SOLO' | 'TEAM')}>
                                <option value="SOLO">Одиночное</option>
                                <option value="TEAM">Командное</option>
                              </select>
                            </div>
                          </div>
                          <div className={styles.row}>
                            <div className={styles.field}>
                              <label>Макс. участников *</label>
                              <input type="number" min={1} value={editEventMax} onChange={e => setEditEventMax(e.target.value)} />
                            </div>
                            {editEventFormat === 'TEAM' && (
                              <div className={styles.field}>
                                <label>Размер команды</label>
                                <input type="number" min={2} value={editEventTeamSize} onChange={e => setEditEventTeamSize(e.target.value)} />
                              </div>
                            )}
                          </div>
                          <div className={styles.row}>
                            <div className={styles.field}>
                              <label>Дата *</label>
                              <input type="date" value={editEventDate} onChange={e => setEditEventDate(e.target.value)} />
                            </div>
                            <div className={styles.field}>
                              <label>Время начала *</label>
                              <input type="time" value={editEventTime} onChange={e => setEditEventTime(e.target.value)} />
                            </div>
                          </div>
                          <div className={styles.field}>
                            <label>Описание *</label>
                            <input value={editEventDesc} onChange={e => setEditEventDesc(e.target.value)} placeholder="Описание мероприятия" />
                          </div>
                          <div className={styles.field}>
                            <label>Длительность (мин.)</label>
                            <input type="number" min={1} value={editEventDuration} onChange={e => setEditEventDuration(e.target.value)} placeholder="60" />
                          </div>

                          {/* Organizer company search */}
                          <OrganizerBlock
                            label="Компания-организатор мероприятия"
                            tin={editEventOrgTin} onTinChange={tin => { setEditEventOrgTin(tin); setEditEventOrgFound(null); }}
                            found={editEventOrgFound} searching={searchEditEventOrgMutation.isPending}
                            onSearch={() => searchEditEventOrgMutation.mutate(editEventOrgTin.trim())}
                            selected={editEventOrgSelected}
                            onSelect={c => { setEditEventOrgSelected(c); setEditEventOrgFound(null); }}
                            onClear={() => { setEditEventOrgSelected(null); setEditEventOrgFound(null); setEditEventOrgTin(''); }}
                          />

                          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                            <Button size="sm" variant="secondary" onClick={() => setEditingEventId(null)}>Отмена</Button>
                            <Button size="sm" loading={updateEventMutation.isPending}
                              disabled={!editEventType || !editEventDate || !editEventMax || !editEventDesc.trim() || !editEventTime}
                              onClick={() => updateEventMutation.mutate()}>
                              Сохранить
                            </Button>
                          </div>
                        </div>
                      )}

                      {/* Rewards panel */}
                      {rewardModalEventId === ev.id && (
                        <div style={{ margin: '8px 0 4px', padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                            <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)' }}>
                              <Trophy size={14} style={{ display: 'inline', marginRight: 6 }} />
                              Награды мероприятия
                            </p>
                            <button style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }} onClick={() => setRewardModalEventId(null)}>
                              <X size={16} />
                            </button>
                          </div>

                          {/* Prize-places input */}
                          <div className={styles.field} style={{ marginBottom: 12 }}>
                            <label>Количество призовых мест</label>
                            <input
                              type="number" min={0} max={20}
                              value={prizePlacesCount}
                              onChange={e => {
                                const n = Math.max(0, Math.min(20, Number(e.target.value)));
                                setPrizePlacesCount(n);
                                setPlaceRewards(prev => {
                                  const next: Record<number, DictItem | null> = {};
                                  for (let i = 1; i <= n; i++) next[i] = prev[i] ?? null;
                                  return next;
                                });
                              }}
                              style={{ width: 100 }}
                            />
                          </div>

                          {/* Prize-places table */}
                          {prizePlacesCount > 0 && (
                            <div style={{ marginBottom: 16 }}>
                              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
                                <thead>
                                  <tr style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                                    <th style={{ textAlign: 'left', padding: '6px 8px', width: 70, color: 'var(--text-muted)', fontWeight: 600 }}>Место</th>
                                    <th style={{ textAlign: 'left', padding: '6px 8px', color: 'var(--text-muted)', fontWeight: 600 }}>Награда</th>
                                    <th style={{ textAlign: 'left', padding: '6px 8px', color: 'var(--text-muted)', fontWeight: 600 }}>Описание</th>
                                    <th style={{ width: 52 }} />
                                  </tr>
                                </thead>
                                <tbody>
                                  {Array.from({ length: prizePlacesCount }, (_, i) => i + 1).map(place => (
                                    <tr key={place} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                                      <td style={{ padding: '8px', fontWeight: 600 }}>
                                        {place === 1 ? '🥇 1' : place === 2 ? '🥈 2' : place === 3 ? '🥉 3' : `${place}`}
                                      </td>
                                      <td style={{ padding: '8px' }}>
                                        <DictCombobox
                                          items={dictRewards}
                                          value={placeRewards[place] ?? null}
                                          onChange={item => setPlaceRewards(p => ({ ...p, [place]: item }))}
                                          onCreateNew={name => eventApi.createDictionaryReward(name).then(newId => {
                                            qc.invalidateQueries({ queryKey: ['dict-rewards'] });
                                            return newId;
                                          })}
                                          placeholder="Выберите или создайте..."
                                        />
                                      </td>
                                      <td style={{ padding: '8px' }}>
                                        <input
                                          placeholder="Описание *"
                                          value={placeDescriptions[place] ?? ''}
                                          onChange={e => setPlaceDescriptions(d => ({ ...d, [place]: e.target.value }))}
                                          style={{ width: '100%' }}
                                        />
                                      </td>
                                      <td style={{ padding: '8px' }}>
                                        <Button size="sm"
                                          disabled={!placeRewards[place] || !placeDescriptions[place]?.trim()}
                                          loading={addRewardMutation.isPending}
                                          onClick={() => placeRewards[place] && addRewardMutation.mutate({
                                            place,
                                            reward: placeRewards[place]!,
                                            description: placeDescriptions[place] || undefined,
                                          })}>
                                          <Plus size={13} />
                                        </Button>
                                      </td>
                                    </tr>
                                  ))}
                                </tbody>
                              </table>
                            </div>
                          )}

                          {/* Existing rewards list */}
                          {existingRewards.length > 0 && (
                            <div>
                              <p style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-muted)', marginBottom: 8, fontFamily: 'var(--font-heading)' }}>Добавленные награды</p>
                              {existingRewards
                                .slice()
                                .sort((a: EventReward, b: EventReward) => (a.place ?? 99) - (b.place ?? 99))
                                .map((rew: EventReward) => (
                                  <div key={rew.eventRewardId} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '8px 0' }}>
                                      <div style={{ flex: 1 }}>
                                        <span style={{ fontWeight: 600, marginRight: 8 }}>
                                          {rew.place != null ? `${ordinalRu(rew.place)} · ` : ''}
                                        </span>
                                        <span>{rew.rewardName}</span>
                                        {rew.description && <span style={{ color: 'var(--text-muted)', fontSize: 12, marginLeft: 6 }}>— {rew.description}</span>}
                                      </div>
                                      <Button size="sm" variant="secondary"
                                        onClick={() => setPhotoRewardId(photoRewardId === String(rew.eventRewardId) ? null : String(rew.eventRewardId))}>
                                        <Image size={13} />
                                      </Button>
                                      <Button size="sm" variant="danger"
                                        loading={deleteRewardMutation.isPending}
                                        onClick={() => deleteRewardMutation.mutate(String(rew.eventRewardId))}>
                                        <Trash2 size={13} />
                                      </Button>
                                    </div>
                                    {photoRewardId === String(rew.eventRewardId) && (
                                      <div style={{ paddingBottom: 10 }}>
                                        <PhotoImg
                                          key={rewardPhotoKey}
                                          src={`/api/files/EVENT_REWARD/${rew.eventRewardId}?k=${rewardPhotoKey}`}
                                        />
                                        <div style={{ display: 'flex', gap: 8 }}>
                                          <Button size="sm" loading={uploadRewardPhotoMutation.isPending} onClick={() => rewardPhotoFileRef.current?.click()}>
                                            <Image size={13} /> Загрузить фото
                                          </Button>
                                          <input ref={rewardPhotoFileRef} type="file" accept="image/*" style={{ display: 'none' }}
                                            onChange={e => { const f = e.target.files?.[0]; if (f) { uploadRewardPhotoMutation.mutate(f); e.target.value = ''; } }} />
                                          <Button size="sm" variant="danger" loading={deleteRewardPhotoMutation.isPending} onClick={() => deleteRewardPhotoMutation.mutate()}>
                                            <Trash2 size={13} />
                                          </Button>
                                        </div>
                                      </div>
                                    )}
                                  </div>
                                ))}
                            </div>
                          )}
                          {existingRewards.length === 0 && prizePlacesCount === 0 && (
                            <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>Нет наград. Укажите количество призовых мест выше.</p>
                          )}
                        </div>
                      )}

                      {/* Event photo panel */}
                      {photoEventId === ev.id && (
                        <div style={{ margin: '8px 0 4px', padding: 16, background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                            <p style={{ fontSize: 13, fontWeight: 700, fontFamily: 'var(--font-heading)' }}>
                              <Image size={14} style={{ display: 'inline', marginRight: 6 }} />
                              Фотография мероприятия
                            </p>
                            <button style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }} onClick={() => setPhotoEventId(null)}>
                              <X size={16} />
                            </button>
                          </div>
                          <img
                            key={photoKey}
                            src={`/api/files/FESTIVAL_EVENT/${ev.id}?k=${photoKey}`}
                            alt="Фото мероприятия"
                            onError={e => { (e.target as HTMLImageElement).style.display = 'none'; }}
                            onLoad={e => { (e.target as HTMLImageElement).style.display = 'block'; }}
                            style={{ width: '100%', maxHeight: 200, objectFit: 'cover', borderRadius: 8, background: 'var(--surface-secondary)', display: 'none' }}
                          />
                          <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                            <Button loading={uploadEventPhotoMutation.isPending} onClick={() => eventPhotoFileRef.current?.click()}>
                              <Image size={14} /> Загрузить фото
                            </Button>
                            <input ref={eventPhotoFileRef} type="file" accept="image/*" style={{ display: 'none' }}
                              onChange={e => { const f = e.target.files?.[0]; if (f) { uploadEventPhotoMutation.mutate(f); e.target.value = ''; } }} />
                            <Button variant="danger" loading={deleteEventPhotoMutation.isPending} onClick={() => deleteEventPhotoMutation.mutate()}>
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
          )}

          {/* ── Stands ── */}
          {activeTab === 'stands' && (
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
                      <input value={newStandDesc} onChange={e => setNewStandDesc(e.target.value)} placeholder="Описание стенда" />
                    </div>
                  </div>

                  {/* Organizer company search */}
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
                            <input value={editStandDesc} onChange={e => setEditStandDesc(e.target.value)} placeholder="Описание стенда" />
                          </div>

                          {/* Organizer company search */}
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

                      {/* Products panel for COMMERCIAL stands */}
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

                          {/* Existing products */}
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
                                        <input value={editProductDesc} onChange={e => setEditProductDesc(e.target.value)} placeholder="Описание товара" />
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

                          {/* Add new product */}
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
                            <input value={productDesc} onChange={e => setProductDesc(e.target.value)} placeholder="Описание товара" />
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

                      {/* Stand photo panel */}
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
          )}

          {/* ── Ticket Types ── */}
          {activeTab === 'tickets' && (
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
                    <input placeholder="Описание типа билета" value={ticketDesc} onChange={e => setTicketDesc(e.target.value)} />
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
                              <input value={editTicketDesc} onChange={e => setEditTicketDesc(e.target.value)} placeholder="Описание типа билета" />
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
          )}

          {/* ── Photos ── */}
          {activeTab === 'photos' && (
            <Card padding="lg">
              <h3 className={styles.sectionTitle}>Обложка фестиваля</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12, maxWidth: 480 }}>
                <img
                  src={`/api/files/FESTIVAL_LOCATION/${id}`}
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
          )}
        </div>
      </div>

      <Modal open={submitModal} onClose={() => setSubmitModal(false)} title="Отправить заявку" size="sm">
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <p>Отправить заявку на рассмотрение? После отправки редактирование будет недоступно до решения администратора.</p>
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <Button variant="secondary" onClick={() => setSubmitModal(false)}>Отмена</Button>
            <Button loading={submitMutation.isPending} onClick={() => submitMutation.mutate()}>Отправить</Button>
          </div>
        </div>
      </Modal>
    </Layout>
  );
}

// ── Sub-components ─────────────────────────────────────────────────────────────

function InfoRow({ label, value }: { label: string; value?: string }) {
  if (!value) return null;
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      <span style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-muted)', fontFamily: 'var(--font-heading)' }}>{label}</span>
      <span style={{ fontSize: 14 }}>{value}</span>
    </div>
  );
}

function CompanyCard({ company, action }: { company: CompanyInfo; action?: React.ReactNode }) {
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

function OrganizerBlock({
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
          action={
            <Button size="sm" variant="secondary" onClick={onClear}>Изменить</Button>
          }
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
