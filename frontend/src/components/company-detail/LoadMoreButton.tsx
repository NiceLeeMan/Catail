interface LoadMoreButtonProps {
  visible: boolean;
  isLoading: boolean;
  onClick: () => void;
}

export function LoadMoreButton({ visible, isLoading, onClick }: LoadMoreButtonProps) {
  if (!visible) return null;

  return (
    <div className="box-border flex w-full shrink-0 items-start justify-center">
      <button
        type="button"
        onClick={onClick}
        disabled={isLoading}
        className="box-border flex h-11 w-40 shrink-0 cursor-pointer items-center justify-center rounded-[10px] border border-white/[0.08] bg-[#131B2E] text-[14px] font-semibold leading-normal text-[#94A3B8] transition-colors duration-150 hover:bg-[#182338] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399] disabled:cursor-not-allowed disabled:opacity-50"
      >
        {isLoading ? '불러오는 중...' : '더보기'}
      </button>
    </div>
  );
}
