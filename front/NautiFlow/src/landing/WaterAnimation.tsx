import wave4 from "../assets/landing/water-wave-4.svg";
import wave2 from "../assets/landing/water-wave-2.svg";
import wave1 from "../assets/landing/water-wave-1.svg";

type WaterAnimationProps = {
  /** Flip the wave vertically for section separators */
  flipped?: boolean;
  className?: string;
};

export default function WaterAnimation({
  flipped = false,
  className = "",
}: WaterAnimationProps) {
  return (
    <div
      className={`h-[158px] overflow-clip relative w-full ${flipped ? "rotate-180" : ""} ${className}`}
    >
      {/* Layer 4 */}
      <div
        className="absolute mix-blend-multiply"
        style={{
          height: "401.363px",
          left: "-3925.01px",
          top: "20.85px",
          width: "4485.356px",
        }}
      >
        <img
          src={wave4}
          alt=""
          className="absolute inset-0 size-full max-w-none block"
        />
      </div>

      {/* Layer 3 */}
      <div
        className="absolute mix-blend-multiply"
        style={{
          height: "401.363px",
          left: "-3549.71px",
          top: "20.85px",
          width: "4485.356px",
        }}
      >
        <img
          src={wave4}
          alt=""
          className="absolute inset-0 size-full max-w-none block"
        />
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
            <img
              src={wave2}
              alt=""
              className="absolute inset-0 size-full max-w-none block"
            />
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
            <img
              src={wave1}
              alt=""
              className="absolute inset-0 size-full max-w-none block"
            />
          </div>
        </div>
      </div>
    </div>
  );
}
