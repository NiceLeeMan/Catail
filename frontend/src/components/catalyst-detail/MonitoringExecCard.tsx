import { useState } from 'react';
import type { ReactNode } from 'react';
import { ChevronDown, ChevronUp, Info } from 'lucide-react';
import { STATUS_META, type CatalystStatus } from '../../types/catalyst';

interface MonitoringExecCardProps {
  status: CatalystStatus;
  intervalLabel: string;
  lastMonitoredAtLabel: string;
}

function Row({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="box-border flex w-full items-center justify-between">
      <span className="text-[13px] font-normal text-text-muted">{label}</span>
      {children}
    </div>
  );
}

export function MonitoringExecCard({
  status,
  intervalLabel,
  lastMonitoredAtLabel,
}: MonitoringExecCardProps) {
  const [collapsed, setCollapsed] = useState(false);
  const meta = STATUS_META[status];

  return (
    <div className="box-border flex w-[420px] shrink-0 flex-col items-start rounded-card border border-border bg-bg-surface">
      <div className="box-border flex w-full items-center justify-between px-6 pb-4 pt-5">
        <h2 className="text-[16px] font-bold text-text-primary">모니터링 실행 정보</h2>
        <button
          type="button"
          onClick={() => setCollapsed((prev) => !prev)}
          className="box-border flex items-center gap-1 text-[12px] font-semibold text-text-muted hover:text-text-secondary"
        >
          {collapsed ? '펼치기' : '접기'}
          {collapsed ? <ChevronDown className="h-3.5 w-3.5" /> : <ChevronUp className="h-3.5 w-3.5" />}
        </button>
      </div>

      <div className="h-px w-full bg-border" />

      {!collapsed && (
        <>
          <div className="box-border flex w-full flex-col items-start gap-3.5 px-6 py-4">
            <Row label="모니터링 상태">
              <span
                className={`box-border w-fit rounded-full px-2.5 py-0.5 text-[12px] font-semibold ${meta.bg} ${meta.text}`}
              >
                {meta.label}
              </span>
            </Row>
            <Row label="탐색 주기">
              <span className="text-[14px] font-semibold text-text-primary">{intervalLabel}</span>
            </Row>
            <Row label="최근 모니터링 일시">
              <span className="text-[14px] font-semibold text-text-primary">{lastMonitoredAtLabel}</span>
            </Row>
          </div>

          <div className="box-border w-full px-6 pb-5">
            <div className="box-border flex w-full items-center gap-2 rounded-lg bg-accent-soft px-3.5 py-2.5">
              <Info className="h-3.5 w-3.5 shrink-0 text-accent" />
              <span className="flex-1 text-[12px] font-normal leading-[17px] text-text-secondary">
                시그널 수집은 설정한 탐색 주기에 따라 자동으로 실행됩니다.
              </span>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
