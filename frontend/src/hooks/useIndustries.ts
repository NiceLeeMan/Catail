import { useQuery } from '@tanstack/react-query';
import { fetchIndustries } from '../api/industries';

export const useIndustriesQuery = () => {
  return useQuery({
    queryKey: ['industries'],
    queryFn: fetchIndustries,
  });
};
