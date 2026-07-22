import { ExternalLink } from 'lucide-react';
import type { DisclosureItemResponse } from '../../types/disclosure';
import { formatDate } from '../../utils/date';
import { RemarkBadge } from './RemarkBadge';

interface DisclosureTableProps {
  items: DisclosureItemResponse[];
}

export function DisclosureTable({ items }: DisclosureTableProps) {
  const handleOpenSource = (sourceUrl: string) => {
    window.open(sourceUrl, '_blank', 'noopener');
  };

  return (
    <div className="box-border w-full shrink-0 overflow-hidden rounded-card border border-dark-border bg-dark-bg-card">
      <div className="box-border flex w-full items-center gap-5 border-b border-dark-border bg-dark-bg-card-header px-7 py-3.5">
        <div className="w-[120px] shrink-0 text-[13px] font-bold leading-normal text-dark-text-secondary">
          접수일자
        </div>
        <div className="flex-1 text-[13px] font-bold leading-normal text-dark-text-secondary">
          보고서명
        </div>
        <div className="w-[160px] shrink-0 text-[13px] font-bold leading-normal text-dark-text-secondary">
          제출인
        </div>
        <div className="w-[60px] shrink-0 text-center text-[13px] font-bold leading-normal text-dark-text-secondary">
          비고
        </div>
        <div className="h-4 w-4 shrink-0" />
      </div>

      {items.map((item) => (
        <button
          key={item.id}
          type="button"
          onClick={() => handleOpenSource(item.sourceUrl)}
          className="box-border flex h-[60px] w-full shrink-0 items-center gap-5 border-b border-dark-border px-7 text-left last:border-b-0 hover:bg-dark-bg-card-header"
        >
          <div className="w-[120px] shrink-0 text-[14px] font-normal leading-normal text-dark-text-secondary">
            {formatDate(item.receivedDate)}
          </div>
          <div className="flex-1 truncate text-[15px] font-medium leading-normal text-dark-text-primary">
            {item.reportName}
          </div>
          <div className="w-[160px] shrink-0 truncate text-[14px] font-normal leading-normal text-dark-text-secondary">
            {item.submitterName}
          </div>
          <div className="flex w-[60px] shrink-0 items-center justify-center">
            <RemarkBadge codes={item.remarkCodes} />
          </div>
          <ExternalLink className="h-4 w-4 shrink-0 text-dark-text-muted" />
        </button>
      ))}
    </div>
  );
}
