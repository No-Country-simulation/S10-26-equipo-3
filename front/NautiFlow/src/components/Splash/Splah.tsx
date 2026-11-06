import { useEffect, useState } from "react";
import Logo from "../Logo";
 
interface SplashProps {
  onFinish: () => void;
}
 
type Phase = "idle" | "drawing" | "fading";
 
// Tiempos de la animación (ms)
const WAIT = 600; // solo el logo
const DRAW = 1400; // la línea baja
const HOLD = 200; // pausa con la línea completa
const FADE = 500; // la línea y la pantalla desaparecen
 

const LINE_PATH =
  "M0,0 C500,0 1180,20 1150,160 C1120,280 180,300 180,500 C180,720 900,640 1280,843";
 
export default function Splash({ onFinish }: SplashProps) {
  const [phase, setPhase] = useState<Phase>("idle");
 
  useEffect(() => {
    const reduceMotion = window.matchMedia(
      "(prefers-reduced-motion: reduce)"
    ).matches;
 
    
    if (reduceMotion) {
      const t = setTimeout(onFinish, WAIT);
      return () => clearTimeout(t);
    }
 
    const timers = [
      setTimeout(() => setPhase("drawing"), WAIT),
      setTimeout(() => setPhase("fading"), WAIT + DRAW + HOLD),
      setTimeout(onFinish, WAIT + DRAW + HOLD + FADE),
    ];
    return () => timers.forEach(clearTimeout);
  }, [onFinish]);
 
  return (
    <div
      role="status"
      aria-label="Cargando"
      className="fixed inset-0 z-50 grid place-items-center overflow-hidden bg-bg-fill-secondary"
      style={{
        opacity: phase === "fading" ? 0 : 1,
        transition: `opacity ${FADE}ms ease-out`,
      }}
    >
      
      <svg
        className="absolute inset-0 h-full w-full"
        viewBox="0 0 1280 843"
        preserveAspectRatio="none"
        aria-hidden="true"
      >
        <path
          d={LINE_PATH}
          pathLength={1}
          fill="none"
          className="text-bg-fill-primary"
          stroke="currentColor"
          strokeWidth={3}
          strokeLinecap="round"
          strokeDasharray="1"
          style={{
            strokeDashoffset: phase === "idle" ? 1 : 0,
            transition: `stroke-dashoffset ${DRAW}ms ease-in-out`,
          }}
        />
      </svg>
 
      
      <div className="relative w-14 sm:w-16 [&>*]:h-auto [&>*]:w-full">
        <Logo />
      </div>
    </div>
  );
}