import { useState } from 'react';
import { useCatalystsQuery } from '../../../hooks/useCatalysts';
import { useChangeSignalStatusMutation, useSignalsQuery } from '../../../hooks/useSignals';
import type { QueryableSignalStatus, SignalListItemResponse } from '../../../types/signal';
import { CatalystPicker } from '../CatalystPicker';
import { LoadMoreButton } from '../LoadMoreButton';
import { SignalCard } from './SignalCard';
import { SignalEmptyState } from './SignalEmptyState';
import { SignalListError } from './SignalListError';
import { SignalListSkeleton } from './SignalListSkeleton';
import { SignalStatusTabs } from './SignalStatusTabs';

interface SignalSectionProps {
  companyId: number;
}

export function SignalSection({ companyId }: SignalSectionProps) {
  const catalystsQuery = useCatalystsQuery(companyId);
  const catalysts = catalystsQuery.data?.catalysts ?? [];

  const [selectedCatalystId, setSelectedCatalystId] = useState<number | null>(null);
  const [activeStatus, setActiveStatus] = useState<QueryableSignalStatus>('PENDING');

  const effectiveCatalystId = selectedCatalystId ?? catalysts[0]?.catalystId ?? null;
  const signalsQuery = useSignalsQuery(effectiveCatalystId ?? NaN, activeStatus);
  const statusMutation = useChangeSignalStatusMutation(effectiveCatalystId ?? NaN);

  const handleChangeStatus = (signal: SignalListItemResponse, status: 'ADOPTED' | 'EXCLUDED') => {
    statusMutation.mutate({ signalId: signal.signalId, payload: { status } });
  };

  if (catalystsQuery.isLoading) {
    return <SignalListSkeleton />;
  }

  if (catalystsQuery.isError) {
    return <SignalListError onRetry={() => catalystsQuery.refetch()} />;
  }

  if (catalysts.length === 0) {
    return (
      <div className="box-border flex w-full flex-col items-center justify-center gap-2 rounded-2xl border border-dashed border-white/[0.08] bg-[#131B2E] px-6 py-24 text-center">
        <h3 className="text-[18px] font-bold leading-normal text-[#F1F5F9]">
          아직 카탈리스트가 없습니다
        </h3>
        <p className="max-w-[420px] text-[14px] font-normal leading-[21px] text-[#94A3B8]">
          카탈리스트 탭에서 추적할 이슈를 먼저 등록해보세요. 시그널은 활성화된 카탈리스트를 기준으로
          수집돼요.
        </p>
      </div>
    );
  }

  const pages = signalsQuery.data?.pages ?? [];
  const signals = pages.flatMap((page) => page.signals);

  return (
    <div className="flex w-full flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <CatalystPicker
          catalysts={catalysts}
          selectedCatalystId={effectiveCatalystId}
          suffixLabel="의 시그널"
          onSelect={setSelectedCatalystId}
        />

        <SignalStatusTabs activeStatus={activeStatus} onChange={setActiveStatus} />
      </div>

      {signalsQuery.isLoading ? (
        <SignalListSkeleton />
      ) : signalsQuery.isError ? (
        <SignalListError onRetry={() => signalsQuery.refetch()} />
      ) : signals.length === 0 ? (
        <SignalEmptyState status={activeStatus} />
      ) : (
        <>
          <div className="flex w-full flex-col gap-2">
            {signals.map((signal) => (
              <SignalCard
                key={signal.signalId}
                signal={signal}
                isChangingStatus={
                  statusMutation.isPending &&
                  statusMutation.variables?.signalId === signal.signalId
                }
                onAdopt={() => handleChangeStatus(signal, 'ADOPTED')}
                onReject={() => handleChangeStatus(signal, 'EXCLUDED')}
              />
            ))}
          </div>
          <LoadMoreButton
            visible={!!signalsQuery.hasNextPage}
            isLoading={signalsQuery.isFetchingNextPage}
            onClick={() => signalsQuery.fetchNextPage()}
          />
        </>
      )}
    </div>
  );
}
