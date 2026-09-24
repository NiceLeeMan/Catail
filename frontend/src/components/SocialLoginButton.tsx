interface SocialLoginButtonProps {
  provider: 'google';
  label: string;
  icon: React.ReactNode;
  onClick: () => void;
}

export function SocialLoginButton({ label, icon, onClick }: SocialLoginButtonProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="box-border flex w-full shrink-0 cursor-pointer flex-row items-center justify-center gap-[10px] rounded-[10px] border border-white/[0.08] bg-[#0F1729] px-[20px] py-[14px] transition-colors duration-150 hover:bg-[#182338] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#34D399] focus-visible:ring-offset-2 focus-visible:ring-offset-[#131B2E] active:scale-[0.98]"
    >
      {icon}
      <span className="whitespace-nowrap text-[14px] font-semibold leading-normal text-[#F1F5F9]">
        {label}
      </span>
    </button>
  );
}
