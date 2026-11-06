import lanchaCadImg from "../assets/landing/lancha-card.png";
import starIcon from "../assets/landing/star.svg";
import usersIcon from "../assets/landing/users.svg";
import userCheckIcon from "../assets/landing/user-check.svg";
import arrowNarrowRightIcon from "../assets/landing/arrow-narrow-right.svg";
import checkIcon from "../assets/landing/check.svg";

type LanchaCardProps = {
  nombre: string;
  lanchero: string;
  rating: number;
  reseñas: number;
  capacidad: number;
  verificado: boolean;
  /** "nuevo" muestra badge de nuevo en lugar de rating con estrellas */
  esNuevo?: boolean;
  imagen?: string;
  onVerDetalle?: () => void;
};

export default function LanchaCard({
  nombre,
  lanchero,
  rating,
  reseñas,
  capacidad,
  verificado,
  esNuevo = false,
  imagen = lanchaCadImg,
  onVerDetalle,
}: LanchaCardProps) {
  return (
    <div className="relative flex flex-col flex-1 min-w-0 bg-bg-surface-primary rounded-m shadow-[0px_12px_8px_rgba(12,12,13,0.08),0px_4px_2px_rgba(12,12,13,0.05)] px-2.5 py-1">
      {/* Imagen */}
      <div
        className="relative rounded-l mb-[-50px] w-full shrink-0"
        style={{ aspectRatio: "346/275" }}
      >
        <div className="absolute inset-0 rounded-l overflow-hidden pointer-events-none">
          <img
            src={imagen}
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
        <div className="absolute inset-0 rounded-l bg-gradient-to-b from-[rgba(102,102,102,0)] to-[rgba(0,0,0,0.5)]" />
      </div>

      {/* Badge verificado */}
      {verificado && (
        <div className="absolute top-3 left-[244px] flex items-start gap-1 bg-bg-fill-primary rounded-full px-1 py-0.5 overflow-clip z-10">
          <div className="relative shrink-0 size-4">
            <img
              src={checkIcon}
              alt=""
              className="absolute inset-0 size-full max-w-none block"
            />
          </div>
          <span className="font-bold text-[length:var(--text-caption-bold-size)] leading-[var(--text-caption-bold-line-height)] text-text-invert whitespace-nowrap">
            Verificado
          </span>
        </div>
      )}

      {/* Card body */}
      <div className="flex flex-col gap-6 p-4 w-full shrink-0">
        {/* Rating row */}
        <div className="flex items-center justify-between w-full">
          <div className="flex items-center gap-1">
            {esNuevo ? (
              <div className="flex items-center gap-1 border border-border-primary rounded-sm px-1 py-0">
                <span className="font-semibold text-base leading-6 text-text-invert whitespace-nowrap">
                  Nuevo
                </span>
              </div>
            ) : (
              <div className="flex items-center gap-1">
                <div className="relative shrink-0 h-full" style={{ width: "24.75px" }}>
                  <img
                    src={starIcon}
                    alt="Calificación"
                    className="absolute inset-0 size-full max-w-none block"
                  />
                </div>
                <p className="font-semibold text-[length:var(--text-subtitle-sm-size)] leading-[var(--text-heading-lg-line-height)] text-text-invert whitespace-nowrap">
                  <span>{rating.toFixed(1)} (</span>
                  <span className="text-border-primary">{reseñas} reseñas</span>
                  <span>)</span>
                </p>
              </div>
            )}
          </div>

          {/* Capacidad */}
          <div className="flex items-center gap-1">
            <div
              className="relative shrink-0 self-stretch"
              style={{ width: "24.75px" }}
            >
              <img
                src={usersIcon}
                alt="Pasajeros"
                className="absolute inset-0 size-full max-w-none block"
              />
            </div>
            <span className="font-semibold text-[length:var(--text-subtitle-sm-size)] leading-[var(--text-heading-lg-line-height)] text-text-invert whitespace-nowrap">
              {capacidad}
            </span>
          </div>
        </div>

        {/* Header row */}
        <div className="flex items-center justify-between w-full">
          <div className="flex flex-col flex-1 min-w-0 items-start justify-center">
            <p className="font-semibold text-[length:var(--text-subtitle-sm-size)] leading-[var(--text-heading-lg-line-height)] text-text-primary whitespace-nowrap">
              {nombre}
            </p>
            <div className="flex items-center gap-1 w-full">
              <div className="relative shrink-0 size-4">
                <img
                  src={userCheckIcon}
                  alt=""
                  className="absolute inset-0 size-full max-w-none block"
                />
              </div>
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
            <div className="relative size-4">
              <img
                src={arrowNarrowRightIcon}
                alt=""
                className="absolute inset-0 size-full max-w-none block"
              />
            </div>
          </button>
        </div>
      </div>
    </div>
  );
}
