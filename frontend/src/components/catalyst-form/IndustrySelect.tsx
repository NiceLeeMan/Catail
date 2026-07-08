import { useState } from 'react';
import { Search, X } from 'lucide-react';
import { useIndustriesQuery } from '../../hooks/useIndustries';

const MAX_SELECTED = 10;

interface IndustrySelectProps {
  value: number[];
  onChange: (industryIds: number[]) => void;
  error?: string;
}

export function IndustrySelect({ value, onChange, error }: IndustrySelectProps) {
  const { data: industries, isLoading } = useIndustriesQuery();
  const [keyword, setKeyword] = useState('');

  const filtered = (industries ?? []).filter((industry) =>
    industry.name.toLowerCase().includes(keyword.trim().toLowerCase()),
  );
  const selectedIndustries = (industries ?? []).filter((industry) => value.includes(industry.id));
  const isFull = value.length >= MAX_SELECTED;

  const toggle = (id: number) => {
    if (value.includes(id)) {
      onChange(value.filter((industryId) => industryId !== id));
      return;
    }
    if (isFull) return;
    onChange([...value, id]);
  };

  return (
    <div className="box-border flex w-full flex-col gap-2">
      <div className="box-border flex items-baseline justify-between">
        <label className="text-[14px] font-semibold leading-normal text-text-primary">
          관련 산업 <span className="text-status-ended">*</span>
        </label>
        <span className="text-[12px] font-normal text-text-muted">
          {value.length}개 선택됨 (최대 {MAX_SELECTED}개)
        </span>
      </div>
      <p className="text-[13px] font-normal leading-normal text-text-secondary">
        카탈리스트와 연관된 산업을 선택하세요. 하나 이상 선택할 수 있습니다.
      </p>

      {selectedIndustries.length > 0 && (
        <div className="box-border flex flex-wrap gap-2">
          {selectedIndustries.map((industry) => (
            <span
              key={industry.id}
              className="box-border flex items-center gap-1 rounded-full bg-primary-soft px-3 py-1.5 text-[13px] font-medium text-primary"
            >
              {industry.name}
              <button
                type="button"
                onClick={() => toggle(industry.id)}
                aria-label={`${industry.name} 선택 해제`}
              >
                <X className="h-3 w-3" />
              </button>
            </span>
          ))}
        </div>
      )}

      <div className="relative box-border w-full">
        <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-text-muted" />
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="산업 검색 또는 선택"
          className="box-border w-full rounded-card border border-border bg-bg-surface py-3 pl-10 pr-4 text-[14px] text-text-primary placeholder:text-text-muted focus:border-primary focus:outline-none"
        />
      </div>

      <div className="box-border flex max-h-48 w-full flex-col overflow-y-auto rounded-card border border-border bg-bg-surface">
        {isLoading ? (
          <p className="px-4 py-3 text-[13px] text-text-muted">불러오는 중...</p>
        ) : filtered.length === 0 ? (
          <p className="px-4 py-3 text-[13px] text-text-muted">검색 결과가 없습니다.</p>
        ) : (
          filtered.map((industry) => {
            const selected = value.includes(industry.id);
            const disabled = !selected && isFull;
            return (
              <button
                key={industry.id}
                type="button"
                disabled={disabled}
                onClick={() => toggle(industry.id)}
                className={`box-border flex w-full items-center justify-between px-4 py-2.5 text-left text-[14px] disabled:cursor-not-allowed disabled:opacity-40 ${
                  selected ? 'bg-primary-soft text-primary' : 'text-text-primary hover:bg-bg-base'
                }`}
              >
                {industry.name}
              </button>
            );
          })
        )}
      </div>

      {error && <p className="text-[13px] font-normal leading-normal text-status-ended">{error}</p>}
    </div>
  );
}
