import { useState } from 'react';
import { ArrowRight, ChevronDown, Trash2 } from 'lucide-react';
import { ConfirmDialog } from '../ConfirmDialog';
import { STATUS_META, type CatalystStatus } from '../../types/catalyst';

interface ManagementCardProps {
  currentStatus: CatalystStatus;
}

export function ManagementCard({ currentStatus }: ManagementCardProps) {
  const [isDeleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const meta = STATUS_META[currentStatus];

  return (
    <div className="box-border flex w-full flex-col items-start rounded-card border border-border bg-bg-surface">
      <div className="box-border flex w-full items-start px-6 pb-4 pt-5">
        <h2 className="text-[16px] font-bold text-text-primary">관리</h2>
      </div>

      <div className="h-px w-full bg-border" />

      <div className="box-border flex w-full items-start gap-10 px-6 py-5">
        <div className="box-border flex flex-1 flex-col items-start gap-3">
          <h3 className="text-[14px] font-semibold text-text-primary">모니터링 상태 변경</h3>
          <p className="text-[12px] font-normal text-text-muted">
            현재 상태를 변경하려면 목표 상태를 선택하세요.
          </p>

          <div className="box-border flex w-fit items-center gap-3">
            <div className="box-border flex shrink-0 items-center gap-2">
              <span className="whitespace-nowrap text-[13px] font-normal text-text-muted">현재 상태</span>
              <span
                className={`box-border w-fit rounded-full px-2.5 py-0.5 text-[12px] font-semibold ${meta.bg} ${meta.text}`}
              >
                {meta.label}
              </span>
            </div>

            <ArrowRight className="h-4 w-4 shrink-0 text-text-muted" />

            <div className="box-border flex w-[200px] shrink-0 items-center justify-between rounded-lg border border-border px-3.5 py-2.5">
              <span className="text-[13px] font-normal text-text-muted">목표 상태 선택</span>
              <ChevronDown className="h-4 w-4 text-text-muted" />
            </div>

            <button
              type="button"
              className="box-border flex shrink-0 items-center justify-center rounded-lg px-5 py-2.5 text-[13px] font-semibold text-white hover:opacity-90"
              style={{ backgroundImage: 'linear-gradient(90deg, #2E6BF2 0%, #00B89B 100%)' }}
            >
              변경
            </button>
          </div>
        </div>

        <div className="box-border flex flex-1 items-center justify-between gap-6 border-l border-border pl-6">
          <div className="box-border flex flex-1 flex-col items-start gap-1">
            <h3 className="whitespace-nowrap text-[13px] font-semibold text-text-primary">카탈리스트 삭제</h3>
            <p className="text-[12px] font-normal text-text-muted">
              카탈리스트를 삭제하면 목록과 상세 조회에서 제외됩니다.
            </p>
          </div>

          <button
            type="button"
            onClick={() => setDeleteDialogOpen(true)}
            className="box-border flex shrink-0 items-center gap-1.5 rounded-lg border border-status-ended px-5 py-2.5 text-[13px] font-semibold text-status-ended hover:bg-status-ended-bg"
          >
            <Trash2 className="h-3.5 w-3.5" />
            카탈리스트 삭제
          </button>
        </div>
      </div>

      <ConfirmDialog
        open={isDeleteDialogOpen}
        title="카탈리스트를 삭제하시겠습니까?"
        description="삭제하면 목록과 상세 조회에서 제외되며 되돌릴 수 없습니다."
        confirmLabel="삭제"
        cancelLabel="취소"
        onConfirm={() => setDeleteDialogOpen(false)}
        onCancel={() => setDeleteDialogOpen(false)}
      />
    </div>
  );
}
