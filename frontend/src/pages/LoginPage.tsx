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

export function LoginPage() {
  const handleGoogleLogin = () => {
    window.location.href = `${import.meta.env.VITE_API_BASE_URL}/oauth2/authorization/google`;
  };

  return (
    <div className="box-border flex min-h-screen w-full flex-col items-center justify-center gap-0 overflow-hidden bg-bg-base">
      {/* Login Card */}
      <div
        className="box-border flex h-[760px] w-[1280px] shrink-0 flex-row items-start justify-start gap-0 overflow-hidden rounded-[28px] bg-bg-surface"
        style={{ boxShadow: '0px 16px 48px 0px #0E235014' }}
      >
        {/* Hero Panel */}
        <div
          className="box-border flex h-full flex-1 flex-col items-center justify-center gap-[28px] p-[80px]"
          style={{
            backgroundImage:
              'linear-gradient(-137.191deg, #FFFFFF 14.645%, #E4ECFE 57.071%, #D4F7F0 85.355%)',
            backgroundRepeat: 'no-repeat',
            backgroundSize: '100% 100%',
          }}
        >
          {/* Logo Lockup */}
          <div className="box-border flex h-fit w-fit shrink-0 flex-col items-center justify-start gap-[20px]">
            <img
              src={logo}
              alt="Catail 로고"
              className="box-border h-[160px] w-[160px] shrink-0 rounded-[20px] object-contain"
            />
            {/* Logo Wordmark — gradient text */}
            <span
              className="box-border whitespace-nowrap text-left text-[52px] font-extrabold leading-normal"
              style={{
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
                backgroundClip: 'text',
                backgroundImage:
                  'linear-gradient(0deg, #0A1F5C 0%, #1F56E6 50%, #00D4B4 100%)',
                backgroundRepeat: 'no-repeat',
                backgroundSize: '100% 100%',
                color: 'transparent',
              }}
            >
              Catail
            </span>
          </div>
          {/* Hero Tagline */}
          <p className="box-border w-[420px] text-center text-[16px] font-medium leading-normal text-text-secondary">
            관심 산업의 변화를, 놓치지 않고 추적하다
          </p>
        </div>

        {/* Auth Panel */}
        <div className="box-border flex h-full w-[576px] shrink-0 flex-col items-center justify-center gap-[40px] bg-bg-surface p-[64px]">
          {/* Text Block */}
          <div className="box-border flex h-fit w-full shrink-0 flex-col items-start justify-start gap-[10px]">
            <h1 className="box-border w-full text-left text-[30px] font-extrabold leading-normal text-text-primary">
              Catail에 오신 것을 환영합니다
            </h1>
            <p className="box-border w-full text-left text-[14px] font-normal leading-[21px] text-text-secondary">
              관심 산업의 변화를 카탈리스트로 등록하고 지속적으로 추적해보세요
            </p>
          </div>

          {/* Action Block */}
          <div className="box-border flex h-fit w-full shrink-0 flex-col items-start justify-start gap-[12px]">
            <SocialLoginButton
              provider="google"
              label="Google로 계속하기"
              icon={<GoogleGIcon />}
              onClick={handleGoogleLogin}
            />
            <p className="box-border w-full text-center text-[12px] font-normal leading-normal text-text-muted">
              다른 로그인 수단은 추후 추가될 예정입니다
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
