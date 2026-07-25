import type { Market } from '../../types/company';

const TABS: { key: Market; label: string }[] = [
  { key: 'KOSPI', label: '코스피' },
  { key: 'NASDAQ', label: '나스닥' },
];

interface CompanyMarketTabsProps {
  activeMarket: Market;
  onChange: (market: Market) => void;
}

export function CompanyMarketTabs({ activeMarket, onChange }: CompanyMarketTabsProps) {
  return (
    <div className="box-border flex w-fit shrink-0 items-center gap-1 rounded-lg bg-dark-bg-badge p-1">
      {TABS.map((tab) => {
        const isActive = tab.key === activeMarket;
        return (
          <button
            key={tab.key}
            type="button"
            onClick={() => onChange(tab.key)}
            className={`box-border flex shrink-0 items-center rounded-md px-3 py-1.5 text-[14px] leading-normal transition-colors duration-150 ${
              isActive
                ? 'bg-dark-accent font-semibold text-dark-bg-base'
                : 'font-normal text-dark-text-secondary hover:text-dark-text-primary'
            }`}
          >
            {tab.label}
          </button>
        );
      })}
    </div>
  );
}
