const REMARK_COLOR_MAP: Record<string, string> = {
  정정: 'text-[#F59E0B]',
  연결: 'text-dark-accent',
};

interface RemarkBadgeProps {
  codes: string[];
}

export function RemarkBadge({ codes }: RemarkBadgeProps) {
  if (codes.length === 0) {
    return (
      <span className="text-[13px] font-normal leading-normal text-dark-text-muted">
        -
      </span>
    );
  }

  return (
    <div className="flex items-center gap-1">
      {codes.map((code) => (
        <span
          key={code}
          className={`text-[13px] font-normal leading-normal ${
            REMARK_COLOR_MAP[code] ?? 'text-dark-text-muted'
          }`}
        >
          {code}
        </span>
      ))}
    </div>
  );
}
