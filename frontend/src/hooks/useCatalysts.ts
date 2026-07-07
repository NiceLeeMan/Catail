import { useQuery } from '@tanstack/react-query';
import { fetchCatalysts } from '../api/catalysts';

export const useCatalystsQuery = (currentPage: number) => {
  return useQuery({
    queryKey: ['catalysts', 'list', { page: currentPage }],
    queryFn: () => fetchCatalysts(currentPage - 1), // 1-based → 0-based 변환 지점
  });
};
