import { BsArrowDown } from "react-icons/bs";

// Video de fondo del diseño (Figma: "Hero Desktop"), exportado a 720p sin audio; el póster es su primer cuadro.
// Con prefers-reduced-motion se muestra el póster fijo en lugar del video.
const VIDEO_FONDO = "/landing/hero-delta.mp4";
const POSTER_FONDO = "/landing/hero-delta.png";

export default function Hero() {
    return (
        <section className="relative isolate flex min-h-dvh flex-col overflow-hidden bg-linear-to-b from-bg-fill-primary to-bg-fill-primary-hover font-inter">
            <video
                src={VIDEO_FONDO}
                poster={POSTER_FONDO}
                autoPlay
                muted
                loop
                playsInline
                aria-hidden="true"
                className="absolute inset-0 -z-20 h-full w-full object-cover motion-reduce:hidden"
            />
            <img src={POSTER_FONDO} alt="" aria-hidden="true" className="absolute inset-0 -z-20 hidden h-full w-full object-cover motion-reduce:block" />
            <div aria-hidden="true" className="absolute inset-0 -z-10 bg-linear-to-b from-[#666666]/0 to-black/60" />

            <p aria-hidden="true"
                className="pointer-events-none absolute inset-x-0 top-[38%] -translate-y-1/2 select-none bg-linear-to-b from-white to-white/15 bg-clip-text text-center font-jakarta text-[length:min(17vw,244px)] font-bold leading-none tracking-[-0.4px] text-transparent opacity-80"
            >
                NautiFlow
            </p>

            <div className="mt-auto flex flex-col items-start gap-4 px-4 pb-[91px] md:gap-6 md:px-[60px]">
                <h1 className="max-w-[342px] text-[28px] font-bold leading-8 text-text-invert">
                    Encontrá tu próxima lancha en el Delta
                </h1>
          

                <a href="#lanchas-destacadas"
                    className="inline-flex h-10 items-center gap-2 rounded-full bg-bg-fill-primary px-4 text-sm font-medium leading-5 text-text-invert hover:bg-bg-fill-primary-hover focus-visible:ring-2 focus-visible:ring-text-invert md:h-12 md:text-lg md:leading-7"
                >
                    Explorar lanchas
                    <BsArrowDown aria-hidden="true" className="h-4 w-4 md:h-5 md:w-5" />
                </a>
            </div>
        </section>
    );
}
