import { Bell, Timer } from 'lucide-react';
import { STATUS_META, type CatalystStatus } from '../../types/catalyst';

interface CatalystCardProps {
  id: number;
  title: string;
  status: CatalystStatus;
  industryTags: string[];
  pendingSignalCount: number;
  lastMonitoredAtLabel: string | null;
  onClick: (id: number) => void;
}

function StatusBadge({ status }: { status: CatalystStatus }) {
  const meta = STATUS_META[status];
  return (
    <span
      className={`box-border shrink-0 whitespace-nowrap rounded-full px-3 py-1 text-[12px] font-semibold leading-normal ${meta.bg} ${meta.text}`}
    >
      {meta.label}
    </span>
  );
}

function CardTop({ title, status }: { title: string; status: CatalystStatus }) {
  return (
    <div className="box-border flex w-full items-start justify-between gap-3">
      <h3 className="line-clamp-2 flex-1 text-left text-[18px] font-bold leading-[26px] text-text-primary">
        {title}
      </h3>
      <StatusBadge status={status} />
    </div>
  );
}

function IndustryTags({ tags }: { tags: string[] }) {
  return (
    <div className="box-border flex w-full flex-wrap items-center gap-2">
      {tags.map((tag) => (
        <span
          key={tag}
          className="box-border shrink-0 whitespace-nowrap rounded-full bg-primary-soft px-3 py-1 text-[12px] font-medium leading-normal text-primary"
        >
          {tag}
        </span>
      ))}
    </div>
  );
}

function CardBottom({
  pendingCount,
  lastMonitoredAtLabel,
}: {
  pendingCount: number;
  lastMonitoredAtLabel: string | null;
}) {
  return (
    <div className="box-border flex w-full items-center justify-between border-t border-border pt-4">
      <div className="box-border flex items-center gap-2 text-text-secondary">
        <Bell className="h-4 w-4" />
        <span className="text-[13px] font-medium leading-normal">
          미검토 후보 {pendingCount}건
        </span>
      </div>
      <div className="box-border flex items-center gap-2 text-text-muted">
        <Timer className="h-4 w-4" />
        <span className="text-[13px] font-medium leading-normal">
          {lastMonitoredAtLabel ?? '모니터링 이력 없음'}
        </span>
      </div>
    </div>
  );
}

export function CatalystCard({
  id,
  title,
  status,
  industryTags,
  pendingSignalCount,
  lastMonitoredAtLabel,
  onClick,
}: CatalystCardProps) {
  return (
    <button
      type="button"
      onClick={() => onClick(id)}
      className="box-border flex w-full flex-col items-start gap-4 rounded-card border border-border bg-bg-surface p-6 text-left shadow-card transition-shadow hover:shadow-lg"
    >
      <CardTop title={title} status={status} />
      <IndustryTags tags={industryTags} />
      <CardBottom pendingCount={pendingSignalCount} lastMonitoredAtLabel={lastMonitoredAtLabel} />
    </button>
  );
}
