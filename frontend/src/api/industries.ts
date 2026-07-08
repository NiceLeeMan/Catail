import axiosInstance from './axiosInstance';
import type { ApiResponse } from './types';
import type { Industry } from '../types/industry';

export const fetchIndustries = async () => {
  const res = await axiosInstance.get<ApiResponse<Industry[]>>('/industries');
  return res.data.data!;
};
