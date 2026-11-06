import wave5 from "../assets/landing/water-wave-5.svg";
import wave3 from "../assets/landing/water-wave-3.svg";

export default function LandingFooter() {
  return (
    <footer className="relative flex flex-col w-full shrink-0">
      {/* NautiFlow wordmark fading */}
      <div
        className="font-bold opacity-80 bg-clip-text text-transparent text-center w-full h-[230px] flex items-center justify-center"
        style={{
          fontFamily: "'Plus Jakarta Sans', sans-serif",
          fontSize: "230px",
          lineHeight: "161px",
          backgroundImage: "linear-gradient(to bottom, white, #205362)",
        }}
      >
        NautiFlow
      </div>

      {/* Water animation */}
      <div className="relative h-[158px] overflow-clip w-full shrink-0">
        {/* Layer 4-3 */}
        <div
          className="absolute mix-blend-multiply"
          style={{
            height: "401.363px",
            left: "-3925.01px",
            top: "20.85px",
            width: "4485.356px",
          }}
        >
          <img src={wave5} alt="" className="absolute inset-0 size-full max-w-none block" />
        </div>
        <div
          className="absolute mix-blend-multiply"
          style={{
            height: "401.363px",
            left: "-3549.71px",
            top: "20.85px",
            width: "4485.356px",
          }}
        >
          <img src={wave5} alt="" className="absolute inset-0 size-full max-w-none block" />
        </div>

        {/* Layer 2 */}
        <div
          className="absolute flex items-center justify-center mix-blend-multiply"
          style={{
            height: "401.363px",
            left: "-143.34px",
            top: "20.85px",
            width: "4485.356px",
          }}
        >
          <div className="-scale-y-100 flex-none rotate-180">
            <div
              className="relative"
              style={{ height: "401.363px", width: "4485.356px" }}
            >
              <img src={wave3} alt="" className="absolute inset-0 size-full max-w-none block" />
            </div>
          </div>
        </div>

        {/* Layer 1 */}
        <div
          className="absolute flex items-center justify-center mix-blend-multiply"
          style={{
            height: "401.363px",
            left: "-255.41px",
            top: "20.85px",
            width: "4485.356px",
          }}
        >
          <div className="-scale-y-100 flex-none rotate-180">
            <div
              className="relative"
              style={{ height: "401.363px", width: "4485.356px" }}
            >
              <img src={wave3} alt="" className="absolute inset-0 size-full max-w-none block" />
            </div>
          </div>
        </div>
      </div>

      {/* Copyright bar */}
      <div className="flex flex-col items-start justify-center w-full overflow-clip px-[60px] py-10 bg-bg-fill-primary shrink-0">
        <p className="font-semibold text-[length:var(--text-caption-semibold-size)] leading-[var(--text-caption-semibold-line-height)] text-text-invert w-full">
          @2026 NautiFlow. Todos los derechos reservados.
        </p>
      </div>
    </footer>
  );
}
