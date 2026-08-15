import { Inbox } from 'lucide-react';
import type { QueryableSignalStatus } from '../../../types/signal';

const EMPTY_TEXT: Record<QueryableSignalStatus, { title: string; description: string }> = {
  PENDING: {
    title: '검토 대기 중인 시그널이 없습니다',
    description: '새로운 뉴스가 수집되면 여기에서 검토할 수 있어요.',
  },
  EXCLUDED: {
    title: '제외된 시그널이 없습니다',
    description: '제외 처리한 시그널이 이곳에 모여요.',
  },
};

interface SignalEmptyStateProps {
  status: QueryableSignalStatus;
}

export function SignalEmptyState({ status }: SignalEmptyStateProps) {
  const { title, description } = EMPTY_TEXT[status];

  return (
    <div className="box-border flex w-full flex-col items-center justify-center gap-4 rounded-2xl border border-dashed border-white/[0.08] bg-[#131B2E] px-6 py-24 text-center">
      <div className="box-border flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-[#0F1729] text-[#34D399]">
        <Inbox className="h-6 w-6" />
      </div>
      <h3 className="text-[18px] font-bold leading-normal text-[#F1F5F9]">{title}</h3>
      <p className="max-w-[420px] text-[14px] font-normal leading-[21px] text-[#94A3B8]">
        {description}
      </p>
    </div>
  );
}
