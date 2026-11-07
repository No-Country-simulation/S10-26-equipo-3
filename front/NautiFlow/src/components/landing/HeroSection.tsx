import { BsArrowDown } from "react-icons/bs";
import heroVideo from "../../assets/landing/hero.mov";
import LandingNavbar from "./LandingNavbar";

export default function HeroSection() { 
  return (
    <section
      className="relative flex flex-col justify-between items-start w-full min-h-screen min-h-dvh overflow-clip bg-bg-fill-secondary px-4 sm:px-8 md:px-[60px] py-6 md:py-[31px] gap-8 md:gap-12"
    >
      {/* Background Video */}
      <div className="absolute inset-0 size-full overflow-hidden pointer-events-none z-0">
        <video
          autoPlay
          loop
          muted
          playsInline
          className="absolute inset-0 size-full object-cover"
        >
          <source src={heroVideo} type="video/quicktime" />
          <source src={heroVideo} type="video/mp4" />
        </video>
        {/* Overlay suave para asegurar contraste del contenido */}
        <div className="absolute inset-0 bg-black/25" />
      </div>

      {/* Contenedor group */}
      <div className="relative z-10 flex flex-col items-start w-full shrink-0 gap-6 md:gap-10">
        <LandingNavbar />

        {/* Texto hero grande */}
        <div className="flex items-center justify-center p-2 w-full shrink-0 my-auto">
          <p
            className="w-full text-center font-bold opacity-80 bg-clip-text text-transparent select-none"
            style={{
              fontFamily: "'Plus Jakarta Sans', sans-serif",
              fontSize: "clamp(3.5rem, 16vw, 220px)",
              lineHeight: "clamp(3.8rem, 17vw, 240px)",
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
      <div className="flex flex-col gap-4 items-start shrink-0 w-full max-w-[342px] z-10 pb-4 md:pb-0">
        <p className="font-bold text-xl sm:text-2xl md:text-[length:var(--text-heading-lg-size)] leading-snug md:leading-[var(--text-heading-3xl-line-height)] text-text-invert">
          Encuentra tu próxima lancha en el Delta
        </p>
        <button
          type="button"
          onClick={() =>
            document
              .getElementById("lanchas-destacadas")
              ?.scrollIntoView({ behavior: "smooth" })
          }
          className="flex items-center justify-center gap-2 h-12 px-6 py-2 rounded-full bg-bg-fill-primary text-text-invert font-medium text-[length:var(--text-subtitle-sm-size)] leading-[var(--text-subtitle-sm-line-height)] transition-colors hover:bg-bg-fill-primary-hover w-full sm:w-auto cursor-pointer"
        >
          <span>Explorar lanchas</span>
          <div className="relative size-6 flex items-center justify-center">
            <BsArrowDown aria-hidden="true" className="h-5 w-5 shrink-0 text-white" />
          </div>
        </button>
      </div>
    </section>
  );
}
