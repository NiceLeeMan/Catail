import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  changeCatalystStatus,
  createCatalyst,
  fetchCatalystDetail,
  fetchCatalysts,
  updateCatalystBasicInfo,
} from '../api/catalysts';
import type { CatalystStatus, UpdateCatalystBasicInfoPayload } from '../types/catalyst';

export const useCatalystsQuery = (currentPage: number) => {
  return useQuery({
    queryKey: ['catalysts', 'list', { page: currentPage }],
    queryFn: () => fetchCatalysts(currentPage - 1), // 1-based → 0-based 변환 지점
  });
};

// staleTime 기본값(0)에 의존 — 생성 후 목록으로 돌아가면 재마운트 시 자동 재조회됨.
// 전역 staleTime 조정 시 재검토 필요.
export const useCreateCatalystMutation = () => {
  return useMutation({
    mutationFn: createCatalyst,
  });
};

export const useCatalystDetailQuery = (id: number) => {
  return useQuery({
    queryKey: ['catalysts', 'detail', id],
    queryFn: () => fetchCatalystDetail(id),
    enabled: !Number.isNaN(id),
  })
}

export const useUpdateCatalystBasicInfoMutation = (id: number) => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: UpdateCatalystBasicInfoPayload) =>
      updateCatalystBasicInfo(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['catalysts', 'detail', id] })
      queryClient.invalidateQueries({ queryKey: ['catalysts', 'list'] })
    },
  })
}

// 상세 페이지에 머무른 채로 상태가 바뀌므로(재마운트 없음) 성공 시 상세/목록 쿼리를 직접 무효화한다.
export const useChangeCatalystStatusMutation = (id: number) => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (targetStatus: CatalystStatus) =>
      changeCatalystStatus(id, targetStatus),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['catalysts', 'detail', id] })
      queryClient.invalidateQueries({ queryKey: ['catalysts', 'list'] })
    },
  })
}