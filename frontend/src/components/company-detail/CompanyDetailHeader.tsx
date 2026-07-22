import { getAvatarColor, getCompanyInitials } from '../../utils/companyAvatar';
import type { CompanyDetailResponse } from '../../types/company';

interface CompanyDetailHeaderProps {
  company: CompanyDetailResponse;
}

const MARKET_LABELS: Record<string, string> = {
  KOSPI: '코스피',
  KOSDAQ: '코스닥',
};

export function CompanyDetailHeader({ company }: CompanyDetailHeaderProps) {
  return (
    <div className="box-border flex w-full shrink-0 items-center gap-[18px] rounded-card border border-dark-border bg-dark-bg-card p-6">
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
        <h1 className="text-[26px] font-bold leading-normal text-dark-text-primary">
          {company.companyName}
        </h1>
        <div className="box-border flex w-fit shrink-0 items-center gap-2.5">
          <span className="text-[14px] font-normal leading-normal text-dark-text-secondary">
            {company.stockCode}
          </span>
          <span className="text-[14px] font-normal leading-normal text-dark-text-muted">
            ·
          </span>
          <span className="text-[14px] font-normal leading-normal text-dark-text-secondary">
            {MARKET_LABELS[company.market] ?? company.market}
          </span>
          {company.industryName && (
            <>
              <span className="text-[14px] font-normal leading-normal text-dark-text-muted">
                ·
              </span>
              <span className="text-[14px] font-normal leading-normal text-dark-text-secondary">
                {company.industryName}
              </span>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
