import heroBackground from '../../asset/Catail-wave.svg';

export function HeroBanner() {
  return (
    <div
      className="box-border flex h-60 w-full shrink-0 border-b border-dark-border bg-dark-bg-base bg-no-repeat"
      style={{
        backgroundImage: `url(${heroBackground})`,
        backgroundSize: 'auto 100%',
        backgroundPosition: 'right bottom',
      }}
    >
      <div className="mx-auto box-border flex h-full w-full max-w-[1280px] flex-col gap-2 px-8 pt-12">
        <h1 className="text-[32px] font-bold leading-normal text-dark-text-primary animate-[fade-up_0.6s_ease-out_both]">
          기업 탐색
        </h1>
        <p className="text-[15px] font-normal leading-normal text-dark-text-secondary animate-[fade-up_0.6s_ease-out_0.1s_both]">
          관심 기업을 선택해 기업 정보와 모니터링 기준을 확인하세요.
        </p>
      </div>
    </div>
  );
}
