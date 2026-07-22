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
        className="box-border flex h-11 w-40 shrink-0 items-center justify-center rounded-[10px] border border-dark-border bg-dark-bg-card text-[14px] font-semibold leading-normal text-dark-text-secondary hover:bg-dark-bg-card-header disabled:opacity-50"
      >
        {isLoading ? '불러오는 중...' : '더보기'}
      </button>
    </div>
  );
}
