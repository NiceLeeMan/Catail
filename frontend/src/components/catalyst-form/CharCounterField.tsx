import type { UseFormRegisterReturn } from 'react-hook-form';

interface CharCounterFieldProps {
  as: 'input' | 'textarea';
  label: string;
  helperText: string;
  placeholder: string;
  displayMax: number;
  value: string;
  error?: string;
  registration: UseFormRegisterReturn;
  rows?: number;
}

export function CharCounterField({
  as,
  label,
  helperText,
  placeholder,
  displayMax,
  value,
  error,
  registration,
  rows = 6,
}: CharCounterFieldProps) {
  const sharedClassName =
    'box-border w-full rounded-card border border-border bg-bg-surface px-4 py-3 text-[14px] text-text-primary placeholder:text-text-muted focus:border-primary focus:outline-none';

  return (
    <div className="box-border flex w-full flex-col gap-2">
      <div className="box-border flex items-baseline justify-between">
        <label className="text-[14px] font-semibold leading-normal text-text-primary">
          {label} <span className="text-status-ended">*</span>
        </label>
        <span className="text-[12px] font-normal text-text-muted">
          {value.length.toLocaleString()} / {displayMax.toLocaleString()}자
        </span>
      </div>
      <p className="text-[13px] font-normal leading-normal text-text-secondary">{helperText}</p>
      {as === 'textarea' ? (
        <textarea
          {...registration}
          rows={rows}
          placeholder={placeholder}
          className={sharedClassName}
        />
      ) : (
        <input
          {...registration}
          type="text"
          placeholder={placeholder}
          className={sharedClassName}
        />
      )}
      {error && <p className="text-[13px] font-normal leading-normal text-status-ended">{error}</p>}
    </div>
  );
}
