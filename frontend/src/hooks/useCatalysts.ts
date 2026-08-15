import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  changeCatalystStatus,
  createCatalyst,
  deleteCatalyst,
  fetchCatalysts,
  updateCatalyst,
} from '../api/catalysts';
import type {
  CatalystCreateRequest,
  CatalystStatusChangeRequest,
  CatalystUpdateRequest,
} from '../types/catalyst';

const catalystsKey = (companyId: number) => ['companies', companyId, 'catalysts'] as const;

export const useCatalystsQuery = (companyId: number) =>
  useQuery({
    queryKey: catalystsKey(companyId),
    queryFn: () => fetchCatalysts(companyId),
    enabled: !Number.isNaN(companyId),
  });

export const useCreateCatalystMutation = (companyId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CatalystCreateRequest) => createCatalyst(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: catalystsKey(companyId) }),
  });
};

export const useUpdateCatalystMutation = (companyId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ catalystId, payload }: { catalystId: number; payload: CatalystUpdateRequest }) =>
      updateCatalyst(catalystId, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: catalystsKey(companyId) }),
  });
};

export const useChangeCatalystStatusMutation = (companyId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      catalystId,
      payload,
    }: {
      catalystId: number;
      payload: CatalystStatusChangeRequest;
    }) => changeCatalystStatus(catalystId, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: catalystsKey(companyId) }),
  });
};

export const useDeleteCatalystMutation = (companyId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (catalystId: number) => deleteCatalyst(catalystId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: catalystsKey(companyId) }),
  });
};
