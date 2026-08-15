const SKELETON_COUNT = 3;

export function SignalListSkeleton() {
  return (
    <div className="flex w-full flex-col gap-4">
      {Array.from({ length: SKELETON_COUNT }, (_, i) => (
        <div
          key={i}
          className="box-border flex w-full flex-col gap-3 rounded-2xl border border-white/[0.08] bg-[#131B2E] p-5"
        >
          <div className="flex flex-col gap-1.5">
            <div className="skeleton-shimmer h-3 w-20 rounded-full" />
            <div className="skeleton-shimmer h-4 w-3/4 rounded-full" />
          </div>
          <div className="flex flex-col gap-1.5">
            <div className="skeleton-shimmer h-3.5 w-full rounded-full" />
            <div className="skeleton-shimmer h-3.5 w-2/3 rounded-full" />
          </div>
        </div>
      ))}
    </div>
  );
}
