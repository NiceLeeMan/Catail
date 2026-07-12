import axiosInstance from './axiosInstance';
import type { ApiResponse, PageResponse } from './types';
import type {
  CatalystDetailResponse,
  CatalystInfoResponse,
  CatalystListItemResponse,
  CatalystStatus,
  CatalystUpdateResponse,
  CreateCatalystPayload,
  UpdateCatalystBasicInfoPayload,
} from '../types/catalyst'

export const fetchCatalysts = async (page: number) => {
  const res = await axiosInstance.get<ApiResponse<PageResponse<CatalystListItemResponse>>>(
    '/catalysts',
    { params: { page } }, // page는 반드시 0-based로 전달
  );
  return res.data.data!;
};

export const createCatalyst = async (payload: CreateCatalystPayload) => {
  const res = await axiosInstance.post<ApiResponse<CatalystDetailResponse>>('/catalysts', payload);
  return res.data.data!;
};

export const fetchCatalystDetail = async (id: number) => {
  const res = await axiosInstance.get<ApiResponse<CatalystInfoResponse>>(`/catalysts/${id}`);
  return res.data.data!;
};

export const updateCatalystBasicInfo = async (id: number, payload: UpdateCatalystBasicInfoPayload) => {
  const res = await axiosInstance.put<ApiResponse<CatalystUpdateResponse>>(`/catalysts/${id}`, payload);
  return res.data.data!;
};
