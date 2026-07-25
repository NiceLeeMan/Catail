import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { fetchCompanies, fetchCompanyDetail } from '../api/companies';
import type { Market } from '../types/company';

export const useCompaniesQuery = (market: Market, currentPage: number, keyword: string) => {
  const trimmed = keyword.trim();

  return useQuery({
    queryKey: ['companies', 'list', { market, page: currentPage, keyword: trimmed }],
    queryFn: () => fetchCompanies(market, currentPage - 1, trimmed || undefined), // 1-based → 0-based 변환 지점
    placeholderData: keepPreviousData,
  });
};

export const useCompanyDetailQuery = (id: number) => {
  return useQuery({
    queryKey: ['companies', 'detail', id],
    queryFn: () => fetchCompanyDetail(id),
    enabled: !Number.isNaN(id),
  });
};
