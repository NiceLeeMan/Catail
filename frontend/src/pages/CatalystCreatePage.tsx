import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { isAxiosError } from 'axios';
import { ArrowLeft } from 'lucide-react';
import { AppHeader } from '../components/layout/AppHeader';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { CharCounterField } from '../components/catalyst-form/CharCounterField';
import { IndustrySelect } from '../components/catalyst-form/IndustrySelect';
import { MonitoringOptionCard } from '../components/catalyst-form/MonitoringOptionCard';
import { createCatalystSchema, type CreateCatalystFormValues } from '../schemas/catalyst';
import { useCreateCatalystMutation } from '../hooks/useCatalysts';
import type { ApiResponse } from '../api/types';

export function CatalystCreatePage() {
  const navigate = useNavigate();
  const [isCancelDialogOpen, setCancelDialogOpen] = useState(false);
  const createCatalystMutation = useCreateCatalystMutation();

  const {
    register,
    handleSubmit,
    watch,
    control,
    formState: { errors },
  } = useForm<CreateCatalystFormValues>({
    resolver: zodResolver(createCatalystSchema),
    defaultValues: { title: '', content: '', industryIds: [], status: 'INACTIVE' },
  });

  const title = watch('title');
  const content = watch('content');

  const onSubmit = (values: CreateCatalystFormValues) => {
    if (createCatalystMutation.isPending) return;
    createCatalystMutation.mutate(values, {
      onSuccess: () => navigate('/catalysts'),
    });
  };

  const submitError = createCatalystMutation.error;
  const serverErrorMessage =
    submitError &&
    (isAxiosError<ApiResponse<never>>(submitError)
      ? (submitError.response?.data?.error?.message ?? '카탈리스트 생성에 실패했습니다.')
      : '카탈리스트 생성에 실패했습니다.');

  return (
    <div className="box-border flex min-h-screen w-full flex-col items-center bg-bg-base">
      <AppHeader />

      <main className="mx-auto box-border flex w-full max-w-[1280px] flex-col gap-6 px-6 py-10">
        <button
          type="button"
          onClick={() => navigate('/catalysts')}
          className="box-border flex w-fit items-center gap-1 text-[14px] font-medium text-text-secondary hover:text-text-primary"
        >
          <ArrowLeft className="h-4 w-4" />
          돌아가기
        </button>

        <h1 className="text-[24px] font-bold leading-normal text-text-primary">새 카탈리스트 생성</h1>

        <form
          onSubmit={handleSubmit(onSubmit)}
          className="box-border flex w-full flex-col gap-6 rounded-card border border-border bg-bg-surface p-6 shadow-card"
        >
          <CharCounterField
            as="input"
            label="카탈리스트 이름"
            helperText="지속적으로 추적하려는 사건이나 변화의 이름을 입력하세요."
            placeholder="예) 미국의 대중 반도체 수출 규제 강화"
            displayMax={200}
            value={title}
            error={errors.title?.message}
            registration={register('title')}
          />

          <div className="border-t border-border" />

          <CharCounterField
            as="textarea"
            label="상세 내용"
            helperText="카탈리스트에 대해 자세히 기록하세요. 관련 자료 탐색 및 키워드 추천에 활용됩니다."
            placeholder="이 카탈리스트가 다루는 변화, 배경, 원인 등을 자세히 기록하세요."
            displayMax={1000}
            value={content}
            error={errors.content?.message}
            registration={register('content')}
          />

          <div className="border-t border-border" />

          <Controller
            name="industryIds"
            control={control}
            render={({ field }) => (
              <IndustrySelect
                value={field.value}
                onChange={field.onChange}
                error={errors.industryIds?.message}
              />
            )}
          />

          <div className="border-t border-border" />

          <Controller
            name="status"
            control={control}
            render={({ field }) => (
              <MonitoringOptionCard value={field.value} onChange={field.onChange} />
            )}
          />

          {serverErrorMessage && (
            <p className="rounded-card bg-status-ended-bg px-4 py-3 text-[13px] font-medium text-status-ended">
              {serverErrorMessage}
            </p>
          )}

          <div className="border-t border-border" />

          <div className="box-border flex justify-end gap-3">
            <button
              type="button"
              onClick={() => setCancelDialogOpen(true)}
              className="box-border rounded-full border border-border px-5 py-3 text-[14px] font-semibold text-text-secondary hover:bg-bg-base"
            >
              취소
            </button>
            <button
              type="submit"
              disabled={createCatalystMutation.isPending}
              className="box-border rounded-full px-5 py-3 text-[14px] font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60"
              style={{ backgroundImage: 'linear-gradient(90deg, #1F56E6 0%, #00D4B4 100%)' }}
            >
              {createCatalystMutation.isPending ? '생성 중...' : '카탈리스트 생성'}
            </button>
          </div>
        </form>
      </main>

      <ConfirmDialog
        open={isCancelDialogOpen}
        title="입력을 취소하시겠습니까?"
        description="입력한 데이터가 전부 사라집니다."
        confirmLabel="예"
        cancelLabel="아니오"
        onConfirm={() => navigate('/catalysts')}
        onCancel={() => setCancelDialogOpen(false)}
      />
    </div>
  );
}
