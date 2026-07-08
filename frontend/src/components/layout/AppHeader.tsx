import { ChevronDown } from 'lucide-react';
import logo from '../../asset/logo.png';

export function AppHeader() {
  return (
    <header className="sticky top-0 z-10 box-border w-full border-b border-border bg-bg-surface">
      <div className="mx-auto box-border flex h-16 w-full max-w-[1280px] items-center justify-between px-6">
        <img src={logo} alt="Catail" className="h-8" />
        <div className="box-border flex items-center gap-3">
          <div className="box-border flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary-soft text-[13px] font-semibold text-primary">
            KM
          </div>
          <span className="text-[14px] font-medium leading-normal text-text-primary">김민준</span>
          <ChevronDown className="h-4 w-4 text-text-muted" />
        </div>
      </div>
    </header>
  );
}
