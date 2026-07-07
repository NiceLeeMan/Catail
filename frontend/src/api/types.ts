export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: { code: string; message: string } | null;
}

export interface PageResponse<T> {
  items: T[];
  page: number; // 0-based (서버 기준)
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}
