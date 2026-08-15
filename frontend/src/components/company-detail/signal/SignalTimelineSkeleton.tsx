const GROUP_COUNT = 2;
const ITEMS_PER_GROUP = 2;

export function SignalTimelineSkeleton() {
  return (
    <div className="relative">
      <div aria-hidden="true" className="absolute bottom-2 left-[15px] top-2 w-px bg-white/[0.06]" />
      <div className="flex flex-col gap-7">
        {Array.from({ length: GROUP_COUNT }, (_, groupIndex) => (
          <div key={groupIndex} className="flex flex-col gap-3">
            <div className="relative flex items-center gap-2.5 pl-9">
              <span
                aria-hidden="true"
                className="absolute left-[10px] top-1/2 h-3 w-3 -translate-y-1/2 rounded-full bg-white/[0.1]"
              />
              <div className="skeleton-shimmer h-3.5 w-32 rounded-full" />
            </div>
            <div className="flex flex-col gap-3 pl-9">
              {Array.from({ length: ITEMS_PER_GROUP }, (_, itemIndex) => (
                <div
                  key={itemIndex}
                  className="relative box-border flex w-full flex-col gap-2 rounded-xl border border-white/[0.08] bg-[#131B2E] p-4"
                >
                  <span
                    aria-hidden="true"
                    className="absolute -left-[22px] top-5 h-1.5 w-1.5 rounded-full bg-white/[0.1]"
                  />
                  <div className="skeleton-shimmer h-3 w-24 rounded-full" />
                  <div className="skeleton-shimmer h-4 w-3/4 rounded-full" />
                  <div className="skeleton-shimmer h-3.5 w-full rounded-full" />
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
