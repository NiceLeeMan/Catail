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
        className="box-border flex h-9 w-9 shrink-0 items-center justify-center rounded-full border border-border text-text-secondary disabled:cursor-not-allowed disabled:opacity-40 hover:bg-bg-base"
      >
        <ChevronLeft className="h-4 w-4" />
      </button>

      {pages.map((page) => (
        <button
          key={page}
          type="button"
          onClick={() => onPageChange(page)}
          aria-current={page === currentPage ? 'page' : undefined}
          className={`box-border flex h-9 w-9 shrink-0 items-center justify-center rounded-full text-[14px] font-semibold leading-normal ${
            page === currentPage
              ? 'bg-primary text-white'
              : 'text-text-secondary hover:bg-bg-base'
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
        className="box-border flex h-9 w-9 shrink-0 items-center justify-center rounded-full border border-border text-text-secondary disabled:cursor-not-allowed disabled:opacity-40 hover:bg-bg-base"
      >
        <ChevronRight className="h-4 w-4" />
      </button>
    </nav>
  );
}
