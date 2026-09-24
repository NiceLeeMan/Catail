import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-react';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null;

  const groupSize = 5;
  const groupStart = Math.floor((currentPage - 1) / groupSize) * groupSize + 1;
  const groupEnd = Math.min(groupStart + groupSize - 1, totalPages);
  const pages = Array.from({ length: groupEnd - groupStart + 1 }, (_, i) => groupStart + i);

  return (
    <nav className="box-border flex w-full items-center justify-center gap-2">
      <button
        type="button"
        onClick={() => onPageChange(groupStart - 1)}
        disabled={groupStart === 1}
        aria-label="이전 그룹"
        className="box-border flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-white/[0.08] bg-[#131B2E] text-[#94A3B8] transition-colors duration-150 hover:bg-[#182338] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399] disabled:cursor-not-allowed disabled:opacity-40"
      >
        <ChevronsLeft className="h-4 w-4" />
      </button>

      <button
        type="button"
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 1}
        aria-label="이전 페이지"
        className="box-border flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-white/[0.08] bg-[#131B2E] text-[#94A3B8] transition-colors duration-150 hover:bg-[#182338] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399] disabled:cursor-not-allowed disabled:opacity-40"
      >
        <ChevronLeft className="h-4 w-4" />
      </button>

      {pages.map((page) => (
        <button
          key={page}
          type="button"
          onClick={() => onPageChange(page)}
          aria-current={page === currentPage ? 'page' : undefined}
          className={`box-border flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-[14px] font-semibold leading-normal transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399] ${
            page === currentPage
              ? 'border border-[#34D399] bg-[#0F1729] text-[#34D399]'
              : 'text-[#94A3B8] hover:bg-[#182338]'
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
        className="box-border flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-white/[0.08] bg-[#131B2E] text-[#94A3B8] transition-colors duration-150 hover:bg-[#182338] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399] disabled:cursor-not-allowed disabled:opacity-40"
      >
        <ChevronRight className="h-4 w-4" />
      </button>

      <button
        type="button"
        onClick={() => onPageChange(groupEnd + 1)}
        disabled={groupEnd === totalPages}
        aria-label="다음 그룹"
        className="box-border flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-white/[0.08] bg-[#131B2E] text-[#94A3B8] transition-colors duration-150 hover:bg-[#182338] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399] disabled:cursor-not-allowed disabled:opacity-40"
      >
        <ChevronsRight className="h-4 w-4" />
      </button>
    </nav>
  );
}
