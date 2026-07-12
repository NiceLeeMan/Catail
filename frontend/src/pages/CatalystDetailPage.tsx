import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { AppHeader } from '../components/layout/AppHeader';
import { CatalystDetailHeader } from '../components/catalyst-detail/CatalystDetailHeader';
import {
  CatalystDetailTabs,
  type CatalystDetailTab,
} from '../components/catalyst-detail/CatalystDetailTabs';
import { BasicInfoCard } from '../components/catalyst-detail/BasicInfoCard';
import { SearchConditionsCard } from '../components/catalyst-detail/SearchConditionsCard';
import { MonitoringExecCard } from '../components/catalyst-detail/MonitoringExecCard';
import { ManagementCard } from '../components/catalyst-detail/ManagementCard';
import { getMockCatalystDetail } from '../mocks/catalystDetail';

function TabPlaceholder({ label }: { label: string }) {
  return (
    <div className="box-border flex w-full flex-col items-center justify-center gap-2 rounded-card border border-dashed border-border bg-bg-surface px-6 py-24 text-center">
      <p className="text-[14px] font-medium text-text-secondary">{label} 탭은 준비 중입니다.</p>
    </div>
  );
}

export function CatalystDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [activeTab, setActiveTab] = useState<CatalystDetailTab>('info');
  const catalystId = Number(id);

  if (!id || Number.isNaN(catalystId)) {
    return (
      <div className="box-border flex min-h-screen w-full flex-col items-center bg-bg-base">
        <AppHeader />
        <main className="mx-auto box-border flex w-full max-w-[1280px] items-center justify-center px-8 py-24">
          <p className="text-[14px] font-medium text-text-secondary">카탈리스트를 찾을 수 없습니다.</p>
        </main>
      </div>
    );
  }

  const detail = getMockCatalystDetail(catalystId);

  return (
    <div className="box-border flex min-h-screen w-full flex-col items-center bg-bg-base">
      <AppHeader />

      <CatalystDetailHeader
        id={detail.id}
        title={detail.title}
        status={detail.status}
        industryTags={detail.industryTags}
      />

      <CatalystDetailTabs activeTab={activeTab} onChange={setActiveTab} />

      <main className="mx-auto box-border flex w-full max-w-[1280px] flex-col gap-5 px-8 py-8">
        {activeTab === 'timeline' && <TabPlaceholder label="타임라인" />}
        {activeTab === 'candidates' && <TabPlaceholder label="모니터링 후보" />}
        {activeTab === 'info' && (
          <>
            <BasicInfoCard
              title={detail.title}
              content={detail.content}
              industryTags={detail.industryTags}
              createdAtLabel={detail.createdAtLabel}
              updatedAtLabel={detail.updatedAtLabel}
            />

            <div className="box-border flex w-full items-start gap-4">
              <SearchConditionsCard conditions={detail.searchConditions} />
              <MonitoringExecCard
                status={detail.monitoring.status}
                intervalLabel={detail.monitoring.intervalLabel}
                lastMonitoredAtLabel={detail.monitoring.lastMonitoredAtLabel}
              />
            </div>

            <ManagementCard currentStatus={detail.status} />
          </>
        )}
      </main>
    </div>
  );
}
