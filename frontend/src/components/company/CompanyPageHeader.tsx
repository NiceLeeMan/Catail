import logo from '../../asset/logo.png';

export function CompanyPageHeader() {
  return (
    <header className="sticky top-0 z-10 box-border flex h-16 w-full shrink-0 items-center border-b border-white/[0.08] bg-[#0B1120]/95 px-8 backdrop-blur">
      <div className="mx-auto flex w-full max-w-[1280px] items-center gap-2.5">
        <img src={logo} alt="" className="h-7 w-7 shrink-0 object-contain" />
        <span className="text-[16px] font-extrabold leading-normal text-[#34D399]">Catail</span>
      </div>
    </header>
  );
}
