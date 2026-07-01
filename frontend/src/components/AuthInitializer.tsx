import { useEffect, useRef, useState } from 'react';
import axiosInstance from '../api/axiosInstance';
import { useAuthStore } from '../store/authStore';
import { scheduleTokenRefresh } from '../auth/tokenScheduler';

function parseJwtExp(token: string): number {
  const payload = token.split('.')[1];
  const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
  return decoded.exp * 1000;
}

interface Props {
  children: React.ReactNode;
}

export function AuthInitializer({ children }: Props) {
  const [initialized, setInitialized] = useState(false);
  const hasRun = useRef(false);

  useEffect(() => {
    if (hasRun.current) return;
    hasRun.current = true;

    axiosInstance
      .post<{ data: { access_token: string } }>('/auth/refresh')
      .then((res) => {
        const token = res.data.data.access_token;
        const expiresAt = parseJwtExp(token);
        useAuthStore.getState().setAuth(token, expiresAt);
        scheduleTokenRefresh(expiresAt);
      })
      .catch(() => {
        // 비로그인 정상 케이스 — store는 이미 빈 상태
      })
      .finally(() => {
        setInitialized(true);
      });
  }, []);

  if (!initialized) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-bg-base">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
      </div>
    );
  }

  return <>{children}</>;
}
