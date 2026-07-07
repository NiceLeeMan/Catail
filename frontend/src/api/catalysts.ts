import axiosInstance from './axiosInstance';
import type { ApiResponse, PageResponse } from './types';
import type { CatalystListItemResponse } from '../types/catalyst';

export const fetchCatalysts = async (page: number) => {
  const res = await axiosInstance.get<ApiResponse<PageResponse<CatalystListItemResponse>>>(
    '/catalysts',
    { params: { page } }, // page는 반드시 0-based로 전달
  );
  return res.data.data!;
};
