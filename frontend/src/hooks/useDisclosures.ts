import { useInfiniteQuery } from '@tanstack/react-query';
import { fetchDisclosures } from '../api/disclosures';

export const useDisclosuresQuery = (companyId: number) => {
  return useInfiniteQuery({
    queryKey: ['companies', 'disclosures', companyId],
    queryFn: ({ pageParam }) => fetchDisclosures(companyId, pageParam),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (lastPage) =>
      lastPage.hasNext ? (lastPage.nextCursor ?? undefined) : undefined,
    enabled: !Number.isNaN(companyId),
  });
};
