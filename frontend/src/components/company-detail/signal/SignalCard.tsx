import { Check, ExternalLink, X } from 'lucide-react';
import type { SignalListItemResponse } from '../../../types/signal';
import { formatDate } from '../../../utils/date';

interface SignalCardProps {
  signal: SignalListItemResponse;
  isChangingStatus: boolean;
  onAdopt: () => void;
  onReject: () => void;
}

const ACTION_BUTTON =
  'box-border flex h-8 shrink-0 cursor-pointer items-center gap-1.5 rounded-full border px-3 text-[12px] font-semibold leading-normal transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399] disabled:cursor-not-allowed disabled:opacity-40';

const NEUTRAL_BUTTON = `${ACTION_BUTTON} border-white/[0.08] bg-[#0F1729] text-[#94A3B8] hover:border-white/[0.16] hover:text-[#F1F5F9]`;
const ADOPT_BUTTON = `${ACTION_BUTTON} border-[#34D399]/30 bg-[#10B981]/10 text-[#34D399] hover:border-[#34D399]/50 hover:bg-[#10B981]/20`;
const REJECT_BUTTON = `${ACTION_BUTTON} border-[#EF4444]/30 bg-[#EF4444]/10 text-[#EF4444] hover:border-[#EF4444]/50 hover:bg-[#EF4444]/20`;

export function SignalCard({ signal, isChangingStatus, onAdopt, onReject }: SignalCardProps) {
  const handleOpen = () => {
    window.open(signal.link, '_blank', 'noopener');
  };

  return (
    <div className="box-border flex w-full flex-wrap items-center gap-x-3 gap-y-2.5 rounded-xl border border-white/[0.08] bg-[#131B2E] px-5 py-4">
      <div className="flex min-w-0 flex-1 flex-col gap-1">
        <div className="flex items-center gap-1.5 text-[12px] font-normal leading-normal text-[#64748B]">
          <span>{signal.press ?? '출처 미상'}</span>
          <span aria-hidden="true">·</span>
          <span>{formatDate(signal.pubDate)}</span>
        </div>
        <p className="truncate text-[14px] font-semibold leading-normal text-[#F1F5F9]">
          {signal.title}
        </p>
        {signal.relevanceReason && (
          <p className="truncate text-[12px] font-normal leading-normal text-[#34D399]">
            {signal.relevanceReason}
          </p>
        )}
      </div>

      <div className="flex shrink-0 items-center gap-1.5">
        <button type="button" onClick={handleOpen} aria-label="원문 보기" className={NEUTRAL_BUTTON}>
          <ExternalLink className="h-3.5 w-3.5" />
          원문
        </button>
        {signal.status !== 'ADOPTED' && (
          <button
            type="button"
            onClick={onAdopt}
            disabled={isChangingStatus}
            aria-label="채택"
            className={ADOPT_BUTTON}
          >
            <Check className="h-3.5 w-3.5" />
            채택
          </button>
        )}
        {signal.status !== 'EXCLUDED' && (
          <button
            type="button"
            onClick={onReject}
            disabled={isChangingStatus}
            aria-label="기각"
            className={REJECT_BUTTON}
          >
            <X className="h-3.5 w-3.5" />
            기각
          </button>
        )}
      </div>
    </div>
  );
}
