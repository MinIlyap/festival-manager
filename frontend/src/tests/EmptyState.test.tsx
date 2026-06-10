import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';

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

describe('EmptyState: каталог пуст при отсутствии фестивалей', () => {

  beforeEach(() => { vi.clearAllMocks(); });

  it('показывает EmptyState когда список фестивалей пуст', async () => {
    renderHomePage();

    await waitFor(() => {
      const empty = screen.queryByText(/нет фестивалей|ничего не найдено|нет мероприятий/i);
      expect(empty).toBeInTheDocument();
    });
  });


  it('спиннер исчезает после получения пустого ответа', async () => {
    renderHomePage();

    await waitFor(() => {
      expect(document.querySelector('[class*="spinner"]')).toBeNull();
    }, { timeout: 3000 });
  });
});
