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
    <div className="fixed inset-0 z-50 box-border flex items-center justify-center bg-black/40 px-6">
      <div className="box-border flex w-full max-w-[400px] flex-col gap-4 rounded-card bg-bg-surface p-6 shadow-card">
        <h2 className="text-[16px] font-bold leading-normal text-text-primary">{title}</h2>
        <p className="text-[14px] font-normal leading-normal text-text-secondary">{description}</p>
        <div className="box-border flex justify-end gap-2">
          <button
            type="button"
            onClick={onCancel}
            className="box-border rounded-full border border-border px-4 py-2 text-[14px] font-semibold text-text-secondary hover:bg-bg-base"
          >
            {cancelLabel}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className="box-border rounded-full bg-status-ended px-4 py-2 text-[14px] font-semibold text-white"
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
