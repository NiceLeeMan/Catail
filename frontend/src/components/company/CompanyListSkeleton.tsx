const SKELETON_ROW_COUNT = 8;

export function CompanyListSkeleton() {
  return (
    <div className="box-border w-full shrink-0 overflow-hidden rounded-xl border border-dark-border bg-dark-bg-card shadow-card">
      {Array.from({ length: SKELETON_ROW_COUNT }, (_, i) => (
        <div
          key={i}
          className="box-border flex h-16 w-full shrink-0 items-center gap-5 border-b border-dark-border px-7 last:border-b-0"
        >
          <div className="flex w-[340px] shrink-0 items-center gap-3.5">
            <div className="skeleton-shimmer h-[38px] w-[38px] shrink-0 rounded-full" />
            <div className="skeleton-shimmer h-4 w-40 rounded-full" />
          </div>
          <div className="w-[150px] shrink-0">
            <div className="skeleton-shimmer h-4 w-16 rounded-full" />
          </div>
          <div className="flex-1">
            <div className="skeleton-shimmer h-4 w-24 rounded-full" />
          </div>
          <div className="h-[18px] w-[18px] shrink-0" />
        </div>
      ))}
    </div>
  );
}