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

const ROW_GRID = 'grid grid-cols-[1fr_88px_20px] items-center gap-4 md:grid-cols-[1fr_140px_160px_20px] md:gap-5';

export function CompanyTable({ items, onRowClick }: CompanyTableProps) {
  return (
    <div className="box-border w-full shrink-0 overflow-hidden rounded-xl border border-white/[0.08] bg-[#131B2E] shadow-[0_2px_10px_rgba(0,0,0,0.25)]">
      <div className={`box-border w-full border-b border-white/[0.08] bg-[#182338] px-5 py-3.5 md:px-7 ${ROW_GRID}`}>
        <div className="text-[13px] font-bold leading-normal text-[#94A3B8]">기업</div>
        <div className="text-[13px] font-bold leading-normal text-[#94A3B8]">종목코드</div>
        <div className="hidden text-[13px] font-bold leading-normal text-[#94A3B8] md:block">업종</div>
        <div className="h-[18px] w-[18px] shrink-0" />
      </div>

      {items.map((company) => (
        <button
          key={company.id}
          type="button"
          onClick={() => onRowClick(company)}
          className={`group box-border w-full border-b border-white/[0.08] px-5 py-4 text-left transition-colors duration-150 last:border-b-0 hover:bg-[#182338] focus-visible:bg-[#182338] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-[#34D399] md:px-7 md:py-0 md:h-16 ${ROW_GRID}`}
        >
          <div className="flex min-w-0 items-center gap-3.5">
            <CompanyAvatar company={company} />
            <span className="flex-1 truncate text-[15px] font-semibold leading-normal text-[#F1F5F9]">
              {company.companyName}
            </span>
          </div>
          <div className="truncate text-[14px] font-normal leading-normal text-[#94A3B8]">
            {company.stockCode}
          </div>
          <div className="hidden truncate text-[14px] font-normal leading-normal text-[#94A3B8] md:block">
            {company.industryName ?? '-'}
          </div>
          <ChevronRight className="h-[18px] w-[18px] shrink-0 text-[#64748B] transition-transform duration-200 group-hover:translate-x-0.5 group-hover:text-[#94A3B8]" />
        </button>
      ))}
    </div>
  );
}
