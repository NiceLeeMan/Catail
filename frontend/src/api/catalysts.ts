import axiosInstance from './axiosInstance';
import type { ApiResponse } from './types';
import type {
  CatalystCreateRequest,
  CatalystCreateResponse,
  CatalystListResponse,
  CatalystStatusChangeRequest,
  CatalystStatusResponse,
  CatalystUpdateRequest,
  CatalystUpdateResponse,
} from '../types/catalyst';

export const fetchCatalysts = async (companyId: number) => {
  const res = await axiosInstance.get<ApiResponse<CatalystListResponse>>(
    `/companies/${companyId}/catalysts`
  );
  return res.data.data!;
};

export const createCatalyst = async (payload: CatalystCreateRequest) => {
  const res = await axiosInstance.post<ApiResponse<CatalystCreateResponse>>('/catalysts', payload);
  return res.data.data!;
};

export const updateCatalyst = async (catalystId: number, payload: CatalystUpdateRequest) => {
  const res = await axiosInstance.patch<ApiResponse<CatalystUpdateResponse>>(
    `/catalysts/${catalystId}`,
    payload
  );
  return res.data.data!;
};

export const changeCatalystStatus = async (
  catalystId: number,
  payload: CatalystStatusChangeRequest
) => {
  const res = await axiosInstance.patch<ApiResponse<CatalystStatusResponse>>(
    `/catalysts/${catalystId}/status`,
    payload
  );
  return res.data.data!;
};

export const deleteCatalyst = async (catalystId: number) => {
  await axiosInstance.delete(`/catalysts/${catalystId}`);
};
