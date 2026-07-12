import { ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { STATUS_META, type CatalystStatus } from '../../types/catalyst';
import { formatCatalystId } from '../../mocks/catalystDetail';

interface CatalystDetailHeaderProps {
  id: number;
  title: string;
  status: CatalystStatus;
  industryTags: string[];
}

export function CatalystDetailHeader({ id, title, status, industryTags }: CatalystDetailHeaderProps) {
  const navigate = useNavigate();
  const meta = STATUS_META[status];

  return (
    <div className="box-border w-full border-b border-border bg-bg-surface">
      <div className="mx-auto box-border flex w-full max-w-[1280px] flex-col gap-4 px-8 pb-5 pt-7">
        <button
          type="button"
          onClick={() => navigate('/catalysts')}
          className="box-border flex w-fit items-center gap-1.5 text-[13px] font-normal text-text-muted hover:text-text-secondary"
        >
          <ArrowLeft className="h-4 w-4" />
          카탈리스트 목록
        </button>

        <div className="box-border flex w-full items-center justify-between">
          <div className="box-border flex flex-col items-start gap-2.5">
            <h1 className="text-[22px] font-bold leading-normal text-text-primary">{title}</h1>
            <div className="box-border flex items-center gap-2">
              {industryTags.map((tag) => (
                <span
                  key={tag}
                  className="box-border shrink-0 whitespace-nowrap rounded-full border border-border bg-bg-base px-2.5 py-1 text-[12px] font-normal text-text-secondary"
                >
                  {tag}
                </span>
              ))}
            </div>
          </div>

          <div className="box-border flex shrink-0 flex-col items-end gap-1.5">
            <span
              className={`box-border w-fit rounded-full px-3.5 py-1.5 text-[12px] font-semibold ${meta.bg} ${meta.text}`}
            >
              {meta.label}
            </span>
            <span className="text-[12px] font-normal text-text-muted">ID: {formatCatalystId(id)}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
