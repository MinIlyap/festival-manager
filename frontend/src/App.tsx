import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useEffect } from 'react';
import ProtectedRoute from './router/ProtectedRoute';
import { useNotificationStore } from './store/notificationStore';
import { notificationApi } from './api/notification';
import { useAuthStore } from './store/authStore';

// Auth
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import YandexCallbackPage from './pages/auth/YandexCallbackPage';

// Public
import HomePage from './pages/home/HomePage';
import FestivalPage from './pages/festival/FestivalPage';
import EventDetailPage from './pages/event/EventDetailPage';
import StandPage from './pages/stand/StandPage';

// Shared authenticated
import NotificationsPage from './pages/notifications/NotificationsPage';

// USER
import ProfilePage from './pages/profile/ProfilePage';
import TicketsPage from './pages/tickets/TicketsPage';
import HistoryPage from './pages/history/HistoryPage';

// COMPANY
import CompanyProfilePage from './pages/company/CompanyProfilePage';
import CompanyFestivalsPage from './pages/company/CompanyFestivalsPage';
import CompanyFestivalNewPage from './pages/company/CompanyFestivalNewPage';
import CompanyFestivalEditPage from './pages/company/CompanyFestivalEditPage';
import CompanyApplicationsPage from './pages/company/CompanyApplicationsPage';
import CompanyEventsPage from './pages/company/CompanyEventsPage';
import CompanyAwardDistributionPage from './pages/company/CompanyAwardDistributionPage';
import CompanyFestivalAnalyticsPage from './pages/company/CompanyFestivalAnalyticsPage';

// ADMIN
import AdminProfilePage from './pages/admin/AdminProfilePage';
import AdminApplicationsPage from './pages/admin/AdminApplicationsPage';
import AdminApplicationDetailPage from './pages/admin/AdminApplicationDetailPage';
import AdminHistoryPage from './pages/admin/AdminHistoryPage';

// CURATOR
import CuratorProfilePage from './pages/curator/CuratorProfilePage';
import CuratorFestivalsPage from './pages/curator/CuratorFestivalsPage';
import CuratorHistoryPage from './pages/curator/CuratorHistoryPage';
import CuratorTicketsPage from './pages/curator/CuratorTicketsPage';
import CuratorScannerPage from './pages/curator/CuratorScannerPage';
import CuratorBlacklistPage from './pages/curator/CuratorBlacklistPage';

function NotificationPoller() {
  const { accessToken } = useAuthStore();
  const { setUnreadCount } = useNotificationStore();

  useEffect(() => {
    if (!accessToken) return;
    const poll = () => notificationApi.unreadCount().then(count => setUnreadCount(Number(count))).catch(() => {});
    poll();
    const id = setInterval(poll, 30_000);
    return () => clearInterval(id);
  }, [accessToken]);

  return null;
}

function RootRedirect() {
  const { accessToken } = useAuthStore();
  return <Navigate to={accessToken ? '/home' : '/auth/login'} replace />;
}

export default function App() {
  return (
    <BrowserRouter>
      <NotificationPoller />
      <Routes>
        {/* Root redirect */}
        <Route path="/" element={<RootRedirect />} />

        {/* Auth (public) */}
        <Route path="/auth/login" element={<LoginPage />} />
        <Route path="/auth/register" element={<RegisterPage />} />
        <Route path="/oauth2/callback" element={<YandexCallbackPage />} />

        {/* All authenticated routes */}
        <Route element={<ProtectedRoute />}>
          <Route path="/home" element={<HomePage />} />
          <Route path="/festival/:id" element={<FestivalPage />} />
          <Route path="/event/:id" element={<EventDetailPage />} />
          <Route path="/stand/:id" element={<StandPage />} />
          <Route path="/notifications" element={<NotificationsPage />} />
        </Route>

        {/* USER-specific routes (USER and CURATOR can access /profile, /tickets, /history) */}
        <Route element={<ProtectedRoute roles={['USER', 'CURATOR']} />}>
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/tickets" element={<TicketsPage />} />
          <Route path="/history" element={<HistoryPage />} />
        </Route>

        {/* COMPANY routes */}
        <Route element={<ProtectedRoute roles={['COMPANY']} />}>
          <Route path="/company/profile" element={<CompanyProfilePage />} />
          <Route path="/company/festivals" element={<CompanyFestivalsPage />} />
          <Route path="/company/festivals/new" element={<CompanyFestivalNewPage />} />
          <Route path="/company/festivals/:id/edit" element={<CompanyFestivalEditPage />} />
          <Route path="/company/applications" element={<CompanyApplicationsPage />} />
          <Route path="/company/events" element={<CompanyEventsPage />} />
          <Route path="/company/events/:eventId/distribute" element={<CompanyAwardDistributionPage />} />
          <Route path="/company/festivals/:id/analytics" element={<CompanyFestivalAnalyticsPage />} />
        </Route>

        {/* ADMIN routes */}
        <Route element={<ProtectedRoute roles={['ADMIN']} />}>
          <Route path="/admin/profile" element={<AdminProfilePage />} />
          <Route path="/admin/applications" element={<AdminApplicationsPage />} />
          <Route path="/admin/applications/:id" element={<AdminApplicationDetailPage />} />
          <Route path="/admin/history" element={<AdminHistoryPage />} />
        </Route>

        {/* CURATOR-specific routes */}
        <Route element={<ProtectedRoute roles={['CURATOR']} />}>
          <Route path="/curator/profile" element={<CuratorProfilePage />} />
          <Route path="/curator/festivals" element={<CuratorFestivalsPage />} />
          <Route path="/curator/history" element={<CuratorHistoryPage />} />
          <Route path="/curator/tickets" element={<CuratorTicketsPage />} />
          <Route path="/curator/scanner" element={<CuratorScannerPage />} />
          <Route path="/curator/blacklist" element={<CuratorBlacklistPage />} />
        </Route>

        {/* Fallback */}
        <Route path="*" element={<RootRedirect />} />
      </Routes>
    </BrowserRouter>
  );
}
