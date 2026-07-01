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
      className="box-border flex w-full shrink-0 cursor-pointer flex-row items-center justify-center gap-[10px] rounded-[10px] border border-border bg-bg-surface px-[20px] py-[14px]"
    >
      {icon}
      <span className="whitespace-nowrap text-[14px] font-semibold leading-normal text-text-primary">
        {label}
      </span>
    </button>
  );
}
