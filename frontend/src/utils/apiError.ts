import { isAxiosError } from 'axios';
import type { ApiResponse } from '../api/types';

export function getApiErrorMessage(error: unknown, fallback: string): string {
  if (isAxiosError<ApiResponse<unknown>>(error)) {
    const message = error.response?.data?.error?.message;
    if (message) return message;
  }
  return fallback;
}
