import type { CatalystCategory, CatalystStatus } from '../../../types/catalyst';
import { CATALYST_CATEGORY_LABELS, CATALYST_STATUS_LABELS } from '../../../utils/catalyst';

const STATUS_STYLES: Record<CatalystStatus, string> = {
  ACTIVE: 'bg-[#10B981]/10 text-[#34D399]',
  PAUSED: 'bg-[#F59E0B]/10 text-[#F59E0B]',
  INACTIVE: 'bg-white/[0.06] text-[#64748B]',
};

export function CatalystStatusBadge({ status }: { status: CatalystStatus }) {
  return (
    <span
      className={`inline-flex w-fit shrink-0 items-center gap-1.5 rounded-full px-2.5 py-1 text-[12px] font-semibold leading-normal ${STATUS_STYLES[status]}`}
    >
      <span className="h-1.5 w-1.5 shrink-0 rounded-full bg-current" aria-hidden="true" />
      {CATALYST_STATUS_LABELS[status]}
    </span>
  );
}

export function CatalystCategoryBadge({ category }: { category: CatalystCategory }) {
  return (
    <span className="inline-flex w-fit shrink-0 items-center rounded-full border border-white/[0.08] bg-[#0F1729] px-2.5 py-1 text-[12px] font-medium leading-normal text-[#94A3B8]">
      {CATALYST_CATEGORY_LABELS[category]}
    </span>
  );
}
