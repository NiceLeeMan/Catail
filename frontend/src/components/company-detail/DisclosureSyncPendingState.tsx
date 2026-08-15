import { RefreshCw } from 'lucide-react';

export function DisclosureSyncPendingState() {
  return (
    <div className="box-border flex w-full flex-col items-center justify-center gap-4 rounded-2xl border border-dashed border-white/[0.08] bg-[#131B2E] px-6 py-24 text-center">
      <div className="box-border flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-[#0F1729] text-[#34D399]">
        <RefreshCw className="h-6 w-6 motion-safe:animate-spin motion-safe:[animation-duration:2s]" />
      </div>
      <h3 className="text-[18px] font-bold leading-normal text-[#F1F5F9]">
        공시 데이터를 동기화하는 중입니다
      </h3>
      <p className="max-w-[420px] text-[14px] font-normal leading-[21px] text-[#94A3B8]">
        최초 동기화가 아직 완료되지 않았습니다. 잠시 후 다시 확인해주세요.
      </p>
    </div>
  );
}
