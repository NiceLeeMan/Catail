import { Search } from 'lucide-react';

interface CompanySearchBarProps {
  value: string;
  onChange: (value: string) => void;
}

export function CompanySearchBar({ value, onChange }: CompanySearchBarProps) {
  return (
    <div className="box-border flex h-[52px] w-full shrink-0 items-center gap-2.5 rounded-xl border border-dark-accent bg-dark-bg-card px-[18px]">
      <Search className="h-[18px] w-[18px] shrink-0 text-dark-text-secondary" />
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="기업명 또는 종목코드 검색"
        className="w-full bg-transparent text-[15px] font-normal leading-normal text-dark-text-primary placeholder:text-dark-text-muted focus:outline-none"
      />
    </div>
  );
}
