// DART OpenAPI의 rm 필드는 공백으로 구분된 단일 문자 코드다(예: "유 정").
// https://opendart.fss.or.kr 공시서류 목록 API 문서 기준.
const REMARK_LABEL_MAP: Record<string, string> = {
  정: '정정',
  철회: '철회',
  연: '연결',
  유: '유가증권',
  코: '코스닥',
  채: '채권',
  넥: '코넥스',
  공: '공정위',
};

const REMARK_COLOR_MAP: Record<string, string> = {
  정: 'text-[#F59E0B]',
  철회: 'text-[#EF4444]',
  연: 'text-dark-accent',
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
          {REMARK_LABEL_MAP[code] ?? code}
        </span>
      ))}
    </div>
  );
}
