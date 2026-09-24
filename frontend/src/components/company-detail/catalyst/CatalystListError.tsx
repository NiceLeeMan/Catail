interface CatalystListErrorProps {
  onRetry: () => void;
}

export function CatalystListError({ onRetry }: CatalystListErrorProps) {
  return (
    <div
      role="alert"
      className="flex w-full flex-col items-center justify-center gap-4 rounded-2xl border border-white/[0.08] bg-[#131B2E] py-24 text-center"
    >
      <p className="text-[14px] font-medium leading-normal text-[#94A3B8]">
        카탈리스트 목록을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.
      </p>
      <button
        type="button"
        onClick={onRetry}
        className="box-border cursor-pointer rounded-lg border border-white/[0.08] bg-[#0F1729] px-4 py-2 text-[13px] font-semibold leading-normal text-[#F1F5F9] transition-colors duration-150 hover:bg-[#182338] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399]"
      >
        다시 시도
      </button>
    </div>
  );
}
