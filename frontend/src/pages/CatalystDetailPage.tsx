import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { AppHeader } from '../components/layout/AppHeader'
import { CatalystDetailHeader } from '../components/catalyst-detail/CatalystDetailHeader'
import {
  CatalystDetailTabs,
  type CatalystDetailTab,
} from '../components/catalyst-detail/CatalystDetailTabs'
import { BasicInfoCard } from '../components/catalyst-detail/BasicInfoCard'
import { EditBasicInfoModal } from '../components/catalyst-detail/EditBasicInfoModal'
import { SearchConditionsCard } from '../components/catalyst-detail/SearchConditionsCard'
import { MonitoringExecCard } from '../components/catalyst-detail/MonitoringExecCard'
import { ManagementCard } from '../components/catalyst-detail/ManagementCard'
import { CatalystListSkeleton } from '../components/catalyst/CatalystListSkeleton'
import { useCatalystDetailQuery, useUpdateCatalystBasicInfoMutation } from '../hooks/useCatalysts'
import { formatDate, formatDateTime } from '../utils/date'
import type { UpdateCatalystBasicInfoFormValues } from '../schemas/catalyst'


function TabPlaceholder({ label }: { label: string }) {
  return (
    <div className="box-border flex w-full flex-col items-center justify-center gap-2 rounded-card border border-dashed border-border bg-bg-surface px-6 py-24 text-center">
      <p className="text-[14px] font-medium text-text-secondary">
        {label} 탭은 준비 중입니다.
      </p>
    </div>
  )
}

export function CatalystDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [activeTab, setActiveTab] = useState<CatalystDetailTab>('info')
  const [isEditModalOpen, setEditModalOpen] = useState(false)
  const catalystId = Number(id)
  const isInvalidId = !id || Number.isNaN(catalystId)

  const { data, isLoading, isError } = useCatalystDetailQuery(catalystId)
  const updateBasicInfoMutation = useUpdateCatalystBasicInfoMutation(catalystId)

  if (isInvalidId) {
    return (
      <div className="box-border flex min-h-screen w-full flex-col items-center bg-bg-base">
        <AppHeader />
        <main className="mx-auto box-border flex w-full max-w-[1280px] items-center justify-center px-8 py-24">
          <p className="text-[14px] font-medium text-text-secondary">
            카탈리스트를 찾을 수 없습니다.
          </p>
        </main>
      </div>
    )
  }

  const openEditModal = () => {
    updateBasicInfoMutation.reset();
    setEditModalOpen(true);
  };

  const handleUpdateBasicInfo = (values: UpdateCatalystBasicInfoFormValues) => {
    if (updateBasicInfoMutation.isPending) return;
    updateBasicInfoMutation.mutate(values, {
      onSuccess: () => setEditModalOpen(false),
    });
  };

  return (
    <div className="box-border flex min-h-screen w-full flex-col items-center bg-bg-base">
      <AppHeader />

      {isLoading ? (
        <main className="mx-auto box-border w-full max-w-[1280px] px-8 py-10">
          <CatalystListSkeleton />
        </main>
      ) : isError || !data ? (
        <main className="mx-auto box-border w-full max-w-[1280px] px-8 py-10">
          <div className="flex w-full flex-col items-center justify-center gap-2 py-24 text-center">
            <p className="text-[14px] font-medium leading-normal text-text-secondary">
              카탈리스트 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.
            </p>
          </div>
        </main>
      ) : (
        <>
          <CatalystDetailHeader
            id={catalystId}
            title={data.basicInfo.title}
            status={data.monitoringOperation.status}
            industryTags={data.basicInfo.industries}
          />

          <CatalystDetailTabs activeTab={activeTab} onChange={setActiveTab} />

          <main className="mx-auto box-border flex w-full max-w-[1280px] flex-col gap-5 px-8 py-8">
            {activeTab === 'timeline' && <TabPlaceholder label="타임라인" />}
            {activeTab === 'candidates' && (
              <TabPlaceholder label="모니터링 후보" />
            )}
            {activeTab === 'info' && (
              <>
                <BasicInfoCard
                  title={data.basicInfo.title}
                  content={data.basicInfo.content}
                  industryTags={data.basicInfo.industries}
                  createdAtLabel={formatDate(data.basicInfo.createdAt)}
                  updatedAtLabel={formatDateTime(data.basicInfo.updatedAt)}
                  onEditClick={openEditModal}
                />

                <div className="box-border flex w-full items-start gap-4">
                  <SearchConditionsCard
                    conditions={data.monitoringOperation.searchConditions}
                  />
                  <MonitoringExecCard
                    status={data.monitoringOperation.status}
                    intervalLabel={`${data.monitoringOperation.searchIntervalHours}시간`}
                    lastMonitoredAtLabel={
                      data.monitoringOperation.lastSearchedAt
                        ? formatDateTime(
                            data.monitoringOperation.lastSearchedAt
                          )
                        : '모니터링 이력 없음'
                    }
                  />
                </div>

                <ManagementCard
                  currentStatus={data.monitoringOperation.status}
                />

                <EditBasicInfoModal
                  open={isEditModalOpen}
                  initialTitle={data.basicInfo.title}
                  initialContent={data.basicInfo.content}
                  initialIndustryNames={data.basicInfo.industries}
                  onClose={() => setEditModalOpen(false)}
                  onSubmit={handleUpdateBasicInfo}
                  isSubmitting={updateBasicInfoMutation.isPending}
                  submitError={
                    updateBasicInfoMutation.isError
                      ? '수정에 실패했습니다.'
                      : undefined
                  }
                />
              </>
            )}
          </main>
        </>
      )}
    </div>
  )
}
