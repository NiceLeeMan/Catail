import axios from 'axios';
import { useAuthStore } from '../store/authStore';

const refreshClient = axios.create({
  baseURL: `${import.meta.env.VITE_API_BASE_URL}/api`,
  withCredentials: true,
});

let timerId: ReturnType<typeof setTimeout> | null = null;

function parseJwtExp(token: string): number {
  const payload = token.split('.')[1];
  const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
  return decoded.exp * 1000;
}

export function scheduleTokenRefresh(expiresAtMs: number): void {
  if (timerId !== null) {
    clearTimeout(timerId);
  }

  const delay = expiresAtMs - Date.now() - 5 * 60 * 1000;
  if (delay <= 0) return;

  timerId = setTimeout(async () => {
    timerId = null;
    try {
      const res = await refreshClient.post<{ data: { access_token: string } }>('/auth/refresh');
      const newToken = res.data.data.access_token;
      const newExpiresAt = parseJwtExp(newToken);
      useAuthStore.getState().setAuth(newToken, newExpiresAt);
      scheduleTokenRefresh(newExpiresAt);
    } catch {
      useAuthStore.getState().clearAuth();
    }
  }, delay);
}

export function clearScheduledRefresh(): void {
  if (timerId !== null) {
    clearTimeout(timerId);
    timerId = null;
  }
}
