import { zodResolver } from '@hookform/resolvers/zod';
import { X } from 'lucide-react';
import { useEffect, useId } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { z } from 'zod';
import type { CatalystListItemResponse } from '../../../types/catalyst';
import { CATALYST_CATEGORIES, CATALYST_CATEGORY_LABELS } from '../../../utils/catalyst';

const formSchema = z.object({
  category: z.enum(CATALYST_CATEGORIES),
  detail: z
    .string()
    .trim()
    .min(10, '10자 이상 입력해주세요.')
    .max(300, '300자 이하로 입력해주세요.'),
  status: z.enum(['ACTIVE', 'INACTIVE']),
});

export type CatalystFormValues = z.infer<typeof formSchema>;

interface CatalystFormModalProps {
  mode: 'create' | 'edit';
  catalyst?: CatalystListItemResponse;
  isSubmitting: boolean;
  errorMessage: string | null;
  onSubmit: (values: CatalystFormValues) => void;
  onClose: () => void;
}

export function CatalystFormModal({
  mode,
  catalyst,
  isSubmitting,
  errorMessage,
  onSubmit,
  onClose,
}: CatalystFormModalProps) {
  const titleId = useId();
  const detailErrorId = useId();

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<CatalystFormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      category: catalyst?.category ?? 'FINANCIAL_PERFORMANCE',
      detail: catalyst?.detail ?? '',
      status: catalyst?.status === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE',
    },
  });

  const detailValue = useWatch({ control, name: 'detail' });
  const detailLength = detailValue?.length ?? 0;

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  return (
    <div
      className="fixed inset-0 z-50 box-border flex items-center justify-center bg-black/60 px-6"
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        onClick={(e) => e.stopPropagation()}
        className="box-border flex max-h-[90vh] w-full max-w-[480px] flex-col gap-5 overflow-y-auto rounded-2xl border border-white/[0.08] bg-[#131B2E] p-6 shadow-[0_20px_60px_rgba(0,0,0,0.45)]"
      >
        <div className="flex items-center justify-between">
          <h2 id={titleId} className="text-[17px] font-bold leading-normal text-[#F1F5F9]">
            {mode === 'create' ? '' : ''}
          </h2>
          <button
            type="button"
            onClick={onClose}
            aria-label="닫기"
            className="box-border flex h-8 w-8 shrink-0 cursor-pointer items-center justify-center rounded-lg text-[#64748B] transition-colors duration-150 hover:bg-[#182338] hover:text-[#F1F5F9] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399]"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        {errorMessage && (
          <p
            role="alert"
            className="rounded-lg border border-[#EF4444]/30 bg-[#EF4444]/10 px-3 py-2 text-[13px] font-medium leading-normal text-[#EF4444]"
          >
            {errorMessage}
          </p>
        )}

        <form
          onSubmit={handleSubmit(onSubmit)}
          className="box-border flex flex-col gap-5"
        >
          <div className="flex flex-col gap-2">
            <span className="text-[13px] font-semibold leading-normal text-[#F1F5F9]">카테고리</span>
            <div className="grid grid-cols-2 gap-2" role="radiogroup" aria-label="카테고리">
              {CATALYST_CATEGORIES.map((cat) => (
                <label key={cat} className="relative">
                  <input type="radio" value={cat} className="peer sr-only" {...register('category')} />
                  <span className="block cursor-pointer rounded-lg border border-white/[0.08] bg-[#0F1729] px-3 py-2 text-center text-[13px] font-medium leading-normal text-[#94A3B8] transition-colors duration-150 peer-checked:border-[#34D399] peer-checked:bg-[#10B981]/10 peer-checked:text-[#34D399] peer-focus-visible:outline-none peer-focus-visible:ring-2 peer-focus-visible:ring-[#34D399]">
                    {CATALYST_CATEGORY_LABELS[cat]}
                  </span>
                </label>
              ))}
            </div>
          </div>

          <div className="flex flex-col gap-2">
            <label htmlFor="catalyst-detail" className="text-[13px] font-semibold leading-normal text-[#F1F5F9]">
              추적 내용
            </label>
            <textarea
              id="catalyst-detail"
              rows={4}
              maxLength={300}
              placeholder="예: 반도체 가격 반등 여부와 분기 실적 가이던스 변화를 추적하고 싶어요."
              aria-describedby={detailErrorId}
              aria-invalid={!!errors.detail}
              className="w-full resize-none rounded-lg border border-white/[0.08] bg-[#0F1729] px-3 py-2.5 text-[14px] leading-normal text-[#F1F5F9] placeholder:text-[#64748B] focus:border-[#34D399] focus:outline-none focus:ring-2 focus:ring-[#34D399]/30"
              {...register('detail')}
            />
            <div className="flex items-center justify-between" id={detailErrorId}>
              <span className="text-[12px] font-normal leading-normal text-[#EF4444]">
                {errors.detail?.message}
              </span>
              <span className="text-[12px] font-normal leading-normal text-[#64748B]">
                {detailLength}/300자
              </span>
            </div>
          </div>

          {mode === 'create' && (
            <div className="flex flex-col gap-2">
              <span className="text-[13px] font-semibold leading-normal text-[#F1F5F9]">등록 상태</span>
              <div className="grid grid-cols-2 gap-2" role="radiogroup" aria-label="등록 상태">
                <label className="relative">
                  <input type="radio" value="INACTIVE" className="peer sr-only" {...register('status')} />
                  <span className="block cursor-pointer rounded-lg border border-white/[0.08] bg-[#0F1729] px-3 py-2 text-center text-[13px] font-medium leading-normal text-[#94A3B8] transition-colors duration-150 peer-checked:border-[#34D399] peer-checked:bg-[#10B981]/10 peer-checked:text-[#34D399] peer-focus-visible:outline-none peer-focus-visible:ring-2 peer-focus-visible:ring-[#34D399]">
                    비활성으로 저장
                  </span>
                </label>
                <label className="relative">
                  <input type="radio" value="ACTIVE" className="peer sr-only" {...register('status')} />
                  <span className="block cursor-pointer rounded-lg border border-white/[0.08] bg-[#0F1729] px-3 py-2 text-center text-[13px] font-medium leading-normal text-[#94A3B8] transition-colors duration-150 peer-checked:border-[#34D399] peer-checked:bg-[#10B981]/10 peer-checked:text-[#34D399] peer-focus-visible:outline-none peer-focus-visible:ring-2 peer-focus-visible:ring-[#34D399]">
                    즉시 활성화
                  </span>
                </label>
              </div>
              <p className="text-[12px] font-normal leading-normal text-[#64748B]">
                활성화하면 관련 뉴스·공시를 실시간으로 모니터링해요. 나중에 언제든 바꿀 수 있어요.
              </p>
            </div>
          )}

          <div className="flex justify-end gap-2 pt-1">
            <button
              type="button"
              onClick={onClose}
              className="box-border cursor-pointer rounded-lg border border-white/[0.08] px-4 py-2 text-[13px] font-semibold leading-normal text-[#94A3B8] transition-colors duration-150 hover:bg-[#182338] hover:text-[#F1F5F9] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399]"
            >
              취소
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="box-border cursor-pointer rounded-lg bg-[#34D399] px-4 py-2 text-[13px] font-semibold leading-normal text-[#0B1120] transition-colors duration-150 hover:bg-[#6EE7B7] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399] focus-visible:ring-offset-2 focus-visible:ring-offset-[#131B2E] disabled:cursor-not-allowed disabled:opacity-50"
            >
              {isSubmitting ? '저장 중...' : '저장'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
