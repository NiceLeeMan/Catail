export function CatalystListError() {
  return (
    <div className="flex w-full flex-col items-center justify-center gap-2 py-24 text-center">
      <p className="text-[14px] font-medium leading-normal text-text-secondary">
        카탈리스트 목록을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.
      </p>
    </div>
  );
}
