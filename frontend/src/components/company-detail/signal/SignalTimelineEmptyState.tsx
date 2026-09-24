import { History } from 'lucide-react';

export function SignalTimelineEmptyState() {
  return (
    <div className="box-border flex w-full flex-col items-center justify-center gap-4 rounded-2xl border border-dashed border-white/[0.08] bg-[#131B2E] px-6 py-24 text-center">
      <div className="box-border flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-[#0F1729] text-[#34D399]">
        <History className="h-6 w-6" />
      </div>
      <h3 className="text-[18px] font-bold leading-normal text-[#F1F5F9]">
        아직 채택된 시그널이 없습니다
      </h3>
      <p className="max-w-[420px] text-[14px] font-normal leading-[21px] text-[#94A3B8]">
        시그널 탭에서 검토 대기 중인 뉴스를 채택하면 이곳에 타임라인으로 쌓여요.
      </p>
    </div>
  );
}
