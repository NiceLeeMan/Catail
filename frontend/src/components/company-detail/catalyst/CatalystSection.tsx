import { Plus } from 'lucide-react';
import { useState } from 'react';
import { ConfirmDialog } from '../../ConfirmDialog';
import type { CatalystListItemResponse } from '../../../types/catalyst';
import {
  useChangeCatalystStatusMutation,
  useCatalystsQuery,
  useCreateCatalystMutation,
  useDeleteCatalystMutation,
  useUpdateCatalystMutation,
} from '../../../hooks/useCatalysts';
import { CATALYST_MAX_COUNT, getCatalystStatusAction } from '../../../utils/catalyst';
import { getApiErrorMessage } from '../../../utils/apiError';
import { CatalystCard } from './CatalystCard';
import { CatalystEmptyState } from './CatalystEmptyState';
import { CatalystListError } from './CatalystListError';
import { CatalystListSkeleton } from './CatalystListSkeleton';
import { CatalystFormModal, type CatalystFormValues } from './CatalystFormModal';

type ModalState = { mode: 'create' } | { mode: 'edit'; catalyst: CatalystListItemResponse };

interface CatalystSectionProps {
  companyId: number;
}

export function CatalystSection({ companyId }: CatalystSectionProps) {
  const { data, isLoading, isError, refetch } = useCatalystsQuery(companyId);
  const createMutation = useCreateCatalystMutation(companyId);
  const updateMutation = useUpdateCatalystMutation(companyId);
  const statusMutation = useChangeCatalystStatusMutation(companyId);
  const deleteMutation = useDeleteCatalystMutation(companyId);

  const [modalState, setModalState] = useState<ModalState | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<CatalystListItemResponse | null>(null);

  const catalysts = data?.catalysts ?? [];
  const atLimit = catalysts.length >= CATALYST_MAX_COUNT;

  const activeMutation = modalState?.mode === 'edit' ? updateMutation : createMutation;
  const modalErrorMessage = activeMutation.isError
    ? getApiErrorMessage(activeMutation.error, '요청을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.')
    : null;

  const handleFormSubmit = (values: CatalystFormValues) => {
    if (modalState?.mode === 'edit') {
      updateMutation.mutate(
        {
          catalystId: modalState.catalyst.catalystId,
          payload: { category: values.category, detail: values.detail },
        },
        { onSuccess: () => setModalState(null) }
      );
      return;
    }

    createMutation.mutate(
      { companyId, category: values.category, detail: values.detail, status: values.status },
      { onSuccess: () => setModalState(null) }
    );
  };

  const handleChangeStatus = (catalyst: CatalystListItemResponse) => {
    const action = getCatalystStatusAction(catalyst.status);
    if (!action) return;
    statusMutation.mutate({ catalystId: catalyst.catalystId, payload: { status: action.target } });
  };

  const handleConfirmDelete = () => {
    if (!deleteTarget) return;
    deleteMutation.mutate(deleteTarget.catalystId, { onSuccess: () => setDeleteTarget(null) });
  };

  return (
    <div className="flex w-full flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-[13px] font-normal leading-normal text-[#64748B]">
          최대 {CATALYST_MAX_COUNT}개까지 등록할 수 있어요 · {catalysts.length}/{CATALYST_MAX_COUNT}
        </p>
        <button
          type="button"
          onClick={() => setModalState({ mode: 'create' })}
          disabled={atLimit}
          className="box-border flex shrink-0 cursor-pointer items-center gap-1.5 rounded-lg bg-[#34D399] px-3.5 py-2 text-[13px] font-semibold leading-normal text-[#0B1120] transition-colors duration-150 hover:bg-[#6EE7B7] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399] focus-visible:ring-offset-2 focus-visible:ring-offset-[#0B1120] disabled:cursor-not-allowed disabled:bg-white/[0.08] disabled:text-[#64748B]"
        >
          <Plus className="h-4 w-4" aria-hidden="true" />
          새 카탈리스트
        </button>
      </div>

      {isLoading ? (
        <CatalystListSkeleton />
      ) : isError ? (
        <CatalystListError onRetry={() => refetch()} />
      ) : catalysts.length === 0 ? (
        <CatalystEmptyState />
      ) : (
        <div className="grid w-full grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {catalysts.map((catalyst) => (
            <CatalystCard
              key={catalyst.catalystId}
              catalyst={catalyst}
              isStatusChanging={
                statusMutation.isPending &&
                statusMutation.variables?.catalystId === catalyst.catalystId
              }
              onChangeStatus={() => handleChangeStatus(catalyst)}
              onEdit={() => setModalState({ mode: 'edit', catalyst })}
              onDelete={() => setDeleteTarget(catalyst)}
            />
          ))}
        </div>
      )}

      {modalState && (
        <CatalystFormModal
          mode={modalState.mode}
          catalyst={modalState.mode === 'edit' ? modalState.catalyst : undefined}
          isSubmitting={activeMutation.isPending}
          errorMessage={modalErrorMessage}
          onSubmit={handleFormSubmit}
          onClose={() => setModalState(null)}
        />
      )}

      <ConfirmDialog
        open={!!deleteTarget}
        title="카탈리스트를 삭제할까요?"
        description={`"${deleteTarget?.title ?? ''}" 카탈리스트를 삭제하면 되돌릴 수 없어요.`}
        confirmLabel="삭제"
        cancelLabel="취소"
        onConfirm={handleConfirmDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}
