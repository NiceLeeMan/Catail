interface MonitoringOptionCardProps {
  value: 'ACTIVE' | 'INACTIVE';
  onChange: (status: 'ACTIVE' | 'INACTIVE') => void;
}

const OPTIONS: { value: 'ACTIVE' | 'INACTIVE'; title: string; description: string }[] = [
  { value: 'ACTIVE', title: '바로 시작', description: '검색 키워드 생성 및 모니터링을 즉시 시작합니다.' },
  { value: 'INACTIVE', title: '나중에 시작', description: '카탈리스트만 생성하고 모니터링은 나중에 시작합니다.' },
];

export function MonitoringOptionCard({ value, onChange }: MonitoringOptionCardProps) {
  return (
    <div className="box-border flex w-full flex-col gap-2">
      <label className="text-[14px] font-semibold leading-normal text-text-primary">
        모니터링 시작 여부 <span className="text-status-ended">*</span>
      </label>
      <p className="text-[13px] font-normal leading-normal text-text-secondary">
        생성 후 바로 모니터링을 시작할지 선택하세요.
      </p>

      <div className="box-border grid w-full grid-cols-1 gap-3 sm:grid-cols-2">
        {OPTIONS.map((option) => {
          const selected = value === option.value;
          return (
            <button
              key={option.value}
              type="button"
              onClick={() => onChange(option.value)}
              className={`box-border flex flex-col items-start gap-1 rounded-card border px-4 py-3 text-left shadow-card ${
                selected ? 'border-primary bg-primary-soft' : 'border-border bg-bg-surface'
              }`}
            >
              <span className="text-[14px] font-semibold leading-normal text-text-primary">
                {option.title}
              </span>
              <span className="text-[13px] font-normal leading-normal text-text-secondary">
                {option.description}
              </span>
            </button>
          );
        })}
      </div>

      {value === 'ACTIVE' && (
        <p className="text-[13px] font-medium leading-normal text-status-ended">
          ⚠ 생성 즉시 뉴스 수집이 시작됩니다.
        </p>
      )}
    </div>
  );
}
