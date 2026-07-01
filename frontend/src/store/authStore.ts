import { create } from 'zustand';

interface AuthState {
  accessToken: string | null;
  expiresAt: number | null;
  setAuth: (token: string, expiresAt: number) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  expiresAt: null,
  setAuth: (token, expiresAt) => set({ accessToken: token, expiresAt }),
  clearAuth: () => set({ accessToken: null, expiresAt: null }),
}));
