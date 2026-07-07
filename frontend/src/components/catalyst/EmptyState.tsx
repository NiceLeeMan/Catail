import { Compass } from 'lucide-react';

export function EmptyState() {
  return (
    <div className="box-border flex w-full flex-col items-center justify-center gap-4 rounded-card border border-dashed border-border bg-bg-surface px-6 py-24 text-center">
      <div className="box-border flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-primary-soft text-primary">
        <Compass className="h-6 w-6" />
      </div>
      <h3 className="text-[18px] font-bold leading-normal text-text-primary">
        등록된 카탈리스트가 없습니다
      </h3>
      <p className="max-w-[420px] text-[14px] font-normal leading-[21px] text-text-secondary">
        관심 있는 산업의 변화를 추적할 카탈리스트를 새로 만들어보세요
      </p>
    </div>
  );
}
