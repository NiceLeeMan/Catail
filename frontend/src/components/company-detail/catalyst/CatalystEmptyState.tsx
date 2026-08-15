import { Radar } from 'lucide-react';

export function CatalystEmptyState() {
  return (
    <div className="box-border flex w-full flex-col items-center justify-center gap-4 rounded-2xl border border-dashed border-white/[0.08] bg-[#131B2E] px-6 py-24 text-center">
      <div className="box-border flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-[#0F1729] text-[#34D399]">
        <Radar className="h-6 w-6" />
      </div>
      <h3 className="text-[18px] font-bold leading-normal text-[#F1F5F9]">
        등록된 카탈리스트가 없습니다
      </h3>
      <p className="max-w-[420px] text-[14px] font-normal leading-[21px] text-[#94A3B8]">
        추적하고 싶은 이슈를 카탈리스트로 등록하면 관련 뉴스와 공시를 실시간으로 모니터링해요.
      </p>
    </div>
  );
}
