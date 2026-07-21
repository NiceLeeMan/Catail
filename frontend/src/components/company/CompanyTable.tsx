import { ChevronRight } from 'lucide-react';
import type { CompanyListItemResponse } from '../../types/company';
import { getAvatarColor, getCompanyInitials } from '../../utils/companyAvatar';

interface CompanyTableProps {
  items: CompanyListItemResponse[];
  onRowClick: (company: CompanyListItemResponse) => void;
}

function CompanyAvatar({ company }: { company: CompanyListItemResponse }) {
  if (company.logoUrl) {
    return (
      <img
        src={company.logoUrl}
        alt={company.companyName}
        className="h-[38px] w-[38px] shrink-0 rounded-full object-cover"
      />
    );
  }

  return (
    <div
      className="box-border flex h-[38px] w-[38px] shrink-0 items-center justify-center rounded-full"
      style={{ backgroundColor: getAvatarColor(company.stockCode) }}
    >
      <span className="text-[13px] font-bold leading-normal text-white">
        {getCompanyInitials(company.companyName)}
      </span>
    </div>
  );
}

export function CompanyTable({ items, onRowClick }: CompanyTableProps) {
  return (
    <div className="box-border w-full shrink-0 overflow-hidden rounded-xl border border-dark-border bg-dark-bg-card shadow-card">
      <div className="box-border flex w-full items-center gap-5 border-b border-dark-border bg-dark-bg-card-header px-7 py-3.5">
        <div className="w-[340px] shrink-0 text-[13px] font-bold leading-normal text-dark-text-secondary">
          기업
        </div>
        <div className="w-[150px] shrink-0 text-[13px] font-bold leading-normal text-dark-text-secondary">
          종목코드
        </div>
        <div className="flex-1 text-[13px] font-bold leading-normal text-dark-text-secondary">
          업종
        </div>
        <div className="h-[18px] w-[18px] shrink-0" />
      </div>

      {items.map((company) => (
        <button
          key={company.stockCode}
          type="button"
          onClick={() => onRowClick(company)}
          className="box-border flex h-16 w-full shrink-0 items-center gap-5 border-b border-dark-border px-7 text-left last:border-b-0 hover:bg-dark-bg-card-header"
        >
          <div className="flex w-[340px] shrink-0 items-center gap-3.5">
            <CompanyAvatar company={company} />
            <span className="flex-1 truncate text-[15px] font-semibold leading-normal text-dark-text-primary">
              {company.companyName}
            </span>
          </div>
          <div className="w-[150px] shrink-0 text-[14px] font-normal leading-normal text-dark-text-secondary">
            {company.stockCode}
          </div>
          <div className="flex-1 text-[14px] font-normal leading-normal text-dark-text-secondary">
            {company.industryName ?? '-'}
          </div>
          <ChevronRight className="h-[18px] w-[18px] shrink-0 text-dark-text-muted" />
        </button>
      ))}
    </div>
  );
}
