const SKELETON_COUNT = 3;

export function CatalystListSkeleton() {
  return (
    <div className="grid w-full grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
      {Array.from({ length: SKELETON_COUNT }, (_, i) => (
        <div
          key={i}
          className="box-border flex w-full flex-col gap-4 rounded-2xl border border-white/[0.08] bg-[#131B2E] p-5"
        >
          <div className="flex flex-col gap-2">
            <div className="skeleton-shimmer h-4 w-28 rounded-full" />
            <div className="flex gap-1.5">
              <div className="skeleton-shimmer h-5 w-16 rounded-full" />
              <div className="skeleton-shimmer h-5 w-12 rounded-full" />
            </div>
          </div>
          <div className="flex flex-col gap-1.5">
            <div className="skeleton-shimmer h-3.5 w-full rounded-full" />
            <div className="skeleton-shimmer h-3.5 w-3/4 rounded-full" />
          </div>
          <div className="skeleton-shimmer h-8 w-full rounded-full" />
        </div>
      ))}
    </div>
  );
}
