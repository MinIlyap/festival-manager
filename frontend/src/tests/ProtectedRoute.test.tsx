import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { describe, it, expect, vi } from 'vitest';


vi.mock('../store/authStore', () => ({
  useAuthStore: vi.fn(),
}));

import { useAuthStore } from '../store/authStore';
import ProtectedRoute from '../router/ProtectedRoute';


const ScannerPage = () => <div data-testid="scanner-page">Страница сканера</div>;
const HomePage    = () => <div data-testid="home-page">Главная</div>;
const LoginPage   = () => <div data-testid="login-page">Вход</div>;

function renderWithRole(role: string | null, path: string) {
  (useAuthStore as ReturnType<typeof vi.fn>).mockReturnValue({
    accessToken: role ? 'mock.jwt.token' : null,
    role,
  });

  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route element={<ProtectedRoute roles={['CURATOR']} />}>
          <Route path="/curator/scanner" element={<ScannerPage />} />
        </Route>
        <Route path="/home"       element={<HomePage />} />
        <Route path="/auth/login" element={<LoginPage />} />
      </Routes>
    </MemoryRouter>
  );
}

describe('ProtectedRoute: разграничение доступа по роли', () => {

  it('роль COMPANY не даёт доступа к странице куратора → редирект на /home', () => {
    renderWithRole('COMPANY', '/curator/scanner');

    expect(screen.queryByTestId('scanner-page')).toBeNull();
    expect(screen.getByTestId('home-page')).toBeInTheDocument();
  });

  it('роль USER не даёт доступа к странице куратора → редирект на /home', () => {
    renderWithRole('USER', '/curator/scanner');

    expect(screen.queryByTestId('scanner-page')).toBeNull();
    expect(screen.getByTestId('home-page')).toBeInTheDocument();
  });

  it('роль CURATOR открывает страницу сканера', () => {
    renderWithRole('CURATOR', '/curator/scanner');

    expect(screen.getByTestId('scanner-page')).toBeInTheDocument();
    expect(screen.queryByTestId('home-page')).toBeNull();
  });

  it('без токена пользователь перенаправляется на страницу входа', () => {
    renderWithRole(null, '/curator/scanner');

    expect(screen.queryByTestId('scanner-page')).toBeNull();
    expect(screen.getByTestId('login-page')).toBeInTheDocument();
  });
});
