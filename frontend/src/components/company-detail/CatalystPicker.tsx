import type { CatalystListItemResponse } from '../../types/catalyst';

interface CatalystPickerProps {
  catalysts: CatalystListItemResponse[];
  selectedCatalystId: number | null;
  suffixLabel: string;
  onSelect: (catalystId: number) => void;
}

export function CatalystPicker({
  catalysts,
  selectedCatalystId,
  suffixLabel,
  onSelect,
}: CatalystPickerProps) {
  if (catalysts.length <= 1) {
    return (
      <span className="text-[13px] font-normal leading-normal text-[#64748B]">
        {catalysts[0]?.title}
        {suffixLabel}
      </span>
    );
  }

  return (
    <div className="flex flex-wrap items-center gap-1.5" role="tablist" aria-label="카탈리스트 선택">
      {catalysts.map((catalyst) => {
        const isActive = catalyst.catalystId === selectedCatalystId;
        return (
          <button
            key={catalyst.catalystId}
            type="button"
            onClick={() => onSelect(catalyst.catalystId)}
            aria-current={isActive ? 'true' : undefined}
            className={`box-border rounded-full border px-3 py-1.5 text-[13px] font-medium leading-normal transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399] ${
              isActive
                ? 'border-[#34D399] bg-[#10B981]/10 text-[#34D399]'
                : 'border-white/[0.08] text-[#94A3B8] hover:text-[#F1F5F9]'
            }`}
          >
            {catalyst.title}
          </button>
        );
      })}
    </div>
  );
}
