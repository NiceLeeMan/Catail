import { Navigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { scheduleTokenRefresh } from '../auth/tokenScheduler';

function parseJwtExp(token: string): number {
  const payload = token.split('.')[1];
  const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
  return decoded.exp * 1000;
}

export function OAuthCallbackPage() {
  const [searchParams] = useSearchParams();
  const accessToken = searchParams.get('access_token');

  if (!accessToken) {
    return <Navigate to="/login" replace />;
  }

  const expiresAt = parseJwtExp(accessToken);
  useAuthStore.getState().setAuth(accessToken, expiresAt);
  scheduleTokenRefresh(expiresAt);

  return <Navigate to="/" replace />;
}
