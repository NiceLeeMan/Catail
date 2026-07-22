import { useState } from 'react';
import { ArrowLeft } from 'lucide-react';
import { useNavigate, useParams } from 'react-router-dom';
import { CompanyPageHeader } from '../components/company/CompanyPageHeader';
import { CompanyListSkeleton } from '../components/company/CompanyListSkeleton';
import { CompanyDetailHeader } from '../components/company-detail/CompanyDetailHeader';
import {
  CompanyDetailTabs,
  type CompanyDetailTab,
} from '../components/company-detail/CompanyDetailTabs';
import { DisclosureTable } from '../components/company-detail/DisclosureTable';
import { DisclosureEmptyState } from '../components/company-detail/DisclosureEmptyState';
import { DisclosureSyncPendingState } from '../components/company-detail/DisclosureSyncPendingState';
import { DisclosureListError } from '../components/company-detail/DisclosureListError';
import { LoadMoreButton } from '../components/company-detail/LoadMoreButton';
import { useCompanyDetailQuery } from '../hooks/useCompanies';
import { useDisclosuresQuery } from '../hooks/useDisclosures';
import { formatDateTime } from '../utils/date';

export function CompanyDetailPage() {
  const { companyId } = useParams<{ companyId: string }>();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<CompanyDetailTab>('disclosure');

  const id = Number(companyId);
  const isInvalidId = !companyId || Number.isNaN(id);

  const companyQuery = useCompanyDetailQuery(id);
  const disclosuresQuery = useDisclosuresQuery(id);

  if (isInvalidId) {
    return (
      <div className="box-border flex min-h-screen w-full flex-col items-center bg-dark-bg-base">
        <CompanyPageHeader />
        <main className="mx-auto box-border flex w-full max-w-[1280px] items-center justify-center px-8 py-24">
          <p className="text-[14px] font-medium leading-normal text-dark-text-secondary">
            기업을 찾을 수 없습니다.
          </p>
        </main>
      </div>
    );
  }

  const disclosurePages = disclosuresQuery.data?.pages ?? [];
  const disclosureItems = disclosurePages.flatMap((page) => page.items);
  const lastDisclosurePage = disclosurePages.at(-1);

  return (
    <div className="box-border flex min-h-screen w-full flex-col items-center bg-dark-bg-base">
      <CompanyPageHeader />

      <main className="mx-auto box-border flex w-full max-w-[1280px] flex-col gap-5 px-8 py-9">
        <button
          type="button"
          onClick={() => navigate('/companies')}
          className="box-border flex w-fit shrink-0 items-center gap-1.5 text-[14px] font-normal leading-normal text-dark-text-secondary hover:text-dark-text-primary"
        >
          <ArrowLeft className="h-4 w-4" />
          기업 목록으로
        </button>

        {companyQuery.isLoading ? (
          <CompanyListSkeleton />
        ) : companyQuery.isError || !companyQuery.data ? (
          <div className="flex w-full flex-col items-center justify-center gap-2 py-24 text-center">
            <p className="text-[14px] font-medium leading-normal text-dark-text-secondary">
              기업 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.
            </p>
          </div>
        ) : (
          <>
            <CompanyDetailHeader company={companyQuery.data} />

            <CompanyDetailTabs activeTab={activeTab} onChange={setActiveTab} />

            {activeTab === 'disclosure' && (
              <>
                {disclosuresQuery.isLoading ? (
                  <CompanyListSkeleton />
                ) : disclosuresQuery.isError ? (
                  <DisclosureListError />
                ) : disclosureItems.length === 0 ? (
                  lastDisclosurePage?.initialSyncCompleted ? (
                    <DisclosureEmptyState />
                  ) : (
                    <DisclosureSyncPendingState />
                  )
                ) : (
                  <>
                    <DisclosureTable items={disclosureItems} />
                    <LoadMoreButton
                      visible={disclosuresQuery.hasNextPage}
                      isLoading={disclosuresQuery.isFetchingNextPage}
                      onClick={() => disclosuresQuery.fetchNextPage()}
                    />
                  </>
                )}

                {lastDisclosurePage?.lastSyncedAt && (
                  <p className="text-[13px] font-normal leading-normal text-dark-text-muted">
                    마지막 동기화: {formatDateTime(lastDisclosurePage.lastSyncedAt)}
                  </p>
                )}
              </>
            )}
          </>
        )}
      </main>
    </div>
  );
}
