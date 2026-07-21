import { useState } from 'react';
import { TrendingUp } from 'lucide-react';
import { CompanyPageHeader } from '../components/company/CompanyPageHeader';
import { HeroBanner } from '../components/company/HeroBanner';
import { CompanySearchBar } from '../components/company/CompanySearchBar';
import { CompanyTable } from '../components/company/CompanyTable';
import { Pagination } from '../components/company/Pagination';
import { CompanyListSkeleton } from '../components/company/CompanyListSkeleton';
import { CompanyListError } from '../components/company/CompanyListError';
import { CompanyEmptyState } from '../components/company/CompanyEmptyState';
import { useCompaniesQuery } from '../hooks/useCompanies';
import { useDebouncedValue } from '../hooks/useDebouncedValue';
import type { CompanyListItemResponse } from '../types/company';

export function CompanyListPage() {
  const [keyword, setKeyword] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const debouncedKeyword = useDebouncedValue(keyword, 300);
  const isSearching = debouncedKeyword.trim().length > 0;

  const { data, isLoading, isError } = useCompaniesQuery(currentPage, debouncedKeyword);

  const handleKeywordChange = (value: string) => {
    setKeyword(value);
    setCurrentPage(1);
  };

  const handleRowClick = (company: CompanyListItemResponse) => {
    console.log('company clicked', company.stockCode);
  };

  return (
    <div className="box-border flex min-h-screen w-full flex-col items-center bg-dark-bg-base">
      <CompanyPageHeader />
      <HeroBanner />

      <main className="mx-auto box-border flex w-full max-w-[1280px] flex-col gap-6 px-8 py-9">
        <div className="box-border flex w-full items-center justify-between">
          <div className="box-border flex w-fit shrink-0 items-center gap-2.5">
            <div className="box-border flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-dark-bg-badge">
              <TrendingUp className="h-4 w-4 text-dark-accent" />
            </div>
            <span className="text-[17px] font-semibold leading-normal text-dark-text-primary">
              코스피 상장기업
            </span>
          </div>
          <span className="text-[14px] font-normal leading-normal text-dark-text-secondary">
            총 {data?.totalElements ?? 0}건
          </span>
        </div>

        <CompanySearchBar value={keyword} onChange={handleKeywordChange} />

        {isLoading ? (
          <CompanyListSkeleton />
        ) : isError || !data ? (
          <CompanyListError />
        ) : data.items.length === 0 ? (
          <CompanyEmptyState isSearching={isSearching} />
        ) : (
          <>
            <CompanyTable items={data.items} onRowClick={handleRowClick} />
            <Pagination
              currentPage={currentPage}
              totalPages={data.totalPages}
              onPageChange={setCurrentPage}
            />
          </>
        )}
      </main>
    </div>
  );
}
