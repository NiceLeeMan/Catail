import { FileX } from 'lucide-react';

export function DisclosureEmptyState() {
  return (
    <div className="box-border flex w-full flex-col items-center justify-center gap-4 rounded-2xl border border-dashed border-white/[0.08] bg-[#131B2E] px-6 py-24 text-center">
      <div className="box-border flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-[#0F1729] text-[#34D399]">
        <FileX className="h-6 w-6" />
      </div>
      <h3 className="text-[18px] font-bold leading-normal text-[#F1F5F9]">등록된 공시가 없습니다</h3>
      <p className="max-w-[420px] text-[14px] font-normal leading-[21px] text-[#94A3B8]">
        아직 조회 가능한 공시 내역이 없습니다.
      </p>
    </div>
  );
}
