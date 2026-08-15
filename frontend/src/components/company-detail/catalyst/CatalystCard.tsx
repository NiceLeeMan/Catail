import { Pause, Pencil, Play, Trash2 } from 'lucide-react';
import type { CatalystListItemResponse } from '../../../types/catalyst';
import { getCatalystStatusAction } from '../../../utils/catalyst';
import { formatDate } from '../../../utils/date';
import { CatalystCategoryBadge, CatalystStatusBadge } from './CatalystBadges';

interface CatalystCardProps {
  catalyst: CatalystListItemResponse;
  isStatusChanging: boolean;
  onChangeStatus: () => void;
  onEdit: () => void;
  onDelete: () => void;
}

const ACTION_BUTTON =
  'box-border flex cursor-pointer items-center gap-1.5 rounded-lg px-2.5 py-1.5 text-[13px] font-medium leading-normal text-[#94A3B8] transition-colors duration-150 hover:bg-[#182338] hover:text-[#F1F5F9] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399] disabled:cursor-not-allowed disabled:opacity-40';

export function CatalystCard({
  catalyst,
  isStatusChanging,
  onChangeStatus,
  onEdit,
  onDelete,
}: CatalystCardProps) {
  const statusAction = getCatalystStatusAction(catalyst.status);
  const StatusIcon = statusAction?.target === 'PAUSED' ? Pause : Play;

  return (
    <div className="box-border flex w-full flex-col gap-4 rounded-2xl border border-white/[0.08] bg-[#131B2E] p-5">
      <div className="flex items-start justify-between gap-3">
        <div className="flex flex-col gap-2">
          <h3 className="text-[15px] font-bold leading-normal text-[#F1F5F9]">{catalyst.title}</h3>
          <div className="flex items-center gap-1.5">
            <CatalystCategoryBadge category={catalyst.category} />
            <CatalystStatusBadge status={catalyst.status} />
          </div>
        </div>
      </div>

      <p className="text-[14px] font-normal leading-[21px] text-[#94A3B8]">{catalyst.detail}</p>

      <div className="flex items-center justify-between gap-2 border-t border-white/[0.08] pt-3">
        <span className="text-[12px] font-normal leading-normal text-[#64748B]">
          {formatDate(catalyst.createdAt)} 등록
        </span>
        <div className="flex items-center gap-1">
          {statusAction && (
            <button
              type="button"
              onClick={onChangeStatus}
              disabled={isStatusChanging}
              className={ACTION_BUTTON}
            >
              <StatusIcon className="h-3.5 w-3.5" aria-hidden="true" />
              {statusAction.label}
            </button>
          )}
          <button type="button" onClick={onEdit} className={ACTION_BUTTON}>
            <Pencil className="h-3.5 w-3.5" aria-hidden="true" />
            수정
          </button>
          <button
            type="button"
            onClick={onDelete}
            className={`${ACTION_BUTTON} hover:!bg-[#EF4444]/10 hover:!text-[#EF4444]`}
          >
            <Trash2 className="h-3.5 w-3.5" aria-hidden="true" />
            삭제
          </button>
        </div>
      </div>
    </div>
  );
}
