import axiosInstance from './axiosInstance';
import type { ApiResponse } from './types';
import type {
  QueryableSignalStatus,
  SignalListResponse,
  SignalStatusChangeRequest,
  SignalStatusResponse,
  SignalTimelineResponse,
} from '../types/signal';

export const fetchSignals = async (
  catalystId: number,
  status: QueryableSignalStatus,
  cursor?: string,
  size = 10
) => {
  const res = await axiosInstance.get<ApiResponse<SignalListResponse>>(
    `/catalysts/${catalystId}/signals`,
    { params: { status, ...(cursor ? { cursor } : {}), size } }
  );
  return res.data.data!;
};

export const fetchSignalTimeline = async (catalystId: number) => {
  const res = await axiosInstance.get<ApiResponse<SignalTimelineResponse>>(
    `/catalysts/${catalystId}/signals/timeline`
  );
  return res.data.data!;
};

export const changeSignalStatus = async (signalId: number, payload: SignalStatusChangeRequest) => {
  const res = await axiosInstance.patch<ApiResponse<SignalStatusResponse>>(
    `/signals/${signalId}/status`,
    payload
  );
  return res.data.data!;
};
