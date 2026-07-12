import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  changeCatalystStatus,
  createCatalyst,
  deleteCatalyst,
  fetchCatalystDetail,
  fetchCatalysts,
} from '../api/catalysts';
import type { CatalystStatus } from '../types/catalyst';

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
  });
};
