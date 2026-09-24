interface ConfirmDialogProps {
  open: boolean;
  title: string;
  description: string;
  confirmLabel: string;
  cancelLabel: string;
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmDialog({
  open,
  title,
  description,
  confirmLabel,
  cancelLabel,
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 box-border flex items-center justify-center bg-black/60 px-6"
      onClick={onCancel}
    >
      <div
        role="alertdialog"
        aria-modal="true"
        onClick={(e) => e.stopPropagation()}
        className="box-border flex w-full max-w-[400px] flex-col gap-4 rounded-2xl border border-white/[0.08] bg-[#131B2E] p-6 shadow-[0_20px_60px_rgba(0,0,0,0.45)]"
      >
        <h2 className="text-[16px] font-bold leading-normal text-[#F1F5F9]">{title}</h2>
        <p className="text-[14px] font-normal leading-normal text-[#94A3B8]">{description}</p>
        <div className="box-border flex justify-end gap-2">
          <button
            type="button"
            onClick={onCancel}
            className="box-border cursor-pointer rounded-lg border border-white/[0.08] px-4 py-2 text-[13px] font-semibold leading-normal text-[#94A3B8] transition-colors duration-150 hover:bg-[#182338] hover:text-[#F1F5F9] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399]"
          >
            {cancelLabel}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className="box-border cursor-pointer rounded-lg bg-[#EF4444] px-4 py-2 text-[13px] font-semibold leading-normal text-white transition-colors duration-150 hover:bg-[#DC2626] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#EF4444] focus-visible:ring-offset-2 focus-visible:ring-offset-[#131B2E]"
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
