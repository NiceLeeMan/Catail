import { SearchX } from 'lucide-react';

interface CompanyEmptyStateProps {
  isSearching: boolean;
}

export function CompanyEmptyState({ isSearching }: CompanyEmptyStateProps) {
  return (
    <div className="box-border flex w-full flex-col items-center justify-center gap-4 rounded-xl border border-dashed border-dark-border bg-dark-bg-card px-6 py-24 text-center">
      <div className="box-border flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-dark-bg-badge text-dark-accent">
        <SearchX className="h-6 w-6" />
      </div>
      <h3 className="text-[18px] font-bold leading-normal text-dark-text-primary">
        {isSearching ? '검색 결과가 없습니다' : '등록된 기업이 없습니다'}
      </h3>
      <p className="max-w-[420px] text-[14px] font-normal leading-[21px] text-dark-text-secondary">
        {isSearching
          ? '다른 기업명 또는 종목코드로 다시 검색해보세요.'
          : '현재 조회 가능한 상장기업이 없습니다.'}
      </p>
    </div>
  );
}
