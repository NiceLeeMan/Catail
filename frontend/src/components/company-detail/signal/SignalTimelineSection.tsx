import { useState } from 'react';
import { useCatalystsQuery } from '../../../hooks/useCatalysts';
import { useSignalTimelineQuery } from '../../../hooks/useSignals';
import { CatalystPicker } from '../CatalystPicker';
import { SignalListError } from './SignalListError';
import { SignalTimeline } from './SignalTimeline';
import { SignalTimelineEmptyState } from './SignalTimelineEmptyState';
import { SignalTimelineError } from './SignalTimelineError';
import { SignalTimelineSkeleton } from './SignalTimelineSkeleton';

interface SignalTimelineSectionProps {
  companyId: number;
}

export function SignalTimelineSection({ companyId }: SignalTimelineSectionProps) {
  const catalystsQuery = useCatalystsQuery(companyId);
  const catalysts = catalystsQuery.data?.catalysts ?? [];

  const [selectedCatalystId, setSelectedCatalystId] = useState<number | null>(null);
  const effectiveCatalystId = selectedCatalystId ?? catalysts[0]?.catalystId ?? null;

  const timelineQuery = useSignalTimelineQuery(effectiveCatalystId ?? NaN);

  if (catalystsQuery.isLoading) {
    return <SignalTimelineSkeleton />;
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
          카탈리스트 탭에서 추적할 이슈를 먼저 등록해보세요. 채택된 시그널이 쌓이면 여기에 타임라인으로
          보여드려요.
        </p>
      </div>
    );
  }

  const signals = timelineQuery.data?.signals ?? [];

  return (
    <div className="flex w-full flex-col gap-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <CatalystPicker
          catalysts={catalysts}
          selectedCatalystId={effectiveCatalystId}
          suffixLabel="의 타임라인"
          onSelect={setSelectedCatalystId}
        />
        {signals.length > 0 && (
          <span className="text-[13px] font-normal leading-normal text-[#64748B]">
            총 {signals.length}건의 확정 이슈
          </span>
        )}
      </div>

      {timelineQuery.isLoading ? (
        <SignalTimelineSkeleton />
      ) : timelineQuery.isError ? (
        <SignalTimelineError onRetry={() => timelineQuery.refetch()} />
      ) : signals.length === 0 ? (
        <SignalTimelineEmptyState />
      ) : (
        <SignalTimeline signals={signals} />
      )}
    </div>
  );
}
