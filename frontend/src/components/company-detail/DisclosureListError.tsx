export function DisclosureListError() {
  return (
    <div className="flex w-full flex-col items-center justify-center gap-2 py-24 text-center">
      <p className="text-[14px] font-medium leading-normal text-dark-text-secondary">
        공시 목록을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.
      </p>
    </div>
  );
}
