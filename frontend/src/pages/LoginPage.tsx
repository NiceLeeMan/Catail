import { Bell, Building2, Newspaper } from 'lucide-react';
import logo from '../asset/logo.png';
import { SocialLoginButton } from '../components/SocialLoginButton';

function GoogleGIcon() {
  return (
    <span
      className="whitespace-nowrap text-[20px] font-bold leading-normal"
      style={{
        WebkitBackgroundClip: 'text',
        WebkitTextFillColor: 'transparent',
        backgroundClip: 'text',
        backgroundImage: 'linear-gradient(0deg, #4285F4 0%, #EA4335 35%, #FBBC05 65%, #34A853 100%)',
        backgroundRepeat: 'no-repeat',
        backgroundSize: '100% 100%',
        color: 'transparent',
      }}
    >
      G
    </span>
  );
}

const PRODUCT_PILLARS = [
  { icon: Building2, label: '기업 탐색' },
  { icon: Newspaper, label: '카탈리스트' },
  { icon: Bell, label: '시그널' },
];

export function LoginPage() {
  const handleGoogleLogin = () => {
    window.location.href = `${import.meta.env.VITE_API_BASE_URL}/oauth2/authorization/google`;
  };

  return (
    <div className="relative flex min-h-screen w-full items-center justify-center overflow-hidden bg-[#0B1120] px-6 py-20">
      {/* Ambient backdrop — CSS-only glow, no imagery */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute left-1/2 top-[-160px] h-[520px] w-[720px] -translate-x-1/2 rounded-full bg-[#10B981]/[0.12] blur-[150px]"
      />

      {/* Content */}
      <div className="relative z-10 flex w-full max-w-[380px] flex-col items-center gap-8 animate-[fade-up_0.6s_ease-out_both]">
        {/* Brand */}
        <div className="flex flex-col items-center gap-3">
          <img src={logo} alt="Catail 로고" className="h-20 w-20 shrink-0 object-contain" />
          <div className="flex flex-col items-center gap-1.5">
            <span className="text-[22px] font-extrabold leading-normal text-[#34D399]">Catail</span>
            <p className="text-center text-[13px] font-medium leading-normal text-[#94A3B8]">
              관심 기업의 공시와 시그널을, 놓치지 않고
            </p>
          </div>
        </div>

        {/* Auth Card */}
        <div className="box-border flex w-full flex-col items-center gap-6 rounded-2xl border border-white/[0.08] bg-[#131B2E] p-8 shadow-[0_20px_60px_rgba(0,0,0,0.45)]">
          <h1 className="text-center text-[15px] font-medium leading-normal text-[#94A3B8]">
            Google 계정으로 바로 시작하세요
          </h1>

          <div className="flex w-full flex-col items-center gap-3">
            <SocialLoginButton
              provider="google"
              label="Google로 계속하기"
              icon={<GoogleGIcon />}
              onClick={handleGoogleLogin}
            />
          </div>
        </div>

        {/* Product pillars */}
        <div className="grid w-full grid-cols-3 gap-2">
          {PRODUCT_PILLARS.map(({ icon: Icon, label }) => (
            <div
              key={label}
              className="flex flex-col items-center gap-2 rounded-xl border border-white/[0.08] bg-[#0F1729] px-3 py-4"
            >
              <Icon className="h-5 w-5 text-[#34D399]" aria-hidden="true" />
              <span className="text-[12px] font-medium leading-normal text-[#94A3B8]">{label}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
