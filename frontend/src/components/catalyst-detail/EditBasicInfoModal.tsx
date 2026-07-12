import { useEffect } from 'react';
import { Pencil, X } from 'lucide-react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { CharCounterField } from '../catalyst-form/CharCounterField';
import { IndustrySelect } from '../catalyst-form/IndustrySelect';
import { useIndustriesQuery } from '../../hooks/useIndustries';
import {
  updateCatalystBasicInfoSchema,
  type UpdateCatalystBasicInfoFormValues,
} from '../../schemas/catalyst';

interface EditBasicInfoModalProps {
  open: boolean;
  initialTitle: string;
  initialContent: string;
  initialIndustryNames: string[];
  onClose: () => void;
  onSubmit: (values: UpdateCatalystBasicInfoFormValues) => void;
  isSubmitting: boolean;
  submitError?: string;
}

export function EditBasicInfoModal({
  open,
  initialTitle,
  initialContent,
  initialIndustryNames,
  onClose,
  onSubmit,
  isSubmitting,
  submitError,
}: EditBasicInfoModalProps) {
  const { data: industries } = useIndustriesQuery();

  const {
    register,
    handleSubmit,
    watch,
    control,
    reset,
    formState: { errors },
  } = useForm<UpdateCatalystBasicInfoFormValues>({
    resolver: zodResolver(updateCatalystBasicInfoSchema),
    defaultValues: { title: initialTitle, content: initialContent, industryIds: [] },
  });

  // industries 목록이 로드된 뒤에야 이름→id 매핑이 가능하므로, 모달이 열리고 목록이 준비되면 다시 채운다.
  useEffect(() => {
    if (!open || !industries) return;
    const industryIds = industries
      .filter((industry) => initialIndustryNames.includes(industry.name))
      .map((industry) => industry.id);
    reset({ title: initialTitle, content: initialContent, industryIds });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, industries]);

  const title = watch('title');
  const content = watch('content');

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 box-border flex items-center justify-center bg-[#0E2350]/45 px-6 backdrop-blur-sm">
      <div
        className="box-border flex max-h-[85vh] w-full max-w-[560px] flex-col overflow-hidden rounded-card bg-bg-surface"
        style={{ boxShadow: '0px 24px 64px -12px rgba(14,35,80,0.45)' }}
      >
        <div className="box-border flex w-full shrink-0 items-center justify-between gap-4 px-7 pb-5 pt-6">
          <div className="box-border flex items-center gap-3">
            <div className="box-border flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary-soft text-primary">
              <Pencil className="h-4 w-4" />
            </div>
            <div className="box-border flex flex-col gap-0.5">
              <h2 className="text-[16px] font-bold leading-normal text-text-primary">기본 정보 수정</h2>
              <p className="text-[12px] font-normal leading-normal text-text-muted">
                이름, 상세 내용, 관련 산업을 수정합니다.
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="닫기"
            className="box-border flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-text-muted hover:bg-bg-base hover:text-text-secondary"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        <div className="h-px w-full shrink-0 bg-border" />

        <form onSubmit={handleSubmit(onSubmit)} className="box-border flex min-h-0 flex-1 flex-col">
          <div className="box-border flex min-h-0 flex-1 flex-col gap-6 overflow-y-auto px-7 py-6">
            <CharCounterField
              as="input"
              label="카탈리스트 이름"
              helperText="지속적으로 추적하려는 사건이나 변화의 이름을 입력하세요."
              placeholder="예) 미국의 대중 반도체 수출 규제 강화"
              displayMax={50}
              value={title}
              error={errors.title?.message}
              registration={register('title')}
            />

            <CharCounterField
              as="textarea"
              label="상세 내용"
              helperText="카탈리스트에 대해 자세히 기록하세요."
              placeholder="이 카탈리스트가 다루는 변화, 배경, 원인 등을 자세히 기록하세요."
              displayMax={500}
              value={content}
              error={errors.content?.message}
              registration={register('content')}
              rows={5}
            />

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

            {submitError && (
              <p className="rounded-card bg-status-ended-bg px-4 py-3 text-[13px] font-medium text-status-ended">
                {submitError}
              </p>
            )}
          </div>

          <div className="h-px w-full shrink-0 bg-border" />

          <div className="box-border flex w-full shrink-0 justify-end gap-3 px-7 py-5">
            <button
              type="button"
              onClick={onClose}
              className="box-border rounded-full border border-border px-5 py-3 text-[14px] font-semibold text-text-secondary transition hover:bg-bg-base"
            >
              취소
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="box-border rounded-full px-5 py-3 text-[14px] font-semibold text-white transition hover:opacity-90 active:scale-95 disabled:cursor-not-allowed disabled:opacity-60"
              style={{ backgroundImage: 'linear-gradient(90deg, #1F56E6 0%, #00D4B4 100%)' }}
            >
              {isSubmitting ? '저장 중...' : '저장'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
