import { getAvatarColor, getCompanyInitials } from '../../utils/companyAvatar';
import type { CompanyDetailResponse } from '../../types/company';

interface CompanyDetailHeaderProps {
  company: CompanyDetailResponse;
}

const MARKET_LABELS: Record<string, string> = {
  KOSPI: '코스피',
  KOSDAQ: '코스닥',
  NASDAQ: '나스닥',
};

export function CompanyDetailHeader({ company }: CompanyDetailHeaderProps) {
  return (
    <div className="box-border flex w-full shrink-0 items-center gap-[18px] rounded-2xl border border-white/[0.08] bg-[#131B2E] p-6">
      {company.logoUrl ? (
        <img
          src={company.logoUrl}
          alt={company.companyName}
          className="h-[60px] w-[60px] shrink-0 rounded-2xl object-cover"
        />
      ) : (
        <div
          className="box-border flex h-[60px] w-[60px] shrink-0 items-center justify-center rounded-2xl"
          style={{ backgroundColor: getAvatarColor(company.stockCode) }}
        >
          <span className="text-[22px] font-bold leading-normal text-white">
            {getCompanyInitials(company.companyName)}
          </span>
        </div>
      )}

      <div className="box-border flex w-fit shrink-0 flex-col gap-1.5">
        <h1 className="text-[26px] font-bold leading-normal text-[#F1F5F9]">
          {company.companyName}
        </h1>
        <div className="box-border flex w-fit shrink-0 items-center gap-2.5">
          <span className="text-[14px] font-normal leading-normal text-[#94A3B8]">
            {company.stockCode}
          </span>
          <span className="text-[14px] font-normal leading-normal text-[#64748B]">·</span>
          <span className="text-[14px] font-normal leading-normal text-[#94A3B8]">
            {MARKET_LABELS[company.market] ?? company.market}
          </span>
          {company.industryName && (
            <>
              <span className="text-[14px] font-normal leading-normal text-[#64748B]">·</span>
              <span className="text-[14px] font-normal leading-normal text-[#94A3B8]">
                {company.industryName}
              </span>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
