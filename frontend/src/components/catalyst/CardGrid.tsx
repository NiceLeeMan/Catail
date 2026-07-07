import { CatalystCard } from './CatalystCard';
import type { CatalystListItemResponse } from '../../types/catalyst';

function toDateLabel(createdAt: string): string {
  const date = new Date(createdAt);
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  return `${yyyy}.${mm}.${dd}`;
}

interface CardGridProps {
  catalysts: CatalystListItemResponse[];
  onCardClick: (id: number) => void;
}

export function CardGrid({ catalysts, onCardClick }: CardGridProps) {
  return (
    <div className="grid w-full grid-cols-2 gap-6">
      {catalysts.map((catalyst) => (
        <CatalystCard
          key={catalyst.id}
          id={catalyst.id}
          title={catalyst.title}
          status={catalyst.status}
          industryTags={catalyst.industryTags}
          pendingSignalCount={catalyst.pendingSignalCount}
          lastMonitoredAtLabel={toDateLabel(catalyst.createdAt)}
          onClick={onCardClick}
        />
      ))}
    </div>
  );
}
