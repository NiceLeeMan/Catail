import type { QueryableSignalStatus } from '../../../types/signal';

const TABS: { key: QueryableSignalStatus; label: string }[] = [
  { key: 'PENDING', label: '검토 대기' },
  { key: 'EXCLUDED', label: '제외됨' },
];

interface SignalStatusTabsProps {
  activeStatus: QueryableSignalStatus;
  onChange: (status: QueryableSignalStatus) => void;
}

export function SignalStatusTabs({ activeStatus, onChange }: SignalStatusTabsProps) {
  return (
    <div className="box-border flex w-fit shrink-0 items-center gap-1 rounded-lg bg-[#0F1729] p-1">
      {TABS.map((tab) => {
        const isActive = tab.key === activeStatus;
        return (
          <button
            key={tab.key}
            type="button"
            onClick={() => onChange(tab.key)}
            aria-current={isActive ? 'true' : undefined}
            className={`box-border flex shrink-0 items-center rounded-md px-3 py-1.5 text-[14px] leading-normal transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399] focus-visible:ring-offset-2 focus-visible:ring-offset-[#0B1120] ${
              isActive
                ? 'bg-[#34D399] font-semibold text-[#0B1120]'
                : 'font-normal text-[#94A3B8] hover:text-[#F1F5F9]'
            }`}
          >
            {tab.label}
          </button>
        );
      })}
    </div>
  );
}
