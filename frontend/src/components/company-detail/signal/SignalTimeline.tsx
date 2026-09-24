import type { SignalListItemResponse } from '../../../types/signal';
import { groupSignalsByDate } from '../../../utils/signalTimeline';
import { TimelineEventCard } from './TimelineEventCard';

interface SignalTimelineProps {
  signals: SignalListItemResponse[];
}

const MAX_STAGGER_DELAY_MS = 400;
const STAGGER_STEP_MS = 40;

export function SignalTimeline({ signals }: SignalTimelineProps) {
  const groups = groupSignalsByDate(signals);
  const globalIndexBySignalId = new Map<number, number>();
  for (const signal of signals) {
    globalIndexBySignalId.set(signal.signalId, globalIndexBySignalId.size);
  }

  return (
    <div className="relative">
      <div
        aria-hidden="true"
        className="absolute bottom-2 left-[15px] top-2 w-px bg-gradient-to-b from-[#34D399]/50 via-white/[0.1] to-white/[0.05]"
      />

      <div className="flex flex-col gap-7">
        {groups.map((group, groupIndex) => (
          <div key={group.key} className="flex flex-col gap-3">
            <div className="relative flex items-center gap-2.5 pl-9">
              <span
                aria-hidden="true"
                className="absolute left-[10px] top-1/2 h-3 w-3 -translate-y-1/2 rounded-full border-2 border-[#0B1120] bg-[#34D399] shadow-[0_0_0_3px_rgba(52,211,153,0.15)]"
              />
              <h3 className="text-[13px] font-bold leading-normal text-[#F1F5F9]">
                {group.dateLabel}
              </h3>
              <span className="text-[12px] font-normal leading-normal text-[#64748B]">
                {group.items.length}건
              </span>
              {groupIndex === 0 && (
                <span className="rounded-full bg-[#10B981]/10 px-2 py-0.5 text-[11px] font-semibold leading-normal text-[#34D399]">
                  최신
                </span>
              )}
            </div>

            <div className="flex flex-col gap-3 pl-9">
              {group.items.map((signal) => {
                const index = globalIndexBySignalId.get(signal.signalId) ?? 0;
                const delay = Math.min(index * STAGGER_STEP_MS, MAX_STAGGER_DELAY_MS);
                return (
                  <div
                    key={signal.signalId}
                    className="relative motion-safe:animate-[timeline-item-in_0.45s_ease-out_both]"
                    style={{ animationDelay: `${delay}ms` }}
                  >
                    <span
                      aria-hidden="true"
                      className="absolute -left-[22px] top-5 h-1.5 w-1.5 rounded-full bg-[#64748B]"
                    />
                    <TimelineEventCard signal={signal} />
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
