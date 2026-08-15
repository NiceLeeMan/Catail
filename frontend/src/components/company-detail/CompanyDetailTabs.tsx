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
    <div className="box-border flex w-full shrink-0 items-start gap-8 border-b border-white/[0.08]">
      {TABS.map((tab) => {
        const isActive = tab.key === activeTab;
        return (
          <button
            key={tab.key}
            type="button"
            disabled={!tab.enabled}
            onClick={() => onChange(tab.key)}
            className={`box-border flex shrink-0 items-start gap-2.5 border-b-2 pb-3 text-[16px] leading-normal transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399] focus-visible:ring-offset-2 focus-visible:ring-offset-[#0B1120] ${
              isActive
                ? 'border-[#34D399] font-bold text-[#34D399]'
                : 'border-transparent font-normal text-[#94A3B8]'
            } ${tab.enabled ? 'hover:text-[#F1F5F9]' : 'cursor-not-allowed opacity-50'}`}
          >
            {tab.label}
          </button>
        );
      })}
    </div>
  );
}
