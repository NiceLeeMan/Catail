import { Pencil } from 'lucide-react';
import type { ReactNode } from 'react';

interface BasicInfoCardProps {
  title: string;
  content: string;
  industryTags: string[];
  createdAtLabel: string;
  updatedAtLabel: string;
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="box-border flex w-full flex-col items-start gap-1.5">
      <span className="text-[12px] font-normal text-text-muted">{label}</span>
      {children}
    </div>
  );
}

export function BasicInfoCard({
  title,
  content,
  industryTags,
  createdAtLabel,
  updatedAtLabel,
}: BasicInfoCardProps) {
  return (
    <div className="box-border flex w-full flex-col items-start rounded-card border border-border bg-bg-surface">
      <div className="box-border flex w-full items-center justify-between px-6 pb-4 pt-5">
        <h2 className="text-[16px] font-bold text-text-primary">기본 정보</h2>
        <button
          type="button"
          className="box-border flex items-center gap-1.5 rounded-lg border border-border px-3.5 py-2 text-[12px] font-semibold text-text-secondary hover:bg-bg-base"
        >
          <Pencil className="h-3.5 w-3.5" />
          수정
        </button>
      </div>

      <div className="h-px w-full bg-border" />

      <div className="box-border flex w-full items-start gap-10 px-6 py-5">
        <div className="box-border flex flex-1 flex-col items-start gap-5">
          <Field label="카탈리스트 이름">
            <p className="text-[14px] font-semibold text-text-primary">{title}</p>
          </Field>
          <Field label="상세 내용">
            <p className="text-[13px] font-normal leading-5 text-text-secondary">{content}</p>
          </Field>
          <Field label="관련 산업">
            <div className="box-border flex w-full flex-wrap items-center gap-2">
              {industryTags.map((tag) => (
                <span
                  key={tag}
                  className="box-border shrink-0 whitespace-nowrap rounded-full bg-primary-soft px-2.5 py-1 text-[12px] font-normal text-primary"
                >
                  {tag}
                </span>
              ))}
            </div>
          </Field>
        </div>

        <div className="box-border flex w-[200px] shrink-0 flex-col items-start gap-5 border-l border-border pl-6">
          <Field label="생성일">
            <p className="text-[14px] font-semibold text-text-primary">{createdAtLabel}</p>
          </Field>
          <Field label="마지막 수정일">
            <p className="text-[14px] font-semibold text-text-primary">{updatedAtLabel}</p>
          </Field>
        </div>
      </div>
    </div>
  );
}
