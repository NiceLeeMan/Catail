import { useNavigate } from 'react-router-dom';
import { logout } from '../api/authApi';

export function HomePage() {
  const navigate = useNavigate();

  return (
    <div className="flex min-h-screen items-center justify-center bg-bg-base">
      <div className="flex flex-col items-center gap-6">
        <h1 className="text-3xl font-extrabold text-text-primary">Catail</h1>
        <p className="text-text-secondary">로그인되었습니다.</p>
        <button
          type="button"
          onClick={() => logout(navigate)}
          className="rounded-[10px] border border-border bg-bg-surface px-6 py-3 text-sm font-semibold text-text-primary hover:bg-bg-base"
        >
          로그아웃
        </button>
      </div>
    </div>
  );
}
