/**
 * Компонентный тест (раздел 3.4, сценарий 5)
 * Проверяет: при пустом каталоге фестивалей отображается компонент EmptyState.
 * Тип: компонентный (unit) — API замоканы, DOM эмулируется jsdom.
 * Библиотеки: Vitest, @testing-library/react, @testing-library/jest-dom.
 *
 * Запуск: npx vitest run src/tests/EmptyStateTest.tsx
 */

import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';

// ── Моки API и стора ─────────────────────────────────────────────────────────

vi.mock('../api/festival', () => ({
  festivalApi: {
    getAll:    vi.fn().mockResolvedValue([]),
    myTickets: vi.fn().mockResolvedValue([]),
  },
}));

vi.mock('../api/event', () => ({
  eventApi: {
    getAll: vi.fn().mockResolvedValue([]),
  },
}));

vi.mock('../store/authStore', () => ({
  useAuthStore: () => ({ accessToken: null, role: null }),
}));

// ── Хелпер рендера ───────────────────────────────────────────────────────────

import HomePage from '../pages/home/HomePage';

function renderHomePage() {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <HomePage />
      </MemoryRouter>
    </QueryClientProvider>
  );
}

// ── Тесты ────────────────────────────────────────────────────────────────────

describe('3.4.5 — EmptyState: каталог пуст при отсутствии фестивалей', () => {

  beforeEach(() => { vi.clearAllMocks(); });

  /**
   * Тип: компонентный
   * Сценарий: API вернул пустые массивы, фильтры не заданы
   * Ожидаемый результат: виден EmptyState с текстом об отсутствии данных
   */
  it('показывает EmptyState когда список фестивалей пуст', async () => {
    renderHomePage();

    await waitFor(() => {
      const empty = screen.queryByText(/нет фестивалей|ничего не найдено|нет мероприятий/i);
      expect(empty).toBeInTheDocument();
    });
  });

  /**
   * Тип: компонентный
   * Сценарий: страница не падает с ошибкой при пустом ответе
   * Ожидаемый результат: спиннер загрузки не висит бесконечно
   */
  it('спиннер исчезает после получения пустого ответа', async () => {
    renderHomePage();

    await waitFor(() => {
      expect(document.querySelector('[class*="spinner"]')).toBeNull();
    }, { timeout: 3000 });
  });
});
