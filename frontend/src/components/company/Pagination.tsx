import { ChevronLeft, ChevronRight } from 'lucide-react';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null;

  const pages = Array.from({ length: totalPages }, (_, i) => i + 1);

  return (
    <nav className="box-border flex w-full items-center justify-center gap-2">
      <button
        type="button"
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 1}
        aria-label="이전 페이지"
        className="box-border flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-dark-border bg-dark-bg-card text-dark-text-secondary disabled:cursor-not-allowed disabled:opacity-40 hover:bg-dark-bg-card-header"
      >
        <ChevronLeft className="h-4 w-4" />
      </button>

      {pages.map((page) => (
        <button
          key={page}
          type="button"
          onClick={() => onPageChange(page)}
          aria-current={page === currentPage ? 'page' : undefined}
          className={`box-border flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-[14px] font-semibold leading-normal ${
            page === currentPage
              ? 'border border-dark-accent bg-dark-bg-badge text-dark-accent'
              : 'text-dark-text-secondary hover:bg-dark-bg-card-header'
          }`}
        >
          {page}
        </button>
      ))}

      <button
        type="button"
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages}
        aria-label="다음 페이지"
        className="box-border flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-dark-border bg-dark-bg-card text-dark-text-secondary disabled:cursor-not-allowed disabled:opacity-40 hover:bg-dark-bg-card-header"
      >
        <ChevronRight className="h-4 w-4" />
      </button>
    </nav>
  );
}
