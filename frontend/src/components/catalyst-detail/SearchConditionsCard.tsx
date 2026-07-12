import { RefreshCw } from 'lucide-react';

interface SearchConditionsCardProps {
  conditions: string[];
}

export function SearchConditionsCard({ conditions }: SearchConditionsCardProps) {
  return (
    <div className="box-border flex flex-1 flex-col items-start rounded-card border border-border bg-bg-surface">
      <div className="box-border flex w-full items-center justify-between px-6 pb-4 pt-5">
        <div className="box-border flex items-center gap-2">
          <h2 className="text-[16px] font-bold text-text-primary">모니터링 검색 조건</h2>
          <span className="text-[13px] font-medium text-text-muted">총 {conditions.length}개</span>
        </div>
        <button
          type="button"
          className="box-border flex shrink-0 items-center gap-1.5 rounded-md px-3.5 py-2 text-[12px] font-semibold text-white hover:opacity-90"
          style={{ backgroundImage: 'linear-gradient(90deg, #2E6BF2 0%, #00B89B 100%)' }}
        >
          <RefreshCw className="h-3.5 w-3.5" />
          검색 조건 재생성
        </button>
      </div>

      <p className="box-border w-full px-6 pb-4 text-[12px] font-normal text-text-muted">
        이 카탈리스트는 아래 검색 조건으로 시그널을 수집합니다.
      </p>

      <div className="h-px w-full bg-border" />

      <div className="box-border w-full px-6 py-4">
        <div className="box-border flex w-full flex-col items-start overflow-hidden rounded-lg border border-border">
          {conditions.map((condition, index) => (
            <div
              key={condition}
              className={`box-border flex w-full items-center gap-3 px-3.5 py-2.5 ${
                index % 2 === 1 ? 'bg-bg-base' : 'bg-bg-surface'
              } ${index > 0 ? 'border-t border-border' : ''}`}
            >
              <span className="box-border flex h-[22px] w-[22px] shrink-0 items-center justify-center rounded-md border border-border bg-bg-base text-[11px] font-semibold text-text-secondary">
                {index + 1}
              </span>
              <span className="flex-1 text-[12.5px] font-normal text-text-primary">{condition}</span>
            </div>
          ))}
          <div className="box-border flex w-full items-center justify-center border-t border-border px-3.5 py-2">
            <span className="text-[13px] font-normal text-text-muted">···</span>
          </div>
        </div>
      </div>
    </div>
  );
}
