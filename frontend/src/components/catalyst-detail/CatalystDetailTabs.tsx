export type CatalystDetailTab = 'timeline' | 'candidates' | 'info';

const TABS: { key: CatalystDetailTab; label: string }[] = [
  { key: 'timeline', label: '타임라인' },
  { key: 'candidates', label: '모니터링 후보' },
  { key: 'info', label: '카탈리스트 정보' },
];

interface CatalystDetailTabsProps {
  activeTab: CatalystDetailTab;
  onChange: (tab: CatalystDetailTab) => void;
}

export function CatalystDetailTabs({ activeTab, onChange }: CatalystDetailTabsProps) {
  return (
    <div className="box-border w-full border-b border-border bg-bg-surface">
      <div className="mx-auto box-border flex w-full max-w-[1280px] items-end gap-8 px-8">
        {TABS.map((tab) => {
          const isActive = tab.key === activeTab;
          return (
            <button
              key={tab.key}
              type="button"
              onClick={() => onChange(tab.key)}
              className={`box-border flex h-11 items-center justify-center border-b-2 text-[14px] ${
                isActive
                  ? 'border-primary font-semibold text-primary'
                  : 'border-transparent font-normal text-text-muted hover:text-text-secondary'
              }`}
            >
              {tab.label}
            </button>
          );
        })}
      </div>
    </div>
  );
}
