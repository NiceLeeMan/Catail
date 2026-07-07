import { useState } from 'react';
import { ChevronDown, Plus } from 'lucide-react';
import logo from '../asset/logo.png';
import { CatalystCard } from '../components/catalyst/CatalystCard';
import { Pagination } from '../components/catalyst/Pagination';
import { EmptyState } from '../components/catalyst/EmptyState';
import type { Catalyst } from '../types/catalyst';

const PAGE_SIZE = 20;

interface CatalystListPageProps {
  catalysts?: Catalyst[];
}

function Header() {
  return (
    <header className="sticky top-0 z-10 box-border w-full border-b border-border bg-bg-surface">
      <div className="mx-auto box-border flex h-16 w-full max-w-[1280px] items-center justify-between px-6">
        <img src={logo} alt="Catail" className="h-8" />
        <div className="box-border flex items-center gap-3">
          <div className="box-border flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary-soft text-[13px] font-semibold text-primary">
            KM
          </div>
          <span className="text-[14px] font-medium leading-normal text-text-primary">김민준</span>
          <ChevronDown className="h-4 w-4 text-text-muted" />
        </div>
      </div>
    </header>
  );
}

function PageHeader({ count }: { count: number }) {
  return (
    <div className="box-border flex w-full items-center justify-between">
      <div className="box-border flex flex-col items-start gap-1">
        <h1 className="text-[24px] font-bold leading-normal text-text-primary">카탈리스트</h1>
        <p className="text-[14px] font-normal leading-normal text-text-secondary">
          관심 산업의 변화를 추적하는 카탈리스트 {count}개
        </p>
      </div>
      <button
        type="button"
        className="box-border flex shrink-0 items-center gap-2 rounded-full px-5 py-3 text-[14px] font-semibold leading-normal text-white"
        style={{ backgroundImage: 'linear-gradient(90deg, #1F56E6 0%, #00D4B4 100%)' }}
      >
        <Plus className="h-4 w-4" />
        새 카탈리스트
      </button>
    </div>
  );
}

export function CatalystListPage({ catalysts = [] }: CatalystListPageProps) {
  const [currentPage, setCurrentPage] = useState(1);

  const pagedCatalysts = catalysts.slice(
    (currentPage - 1) * PAGE_SIZE,
    currentPage * PAGE_SIZE
  );
  const totalPages = Math.ceil(catalysts.length / PAGE_SIZE);

  const handleCardClick = (id: number) => {
    console.log('catalyst clicked', id);
  };

  return (
    <div className="box-border flex min-h-screen w-full flex-col items-center bg-bg-base">
      <Header />

      <main className="mx-auto box-border flex w-full max-w-[1280px] flex-col gap-8 px-6 py-10">
        <PageHeader count={catalysts.length} />

        {catalysts.length === 0 ? (
          <EmptyState />
        ) : (
          <>
            <div className="grid w-full grid-cols-2 gap-6">
              {pagedCatalysts.map((catalyst) => (
                <CatalystCard key={catalyst.id} {...catalyst} onClick={handleCardClick} />
              ))}
            </div>
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={setCurrentPage}
            />
          </>
        )}
      </main>
    </div>
  );
}
