import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { changeSignalStatus, fetchSignalTimeline, fetchSignals } from '../api/signals';
import type { QueryableSignalStatus, SignalStatusChangeRequest } from '../types/signal';

export const useSignalsQuery = (catalystId: number, status: QueryableSignalStatus) => {
  return useInfiniteQuery({
    queryKey: ['catalysts', catalystId, 'signals', status],
    queryFn: ({ pageParam }) => fetchSignals(catalystId, status, pageParam, 10),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (lastPage) =>
      lastPage.hasNext ? (lastPage.nextCursor ?? undefined) : undefined,
    enabled: !Number.isNaN(catalystId),
  });
};

export const useSignalTimelineQuery = (catalystId: number) => {
  return useQuery({
    queryKey: ['catalysts', catalystId, 'signals', 'timeline'],
    queryFn: () => fetchSignalTimeline(catalystId),
    enabled: !Number.isNaN(catalystId),
  });
};

export const useChangeSignalStatusMutation = (catalystId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      signalId,
      payload,
    }: {
      signalId: number;
      payload: SignalStatusChangeRequest;
    }) => changeSignalStatus(signalId, payload),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ['catalysts', catalystId, 'signals'] }),
  });
};
