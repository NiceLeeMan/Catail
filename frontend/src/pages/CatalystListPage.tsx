import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus } from 'lucide-react';
import { AppHeader } from '../components/layout/AppHeader';
import { CardGrid } from '../components/catalyst/CardGrid';
import { Pagination } from '../components/catalyst/Pagination';
import { EmptyState } from '../components/catalyst/EmptyState';
import { CatalystListSkeleton } from '../components/catalyst/CatalystListSkeleton';
import { CatalystListError } from '../components/catalyst/CatalystListError';
import { useCatalystsQuery } from '../hooks/useCatalysts';

function PageHeader({ count, onCreateClick }: { count: number; onCreateClick: () => void }) {
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
        onClick={onCreateClick}
        className="box-border flex shrink-0 items-center gap-2 rounded-full px-5 py-3 text-[14px] font-semibold leading-normal text-white transition hover:opacity-90 active:scale-95"
        style={{ backgroundImage: 'linear-gradient(90deg, #1F56E6 0%, #00D4B4 100%)' }}
      >
        <Plus className="h-4 w-4" />
        새 카탈리스트
      </button>
    </div>
  );
}

export function CatalystListPage() {
  const navigate = useNavigate();
  const [currentPage, setCurrentPage] = useState(1);
  const { data, isLoading, isError } = useCatalystsQuery(currentPage);

  const handleCardClick = (id: number) => {
    navigate(`/catalysts/${id}`);
  };

  return (
    <div className="box-border flex min-h-screen w-full flex-col items-center bg-bg-base">
      <AppHeader />

      <main className="mx-auto box-border flex w-full max-w-[1280px] flex-col gap-8 px-6 py-10">
        {isLoading ? (
          <CatalystListSkeleton />
        ) : isError || !data ? (
          <CatalystListError />
        ) : (
          <>
            <PageHeader count={data.totalElements} onCreateClick={() => navigate('/catalysts/new')} />

            {data.items.length === 0 ? (
              <EmptyState />
            ) : (
              <>
                <CardGrid catalysts={data.items} onCardClick={handleCardClick} />
                <Pagination
                  currentPage={currentPage}
                  totalPages={data.totalPages}
                  onPageChange={setCurrentPage}
                />
              </>
            )}
          </>
        )}
      </main>
    </div>
  );
}
