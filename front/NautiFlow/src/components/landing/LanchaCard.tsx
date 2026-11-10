import lanchaCadImg from "../../assets/landing/lancha-card.png";
import { BsArrowRight, BsCheck, BsPersonCheck, BsFillStarFill } from "react-icons/bs";
type LanchaCardProps = {
  nombre: string;
  lanchero: string;
  rating?: number | null;
  reseñas?: number | null;
  verificado: boolean;
  /** "nuevo" muestra badge de nuevo en lugar de rating con estrellas */
  esNuevo?: boolean;
  imagen?: string | null;
  onVerDetalle?: () => void;
};

export default function LanchaCard({
  nombre,
  lanchero,
  rating,
  reseñas,
  verificado,
  esNuevo = false,
  imagen = lanchaCadImg,
  onVerDetalle,
}: LanchaCardProps) {
  const imgSrc = imagen || lanchaCadImg;
  const ratingNum = typeof rating === "number" && !isNaN(rating) ? rating : 0;
  const resenasNum = typeof reseñas === "number" && !isNaN(reseñas) ? reseñas : 0;
  const esLanchaNueva = esNuevo || resenasNum === 0 || rating == null;

  return (
    <div className="relative flex flex-col flex-1 min-w-0 w-full max-w-sm sm:max-w-none mx-auto bg-bg-surface-primary rounded-m shadow-[0px_12px_8px_rgba(12,12,13,0.08),0px_4px_2px_rgba(12,12,13,0.05)] px-2.5 py-1">
      {/* Imagen */}
      <div
        className="relative rounded-xl mb-[-50px] w-full shrink-0"
        style={{ aspectRatio: "346/275" }}
      >
        <div className="absolute inset-0 rounded-xl overflow-hidden pointer-events-none">
          <img
            src={imgSrc}
            alt={nombre}
            className="absolute max-w-none"
            style={{
              height: "99.24%",
              left: "-17.56%",
              top: "0.21%",
              width: "139.8%",
            }}
          />
        </div>
        <div className="absolute inset-0 rounded-xl bg-gradient-to-b from-[rgba(102,102,102,0)] to-[rgba(0,0,0,0.5)]" />
      </div>

      {/* Badge verificado */}
      {verificado && (
        <div className="absolute top-3 right-4 flex items-start gap-1 bg-bg-fill-primary rounded-full px-1 py-0.5 overflow-clip z-10">
          <BsCheck aria-hidden="true" className="h-5 w-5 shrink-0 text-text-invert" />
          <span className="font-bold text-[length:var(--text-caption-bold-size)] leading-[var(--text-caption-bold-line-height)] text-text-invert whitespace-nowrap">
            Verificado
          </span>
        </div>
      )}

      {/* Card body */}
      <div className="relative z-10 flex flex-col gap-6 p-4 w-full shrink-0">
        {/* Rating row */}
        <div className="flex items-center justify-between w-full">
          <div className="flex items-center gap-1">
            {esLanchaNueva ? (
              <div className="flex items-center gap-1 border border-white/60 rounded-sm px-1.5 py-0.5">
                <span className="font-semibold text-xs leading-4 text-white whitespace-nowrap">
                  Nuevo
                </span>
              </div>
            ) : (
              <div className="flex items-center gap-1">
                <BsFillStarFill aria-hidden="true" className="h-4 w-4 shrink-0 text-bg-warning-primary" />

                <p className="font-semibold text-[length:var(--text-body-sm-size)] leading-[var(--text-heading-lg-line-height)] text-white whitespace-nowrap">
                  <span>{ratingNum.toFixed(1)} </span>
                  <span className="text-white/80 font-normal">({resenasNum} reseñas)</span>
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Header row */}
        <div className="flex items-center justify-between w-full">
          <div className="flex flex-col flex-1 min-w-0 items-start justify-center">
            <p className="font-semibold text-[length:var(--text-subtitle-sm-size)] leading-[var(--text-heading-lg-line-height)] text-text-primary whitespace-nowrap">
              {nombre}
            </p>
            <div className="flex items-center gap-2 w-full">
              <BsPersonCheck aria-hidden="true" className="h-4 w-4 shrink-0 text-text-secondary" />
              <p className="font-normal text-[length:var(--text-body-sm-regular-size)] leading-[var(--text-body-sm-regular-line-height)] text-text-secondary whitespace-nowrap">
                {lanchero}
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={onVerDetalle}
            aria-label={`Ver detalle de ${nombre}`}
            className="flex items-center justify-center shrink-0 size-10 rounded-full bg-bg-fill-primary p-1 transition-colors hover:bg-bg-fill-primary-hover"
          >
            <BsArrowRight aria-hidden="true" className="h-5 w-5 shrink-0 text-text-invert" />
          </button>
        </div>
      </div>
    </div>
  );
}
