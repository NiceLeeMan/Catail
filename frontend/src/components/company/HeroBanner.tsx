export function HeroBanner() {
  return (
    <div className="relative box-border flex h-56 w-full shrink-0 overflow-hidden border-b border-white/[0.08] bg-[#0B1120]">
      {/* Ambient glow */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute right-[-100px] top-1/2 h-[420px] w-[560px] -translate-y-1/2 rounded-full bg-[#10B981]/[0.14] blur-[140px]"
      />

      {/* Market-trend graphic */}
      <svg
        aria-hidden="true"
        viewBox="0 0 900 320"
        preserveAspectRatio="xMidYMid slice"
        className="pointer-events-none absolute inset-y-0 right-0 hidden h-full w-[60%] opacity-80 md:block"
      >
        <defs>
          <linearGradient id="hero-trend-stroke" x1="0" y1="320" x2="900" y2="0" gradientUnits="userSpaceOnUse">
            <stop offset="0" stopColor="#34D399" stopOpacity="0" />
            <stop offset="0.35" stopColor="#34D399" stopOpacity="0.55" />
            <stop offset="1" stopColor="#6EE7B7" stopOpacity="1" />
          </linearGradient>
          <linearGradient id="hero-trend-fill" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0" stopColor="#10B981" stopOpacity="0.22" />
            <stop offset="1" stopColor="#10B981" stopOpacity="0" />
          </linearGradient>
          <pattern id="hero-dot-grid" width="16" height="16" patternUnits="userSpaceOnUse">
            <circle cx="1.4" cy="1.4" r="1.1" fill="#34D399" fillOpacity="0.18" />
          </pattern>
          <linearGradient id="hero-dot-fade" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0" stopColor="white" stopOpacity="0" />
            <stop offset="0.45" stopColor="white" stopOpacity="1" />
          </linearGradient>
          <mask id="hero-dot-mask">
            <rect width="900" height="320" fill="url(#hero-dot-fade)" />
          </mask>
        </defs>

        <rect width="900" height="320" fill="url(#hero-dot-grid)" mask="url(#hero-dot-mask)" />

        <path
          d="M0,260 L110,235 L220,245 L330,175 L440,200 L550,120 L660,150 L770,70 L900,95"
          fill="none"
          stroke="url(#hero-trend-stroke)"
          strokeWidth="3"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        <path
          d="M0,260 L110,235 L220,245 L330,175 L440,200 L550,120 L660,150 L770,70 L900,95 L900,320 L0,320 Z"
          fill="url(#hero-trend-fill)"
        />
        {[
          [770, 70],
          [550, 120],
          [330, 175],
        ].map(([cx, cy]) => (
          <circle key={`${cx}-${cy}`} cx={cx} cy={cy} r="4" fill="#6EE7B7" />
        ))}
      </svg>

      <div className="relative mx-auto box-border flex h-full w-full max-w-[1280px] flex-col justify-center gap-2 px-8">
        <h1 className="text-[28px] font-bold leading-normal text-[#F1F5F9] animate-[fade-up_0.6s_ease-out_both]">
          기업 탐색
        </h1>
        <p className="text-[14px] font-normal leading-normal text-[#94A3B8] animate-[fade-up_0.6s_ease-out_0.1s_both]">
          관심 기업을 선택해 기업 정보와 모니터링 기준을 확인하세요.
        </p>
      </div>
    </div>
  );
}
