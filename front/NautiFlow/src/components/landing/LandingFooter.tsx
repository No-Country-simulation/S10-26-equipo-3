import wave1 from "../../assets/landing/water-wave-1.svg";
import wave2 from "../../assets/landing/water-wave-2.svg";

export default function LandingFooter() {
  return (
    <footer className="relative flex flex-col w-full shrink-0">
      {/* NautiFlow wordmark fading */}
      <div
        className="font-bold opacity-80 bg-clip-text text-transparent text-center w-full flex items-center justify-center py-6 sm:py-10 md:py-12 select-none"
        style={{
          fontFamily: "'Plus Jakarta Sans', sans-serif",
          fontSize: "clamp(3.5rem, 16vw, 230px)",
          lineHeight: "clamp(3.5rem, 16vw, 180px)",
          backgroundImage: "linear-gradient(to bottom, white, #205362)",
        }}
      >
        NautiFlow
      </div>

      {/* Water animation */}
      <div className="relative h-[158px] overflow-clip w-full shrink-0 pointer-events-none select-none">
        <style>{`
          @keyframes wave-sway-1 {
            0% {
              transform: translate3d(0, 0, 0);
            }
            50% {
              transform: translate3d(260px, -10px, 0);
            }
            100% {
              transform: translate3d(0, 0, 0);
            }
          }

          @keyframes wave-sway-2 {
            0% {
              transform: translate3d(0, 0, 0);
            }
            50% {
              transform: translate3d(-220px, 8px, 0);
            }
            100% {
              transform: translate3d(0, 0, 0);
            }
          }

          @keyframes wave-sway-3 {
            0% {
              transform: translate3d(0, 0, 0);
            }
            50% {
              transform: translate3d(180px, -6px, 0);
            }
            100% {
              transform: translate3d(0, 0, 0);
            }
          }

          .animate-wave-1 {
            animation: wave-sway-1 4.5s ease-in-out infinite;
            will-change: transform;
          }

          .animate-wave-2 {
            animation: wave-sway-2 6s ease-in-out infinite;
            will-change: transform;
          }

          .animate-wave-3 {
            animation: wave-sway-3 7.5s ease-in-out infinite;
            will-change: transform;
          }
        `}</style>

        {/* Capa de ola 1 */}
        <div
          className="absolute mix-blend-multiply animate-wave-1"
          style={{
            height: "401px",
            left: "-2000px",
            top: "20px",
            width: "4485px",
          }}
        >
          <img
            src={wave1}
            alt=""
            className="absolute inset-0 size-full max-w-none block"
          />
        </div>

        {/* Capa de ola 2 */}
        <div
          className="absolute mix-blend-multiply animate-wave-2"
          style={{
            height: "401px",
            left: "-1800px",
            top: "10px",
            width: "4485px",
          }}
        >
          <img
            src={wave2}
            alt=""
            className="absolute inset-0 size-full max-w-none block"
          />
        </div>

        {/* Capa de ola 3 */}
        <div
          className="absolute mix-blend-multiply animate-wave-3"
          style={{
            height: "401px",
            left: "-1500px",
            top: "25px",
            width: "4485px",
          }}
        >
          <img
            src={wave1}
            alt=""
            className="absolute inset-0 size-full max-w-none block -scale-x-100"
          />
        </div>
      </div>

      {/* Copyright bar */}
      <div className="flex flex-col items-center sm:items-start justify-center w-full px-4 sm:px-8 md:px-[60px] py-6 sm:py-8 md:py-10 bg-bg-fill-primary shrink-0 text-center sm:text-left">
        <p className="font-semibold text-xs sm:text-sm text-text-invert w-full">
          @2026 NautiFlow. Todos los derechos reservados.
        </p>
      </div>
    </footer>
  );
}
