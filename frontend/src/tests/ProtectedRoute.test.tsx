/**
 * Компонентный тест (раздел 3.3, сценарий 6)
 * Проверяет: пользователь с ролью COMPANY не получает доступ к странице куратора.
 * Тип: компонентный (unit) — без обращения к серверу, DOM эмулируется jsdom.
 * Библиотеки: Vitest, @testing-library/react, @testing-library/jest-dom.
 *
 * Запуск: npx vitest run src/tests/ProtectedRouteTest.tsx
 */

import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { describe, it, expect, vi } from 'vitest';

// ── Мок Zustand-стора авторизации ────────────────────────────────────────────

vi.mock('../store/authStore', () => ({
  useAuthStore: vi.fn(),
}));

import { useAuthStore } from '../store/authStore';
import ProtectedRoute from '../router/ProtectedRoute';

// ── Заглушки страниц ─────────────────────────────────────────────────────────

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

// ── Тесты ────────────────────────────────────────────────────────────────────

describe('3.3.6 — ProtectedRoute: разграничение доступа по роли', () => {

  /**
   * Тип: компонентный
   * Сценарий: пользователь с ролью COMPANY открывает /curator/scanner
   * Ожидаемый результат: страница сканера НЕ отображается, показывается заглушка
   */
  it('роль COMPANY не даёт доступа к странице куратора → редирект на /home', () => {
    renderWithRole('COMPANY', '/curator/scanner');

    expect(screen.queryByTestId('scanner-page')).toBeNull();
    expect(screen.getByTestId('home-page')).toBeInTheDocument();
  });

  /**
   * Тип: компонентный
   * Сценарий: пользователь с ролью USER открывает /curator/scanner
   * Ожидаемый результат: редирект на /home, сканер не виден
   */
  it('роль USER не даёт доступа к странице куратора → редирект на /home', () => {
    renderWithRole('USER', '/curator/scanner');

    expect(screen.queryByTestId('scanner-page')).toBeNull();
    expect(screen.getByTestId('home-page')).toBeInTheDocument();
  });

  /**
   * Тип: компонентный
   * Сценарий: пользователь с ролью CURATOR открывает /curator/scanner
   * Ожидаемый результат: страница сканера отображается
   */
  it('роль CURATOR открывает страницу сканера', () => {
    renderWithRole('CURATOR', '/curator/scanner');

    expect(screen.getByTestId('scanner-page')).toBeInTheDocument();
    expect(screen.queryByTestId('home-page')).toBeNull();
  });

  /**
   * Тип: компонентный
   * Сценарий: неавторизованный пользователь открывает /curator/scanner
   * Ожидаемый результат: перенаправление на /auth/login
   */
  it('без токена пользователь перенаправляется на страницу входа', () => {
    renderWithRole(null, '/curator/scanner');

    expect(screen.queryByTestId('scanner-page')).toBeNull();
    expect(screen.getByTestId('login-page')).toBeInTheDocument();
  });
});
