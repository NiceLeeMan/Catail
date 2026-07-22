import { RefreshCw } from 'lucide-react';

export function DisclosureSyncPendingState() {
  return (
    <div className="box-border flex w-full flex-col items-center justify-center gap-4 rounded-xl border border-dashed border-dark-border bg-dark-bg-card px-6 py-24 text-center">
      <div className="box-border flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-dark-bg-badge text-dark-accent">
        <RefreshCw className="h-6 w-6" />
      </div>
      <h3 className="text-[18px] font-bold leading-normal text-dark-text-primary">
        공시 데이터를 동기화하는 중입니다
      </h3>
      <p className="max-w-[420px] text-[14px] font-normal leading-[21px] text-dark-text-secondary">
        최초 동기화가 아직 완료되지 않았습니다. 잠시 후 다시 확인해주세요.
      </p>
    </div>
  );
}
