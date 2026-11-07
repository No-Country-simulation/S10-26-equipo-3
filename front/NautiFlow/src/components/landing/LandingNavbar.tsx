import { useNavigate } from "react-router-dom";
import Logo from "../Logo";

export default function LandingNavbar() {
  const navigate = useNavigate();

  return (
    <header className="flex items-center justify-between w-full py-2 rounded-xl">
      <Logo />

      <button
        type="button"
        onClick={() => navigate("/registro-lanchero")}
        className="flex items-center justify-center gap-2 h-10 sm:h-12 px-3.5 sm:px-4 py-2 rounded-full bg-bg-fill-primary text-text-invert font-semibold text-xs sm:text-[length:var(--text-subtitle-sm-size)] leading-[var(--text-subtitle-sm-line-height)] transition-colors hover:bg-bg-fill-primary-hover shrink-0"
      >
        Registra tu lancha
      </button>
    </header>
  );
}
