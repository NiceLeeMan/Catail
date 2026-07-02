import axios from 'axios';
import { useAuthStore } from '../store/authStore';
import { clearScheduledRefresh } from '../auth/tokenScheduler';

const axiosInstance = axios.create({
  baseURL: `${import.meta.env.VITE_API_BASE_URL}/api`,
  withCredentials: true,
});

axiosInstance.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().clearAuth();
      clearScheduledRefresh();
    }
    return Promise.reject(error);
  },
);

export default axiosInstance;
