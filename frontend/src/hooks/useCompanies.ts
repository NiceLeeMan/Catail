import { useQuery } from '@tanstack/react-query';
import { fetchCompanies, fetchCompanyDetail } from '../api/companies';

export const useCompaniesQuery = (currentPage: number, keyword: string) => {
  const trimmed = keyword.trim();

  return useQuery({
    queryKey: ['companies', 'list', { page: currentPage, keyword: trimmed }],
    queryFn: () => fetchCompanies(currentPage - 1, trimmed || undefined), // 1-based → 0-based 변환 지점
  });
};

export const useCompanyDetailQuery = (id: number) => {
  return useQuery({
    queryKey: ['companies', 'detail', id],
    queryFn: () => fetchCompanyDetail(id),
    enabled: !Number.isNaN(id),
  });
};
