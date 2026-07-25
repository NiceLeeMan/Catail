export type CompanyDetailTab = 'disclosure' | 'catalyst' | 'timeline' | 'signal';

const TABS: { key: CompanyDetailTab; label: string; enabled: boolean }[] = [
  { key: 'disclosure', label: '공시', enabled: true },
  { key: 'catalyst', label: '카탈리스트', enabled: false },
  { key: 'timeline', label: '타임라인', enabled: false },
  { key: 'signal', label: '시그널', enabled: false },
];

interface CompanyDetailTabsProps {
  activeTab: CompanyDetailTab;
  onChange: (tab: CompanyDetailTab) => void;
}

export function CompanyDetailTabs({ activeTab, onChange }: CompanyDetailTabsProps) {
  return (
    <div className="box-border flex w-full shrink-0 items-start gap-8 border-b border-dark-border">
      {TABS.map((tab) => {
        const isActive = tab.key === activeTab;
        return (
          <button
            key={tab.key}
            type="button"
            disabled={!tab.enabled}
            onClick={() => onChange(tab.key)}
            className={`box-border flex shrink-0 items-start gap-2.5 border-b-2 pb-3 text-[16px] leading-normal ${
              isActive
                ? 'border-dark-accent font-bold text-dark-accent'
                : 'border-transparent font-normal text-dark-text-secondary'
            } ${tab.enabled ? 'hover:text-dark-text-primary' : 'cursor-not-allowed opacity-50'}`}
          >
            {tab.label}
          </button>
        );
      })}
    </div>
  );
}
