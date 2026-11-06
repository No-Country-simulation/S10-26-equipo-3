import arrowDownIcon from "../assets/landing/arrow-down.svg";
import logoContainer from "../assets/landing/logo-container.svg";

export default function HeroSection() {
  return (
    <section
      className="relative flex flex-col gap-[205px] items-start w-full overflow-clip bg-bg-fill-primary pb-[91px] pl-[60px] pr-[47px] pt-[31px]"
      style={{ height: "832px" }}
    >
      {/* Contenedor group */}
      <div className="flex flex-col gap-[75px] items-start w-full shrink-0">
        {/* Header */}
        <div className="flex items-center justify-between w-full max-w-screen-xl px-2 rounded-xl overflow-clip">
          {/* Logo solo icono */}
          <div
            className="relative shrink-0"
            style={{ height: "84.72px", width: "76px" }}
          >
            <img
              src={logoContainer}
              alt="NautiFlow logo"
              className="absolute inset-0 size-full max-w-none block"
            />
          </div>

          {/* CTA registra tu lancha */}
          <button
            type="button"
            className="flex items-center justify-center gap-2 h-12 px-4 py-2 rounded-full bg-bg-fill-primary border border-white/20 text-text-invert font-medium text-[length:var(--text-subtitle-sm-size)] leading-[var(--text-subtitle-sm-line-height)] transition-colors hover:bg-bg-fill-primary-hover"
          >
            Registra tu lancha
          </button>
        </div>

        {/* Texto hero grande */}
        <div className="flex items-center justify-center p-2.5 w-full shrink-0">
          <p
            className="flex-1 min-w-px text-center font-bold opacity-80 bg-clip-text text-transparent"
            style={{
              fontFamily: "'Plus Jakarta Sans', sans-serif",
              fontSize: "220px",
              lineHeight: "240px",
              letterSpacing: "-0.4px",
              backgroundImage:
                "linear-gradient(to bottom, white, rgba(255,255,255,0.15))",
            }}
          >
            NautiFlow
          </p>
        </div>
      </div>

      {/* CTA bottom-left */}
      <div className="flex flex-col gap-[17px] items-start shrink-0 w-[342px]">
        <p className="font-bold text-[length:var(--text-heading-3xl-size)] leading-[var(--text-heading-3xl-line-height)] text-text-invert">
          Encuentra tu próxima lancha en el Delta
        </p>
        <button
          type="button"
          className="flex items-center justify-center gap-2 h-12 px-4 py-2 rounded-full bg-bg-fill-primary border border-white/20 text-text-invert font-medium text-[length:var(--text-subtitle-sm-size)] leading-[var(--text-subtitle-sm-line-height)] transition-colors hover:bg-bg-fill-primary-hover"
        >
          <span>Explorar lanchas</span>
          <div className="relative size-6 flex items-center justify-center">
            <img
              src={arrowDownIcon}
              alt=""
              className="absolute inset-0 size-full max-w-none block"
            />
          </div>
        </button>
      </div>
    </section>
  );
}
