import { useEffect, useRef, useState } from 'react';
import { ArrowRight, Check, ChevronDown, Trash2 } from 'lucide-react';
import { ConfirmDialog } from '../ConfirmDialog';
import { STATUS_META, TARGET_STATUS_OPTIONS, type CatalystStatus } from '../../types/catalyst';

interface ManagementCardProps {
  currentStatus: CatalystStatus;
  onChangeStatus: (targetStatus: CatalystStatus) => void;
  isChangingStatus: boolean;
  changeStatusError?: string;
  onDelete: () => void;
  isDeleting: boolean;
  deleteError?: string;
}

export function ManagementCard({
  currentStatus,
  onChangeStatus,
  isChangingStatus,
  changeStatusError,
  onDelete,
  isDeleting,
  deleteError,
}: ManagementCardProps) {
  const [isDeleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [isDropdownOpen, setDropdownOpen] = useState(false);
  const [targetStatus, setTargetStatus] = useState<CatalystStatus | null>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const meta = STATUS_META[currentStatus];
  const targetOptions = TARGET_STATUS_OPTIONS.filter((status) => status !== currentStatus);

  // 상태변경 성공 시 monitoringOperation이 refetch되어 currentStatus가 갱신된다.
  // 선택해둔 목표 상태가 새 현재 상태와 같아지면(=전이 완료) 선택을 비운 것으로 취급한다.
  const selectedTargetStatus = targetStatus === currentStatus ? null : targetStatus;

  useEffect(() => {
    if (!isDropdownOpen) return;

    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setDropdownOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isDropdownOpen]);

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

            <div ref={dropdownRef} className="relative box-border w-[200px] shrink-0">
              <button
                type="button"
                onClick={() => setDropdownOpen((prev) => !prev)}
                className="box-border flex w-full items-center justify-between rounded-lg border border-border px-3.5 py-2.5 hover:bg-bg-base"
              >
                {selectedTargetStatus ? (
                  <span
                    className={`box-border w-fit rounded-full px-2.5 py-0.5 text-[12px] font-semibold ${STATUS_META[selectedTargetStatus].bg} ${STATUS_META[selectedTargetStatus].text}`}
                  >
                    {STATUS_META[selectedTargetStatus].label}
                  </span>
                ) : (
                  <span className="text-[13px] font-normal text-text-muted">목표 상태 선택</span>
                )}
                <ChevronDown className="h-4 w-4 shrink-0 text-text-muted" />
              </button>

              {isDropdownOpen && (
                <div className="absolute left-0 top-[calc(100%+6px)] z-10 box-border flex w-full flex-col items-start overflow-hidden rounded-lg border border-border bg-bg-surface shadow-card">
                  {targetOptions.map((status) => {
                    const optionMeta = STATUS_META[status];
                    return (
                      <button
                        key={status}
                        type="button"
                        onClick={() => {
                          setTargetStatus(status);
                          setDropdownOpen(false);
                        }}
                        className="box-border flex w-full items-center justify-between px-3.5 py-2.5 hover:bg-bg-base"
                      >
                        <span
                          className={`box-border w-fit rounded-full px-2.5 py-0.5 text-[12px] font-semibold ${optionMeta.bg} ${optionMeta.text}`}
                        >
                          {optionMeta.label}
                        </span>
                        {status === selectedTargetStatus && <Check className="h-4 w-4 text-primary" />}
                      </button>
                    );
                  })}
                </div>
              )}
            </div>

            <button
              type="button"
              disabled={!selectedTargetStatus || isChangingStatus}
              onClick={() => selectedTargetStatus && onChangeStatus(selectedTargetStatus)}
              className="box-border flex shrink-0 items-center justify-center rounded-lg px-5 py-2.5 text-[13px] font-semibold text-white hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
              style={{ backgroundImage: 'linear-gradient(90deg, #2E6BF2 0%, #00B89B 100%)' }}
            >
              {isChangingStatus ? '변경 중...' : '변경'}
            </button>
          </div>

          {changeStatusError && (
            <p className="text-[12px] font-medium text-status-ended">{changeStatusError}</p>
          )}
        </div>

        <div className="box-border flex flex-1 flex-col items-start gap-2 border-l border-border pl-6">
          <div className="box-border flex w-full items-center justify-between gap-6">
            <div className="box-border flex flex-1 flex-col items-start gap-1">
              <h3 className="whitespace-nowrap text-[13px] font-semibold text-text-primary">카탈리스트 삭제</h3>
              <p className="text-[12px] font-normal text-text-muted">
                카탈리스트를 삭제하면 목록과 상세 조회에서 제외됩니다.
              </p>
            </div>

            <button
              type="button"
              disabled={isDeleting}
              onClick={() => setDeleteDialogOpen(true)}
              className="box-border flex shrink-0 items-center gap-1.5 rounded-lg border border-status-ended px-5 py-2.5 text-[13px] font-semibold text-status-ended hover:bg-status-ended-bg disabled:cursor-not-allowed disabled:opacity-50"
            >
              <Trash2 className="h-3.5 w-3.5" />
              {isDeleting ? '삭제 중...' : '카탈리스트 삭제'}
            </button>
          </div>

          {deleteError && <p className="text-[12px] font-medium text-status-ended">{deleteError}</p>}
        </div>
      </div>

      <ConfirmDialog
        open={isDeleteDialogOpen}
        title="카탈리스트를 삭제하시겠습니까?"
        description="삭제하면 목록과 상세 조회에서 제외되며 되돌릴 수 없습니다."
        confirmLabel="삭제"
        cancelLabel="취소"
        onConfirm={() => {
          setDeleteDialogOpen(false);
          onDelete();
        }}
        onCancel={() => setDeleteDialogOpen(false)}
      />
    </div>
  );
}
