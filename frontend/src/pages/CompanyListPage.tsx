import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CompanyPageHeader } from '../components/company/CompanyPageHeader';
import { HeroBanner } from '../components/company/HeroBanner';
import { CompanyMarketTabs } from '../components/company/CompanyMarketTabs';
import { CompanySearchBar } from '../components/company/CompanySearchBar';
import { CompanyTable } from '../components/company/CompanyTable';
import { Pagination } from '../components/company/Pagination';
import { CompanyListSkeleton } from '../components/company/CompanyListSkeleton';
import { CompanyListError } from '../components/company/CompanyListError';
import { CompanyEmptyState } from '../components/company/CompanyEmptyState';
import { useCompaniesQuery } from '../hooks/useCompanies';
import { useDebouncedValue } from '../hooks/useDebouncedValue';
import { useCountUp } from '../hooks/useCountUp';
import type { CompanyListItemResponse, Market } from '../types/company';

export function CompanyListPage() {
  const navigate = useNavigate();
  const [market, setMarket] = useState<Market>('KOSPI');
  const [keyword, setKeyword] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const debouncedKeyword = useDebouncedValue(keyword, 300);
  const isSearching = debouncedKeyword.trim().length > 0;

  const { data, isLoading, isFetching, isError } = useCompaniesQuery(
    market,
    currentPage,
    debouncedKeyword
  );
  const displayedCount = useCountUp(data?.totalElements ?? 0);

  const handleMarketChange = (nextMarket: Market) => {
    setMarket(nextMarket);
    setCurrentPage(1);
  };

  const handleKeywordChange = (value: string) => {
    setKeyword(value);
    setCurrentPage(1);
  };

  const handleRowClick = (company: CompanyListItemResponse) => {
    navigate(`/companies/${company.id}`);
  };

  return (
    <div className="box-border flex min-h-screen w-full flex-col items-center bg-dark-bg-base">
      <CompanyPageHeader />
      <HeroBanner />

      <main className="mx-auto box-border flex w-full max-w-[1280px] flex-col gap-6 px-8 py-9">
        <div className="box-border flex w-full items-center justify-between">
          <CompanyMarketTabs activeMarket={market} onChange={handleMarketChange} />
          <span className="text-[14px] font-normal leading-normal text-dark-text-secondary">
            총 {displayedCount}건
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
          <div
            className={`flex w-full flex-col gap-6 transition-opacity duration-200 ${
              isFetching ? 'opacity-50' : 'opacity-100'
            }`}
          >
            <CompanyTable items={data.items} onRowClick={handleRowClick} />
            <Pagination
              currentPage={currentPage}
              totalPages={data.totalPages}
              onPageChange={setCurrentPage}
            />
          </div>
        )}
      </main>
    </div>
  );
}
