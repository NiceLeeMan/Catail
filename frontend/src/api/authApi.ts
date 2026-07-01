import type { NavigateFunction } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { clearScheduledRefresh } from '../auth/tokenScheduler';
import axiosInstance from './axiosInstance';

export async function logout(navigate: NavigateFunction): Promise<void> {
  try {
    await axiosInstance.post('/auth/logout');
  } catch {
    // 만료된 토큰으로 로그아웃 시도해도 클라이언트 측 정리는 진행
  }
  useAuthStore.getState().clearAuth();
  clearScheduledRefresh();
  navigate('/login');
}
