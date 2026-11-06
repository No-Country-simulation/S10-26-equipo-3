import logoContainer from "../assets/landing/logo-container.svg";

export default function LandingNavbar() {
  return (
    <header className="flex items-center justify-between w-full max-w-screen-xl px-2 py-2 rounded-xl overflow-clip">
      {/* Logotipo */}
      <div className="flex items-center gap-7">
        <div className="h-[85px] w-[76px] relative shrink-0">
          <img
            src={logoContainer}
            alt="NautiFlow logo"
            className="absolute inset-0 size-full max-w-none"
          />
        </div>
      </div>

      {/* CTA */}
      <button
        type="button"
        className="flex items-center justify-center gap-2 h-12 px-4 py-2 rounded-full bg-bg-fill-primary text-text-invert font-semibold text-[length:var(--text-subtitle-sm-size)] leading-[var(--text-subtitle-sm-line-height)] transition-colors hover:bg-bg-fill-primary-hover"
      >
        Registra tu lancha
      </button>
    </header>
  );
}
