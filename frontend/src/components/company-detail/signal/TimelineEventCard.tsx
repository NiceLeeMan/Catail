import { ExternalLink } from 'lucide-react';
import type { SignalListItemResponse } from '../../../types/signal';
import { formatTime } from '../../../utils/date';

interface TimelineEventCardProps {
  signal: SignalListItemResponse;
}

export function TimelineEventCard({ signal }: TimelineEventCardProps) {
  const handleOpen = () => {
    window.open(signal.link, '_blank', 'noopener');
  };

  return (
    <div className="box-border flex w-full flex-col gap-2 rounded-xl border border-white/[0.08] bg-[#131B2E] p-4 transition-colors duration-150 hover:border-white/[0.16]">
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-1.5 text-[12px] font-normal leading-normal text-[#64748B]">
          <span className="font-semibold text-[#34D399]">{signal.press ?? '출처 미상'}</span>
          <span aria-hidden="true">·</span>
          <span>{formatTime(signal.pubDate)}</span>
        </div>
        <button
          type="button"
          onClick={handleOpen}
          aria-label="원문 보기"
          className="box-border flex h-7 w-7 shrink-0 cursor-pointer items-center justify-center rounded-lg text-[#64748B] transition-colors duration-150 hover:bg-[#182338] hover:text-[#F1F5F9] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399]"
        >
          <ExternalLink className="h-3.5 w-3.5" />
        </button>
      </div>

      <h4 className="text-[14px] font-bold leading-normal text-[#F1F5F9]">{signal.title}</h4>

      {signal.relevanceReason && (
        <p className="text-[13px] font-normal leading-[19px] text-[#94A3B8]">
          {signal.relevanceReason}
        </p>
      )}
    </div>
  );
}
