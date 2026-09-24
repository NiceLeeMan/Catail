import { ExternalLink } from 'lucide-react';
import type { DisclosureItemResponse } from '../../types/disclosure';
import { formatDate } from '../../utils/date';
import { RemarkBadge } from './RemarkBadge';

interface DisclosureTableProps {
  items: DisclosureItemResponse[];
}

const ROW_GRID =
  'grid grid-cols-[92px_1fr_50px_16px] items-center gap-3 md:grid-cols-[120px_1fr_160px_60px_16px] md:gap-5';

export function DisclosureTable({ items }: DisclosureTableProps) {
  const handleOpenSource = (sourceUrl: string) => {
    window.open(sourceUrl, '_blank', 'noopener');
  };

  return (
    <div className="box-border w-full shrink-0 overflow-hidden rounded-2xl border border-white/[0.08] bg-[#131B2E]">
      <div className={`box-border w-full border-b border-white/[0.08] bg-[#182338] px-5 py-3.5 md:px-7 ${ROW_GRID}`}>
        <div className="text-[13px] font-bold leading-normal text-[#94A3B8]">접수일자</div>
        <div className="text-[13px] font-bold leading-normal text-[#94A3B8]">보고서명</div>
        <div className="hidden text-[13px] font-bold leading-normal text-[#94A3B8] md:block">제출인</div>
        <div className="text-center text-[13px] font-bold leading-normal text-[#94A3B8]">비고</div>
        <div className="h-4 w-4 shrink-0" />
      </div>

      {items.map((item) => (
        <button
          key={item.id}
          type="button"
          onClick={() => handleOpenSource(item.sourceUrl)}
          className={`box-border w-full border-b border-white/[0.08] px-5 py-4 text-left transition-colors duration-150 last:border-b-0 hover:bg-[#182338] focus-visible:bg-[#182338] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-[#34D399] md:h-[60px] md:px-7 md:py-0 ${ROW_GRID}`}
        >
          <div className="truncate text-[14px] font-normal leading-normal text-[#94A3B8]">
            {formatDate(item.receivedDate)}
          </div>
          <div className="truncate text-[15px] font-medium leading-normal text-[#F1F5F9]">
            {item.reportName}
          </div>
          <div className="hidden truncate text-[14px] font-normal leading-normal text-[#94A3B8] md:block">
            {item.submitterName}
          </div>
          <div className="flex items-center justify-center">
            <RemarkBadge codes={item.remarkCodes} />
          </div>
          <ExternalLink className="h-4 w-4 shrink-0 text-[#64748B]" />
        </button>
      ))}
    </div>
  );
}
