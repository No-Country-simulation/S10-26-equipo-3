import { useEffect, useState } from "react";
import lanchaCadImg from "../../assets/landing/lancha-card.png";
import {
  BsFillStarFill,
  BsStarHalf,
  BsStar,
  BsPersonCheck,
  BsCheck,
  BsXLg,
} from "react-icons/bs";
import { lanchasService, type ResenaLancha } from "../../services/lanchasService";

/** Una reseña individual (tipo interno del modal) */
export type Resena = {
  /** Calificación de 0 a 5 (soporta decimales, ej. 4.5) */
  estrellas: number;
  texto: string;
  /** Nombre del autor (opcional) */
  autor?: string;
};

type ModalResenasProps = {
  idLanchero: number;
  nombre: string;
  lanchero: string;
  rating: number;
  totalReseñas: number;
  verificado?: boolean;
  imagen?: string;
  onClose: () => void;
};

/** Renderiza entre 1 y 5 estrellas (llenas, mitad, vacías) según el valor. */
function StarRating({ value, size = "size-4" }: { value: number; size?: string }) {
  return (
    <div className="flex items-center gap-0.5" aria-label={`${value.toFixed(1)} estrellas`}>
      {Array.from({ length: 5 }, (_, i) => {
        const filled = value >= i + 1;
        const half = !filled && value >= i + 0.5;
        if (filled)
          return (
            <BsFillStarFill
              key={i}
              aria-hidden="true"
              className={`${size} shrink-0 text-bg-warning-primary`}
            />
          );
        if (half)
          return (
            <BsStarHalf
              key={i}
              aria-hidden="true"
              className={`${size} shrink-0 text-bg-warning-primary`}
            />
          );
        return (
          <BsStar
            key={i}
            aria-hidden="true"
            className={`${size} shrink-0 text-border-secondary`}
          />
        );
      })}
    </div>
  );
}

type EstadoResenas =
  | { tipo: "cargando" }
  | { tipo: "error"; mensaje: string }
  | { tipo: "ok"; resenas: ResenaLancha[] };

/** Modal de reseñas de una lancha — fiel al frame Figma 1819:4589 */
export default function ModalResenas({
  idLanchero,
  nombre,
  lanchero,
  rating,
  totalReseñas,
  verificado = false,
  imagen = lanchaCadImg,
  onClose,
}: ModalResenasProps) {
  const [estado, setEstado] = useState<EstadoResenas>({ tipo: "cargando" });

  useEffect(() => {
    setEstado({ tipo: "cargando" });
    lanchasService
      .getResenasByLanchero(idLanchero)
      .then((data) => setEstado({ tipo: "ok", resenas: data.content }))
      .catch((err: unknown) => {
        const mensaje = err instanceof Error ? err.message : "Error inesperado.";
        setEstado({ tipo: "error", mensaje });
      });
  }, [idLanchero]);

  return (
    /* Overlay */
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-label={`Reseñas de ${nombre}`}
      onClick={(e) => {
        // Cerrar al hacer clic fuera del panel
        if (e.target === e.currentTarget) onClose();
      }}
    >
      {/* Panel */}
      <div className="relative flex flex-col sm:flex-row w-full max-w-[720px] bg-bg-surface-primary rounded-xl p-4 shadow-[0px_24px_48px_rgba(12,12,13,0.18),0px_8px_16px_rgba(12,12,13,0.10)] overflow-hidden">
        {/* Botón cerrar — siempre fijo en la esquina superior derecha del modal (tanto en mobile como desktop) */}
        <button
          type="button"
          onClick={onClose}
          aria-label="Cerrar modal de reseñas"
          className="absolute top-6 right-6 sm:top-5 sm:right-5 z-30 flex items-center justify-center size-8 rounded-full bg-white/90 sm:bg-transparent text-text-secondary hover:bg-bg-fill-secondary hover:text-text-primary shadow-sm sm:shadow-none transition-colors cursor-pointer"
        >
          <BsXLg aria-hidden="true" className="size-4 shrink-0" />
        </button>

        {/* ── COLUMNA IZQUIERDA: foto con stats superpuestos ── */}
        <div className="relative shrink-0 w-full sm:w-[260px] " style={{ aspectRatio: "346/275" }}>
          {/* Foto a pantalla completa */}
          <img
            src={imagen}
            alt={nombre}
            className="absolute inset-0 w-full h-full object-cover rounded-xl"
          />

          {/* Gradiente inferior para legibilidad de stats */}
          <div className="absolute inset-0 bg-gradient-to-b from-transparent via-transparent to-black/60 rounded-xl" />

          {/* Badge verificado — esquina superior derecha en desktop, a la izquierda en mobile para no colisionar con el botón cerrar */}
          {verificado && (
            <div className="absolute top-3 left-3 sm:left-auto sm:right-3 flex items-center gap-1 bg-bg-fill-primary rounded-full px-2 py-0.5 z-10">
              <BsCheck aria-hidden="true" className="size-4 shrink-0 text-text-invert" />
              <span className="text-[length:var(--text-caption-bold-size)] leading-[var(--leading-caption-bold-line-height)] font-bold text-text-invert whitespace-nowrap">
                Verificado
              </span>
            </div>
          )}

          {/* Stats: rating + total reseñas — superpuestos en la parte inferior */}
          <div className="absolute bottom-3 left-3 flex items-center gap-3 z-10">
            {/* Rating numérico */}
            <div className="flex items-center gap-1.5">
              <BsFillStarFill aria-hidden="true" className="size-5 shrink-0 text-bg-warning-primary" />
              <span className="font-semibold text-[length:var(--text-subtitle-sm-size)] leading-[var(--leading-subtitle-sm-line-height)] text-white">
                {rating.toFixed(1)}
              </span>
            </div>

            {/* Total reseñas */}
            <span className="font-normal text-[length:var(--text-body-sm-regular-size)] leading-[var(--leading-body-sm-regular-line-height)] text-white/80">
              ({totalReseñas} reseñas)
            </span>
          </div>
        </div>

        {/* ── COLUMNA DERECHA: header + lista reseñas ── */}
        <div className="flex flex-col flex-1 min-w-0">
          {/* Header */}
          <div className="flex items-start justify-between gap-4 px-5 pt-5 pb-4 border-b border-border-primary">
            <div className="flex flex-col gap-0.5 min-w-0 pr-8">
              <h2 className="font-semibold text-[length:var(--text-subtitle-md-size)] leading-[var(--leading-subtitle-md-line-height)] text-text-primary truncate">
                {nombre}
              </h2>
              <div className="flex items-center gap-1.5">
                <BsPersonCheck aria-hidden="true" className="size-4 shrink-0 text-text-secondary" />
                <span className="font-normal text-[length:var(--text-body-sm-regular-size)] leading-[var(--leading-body-sm-regular-line-height)] text-text-secondary truncate">
                  {lanchero}
                </span>
              </div>
            </div>
          </div>

          {/* Lista de reseñas (scrollable) */}
          {estado.tipo === "cargando" && (
            <div className="flex items-center justify-center flex-1 py-10 px-5">
              <span className="text-[length:var(--text-body-sm-regular-size)] text-text-secondary animate-pulse">
                Cargando reseñas…
              </span>
            </div>
          )}

          {estado.tipo === "error" && (
            <div className="flex items-center justify-center flex-1 py-10 px-5">
              <span className="text-[length:var(--text-body-sm-regular-size)] text-text-error text-center">
                {estado.mensaje}
              </span>
            </div>
          )}

          {estado.tipo === "ok" && estado.resenas.length === 0 && (
            <div className="flex items-center justify-center flex-1 py-10 px-5">
              <span className="text-[length:var(--text-body-sm-regular-size)] text-text-secondary text-center">
                Esta lancha aún no tiene reseñas.
              </span>
            </div>
          )}

          {estado.tipo === "ok" && estado.resenas.length > 0 && (
            <ul
              className="flex flex-col overflow-y-auto max-h-[273px] divide-y divide-border-primary px-5"
              aria-label="Lista de reseñas"
            >
              {estado.resenas.map((resena) => (
                <li key={resena.idResena} className="flex flex-col gap-1.5 py-4">
                  <StarRating value={resena.puntuacion} size="size-3.5" />
                  <p className="font-normal text-[length:var(--text-body-sm-regular-size)] leading-[var(--leading-body-sm-regular-line-height)] text-text-primary">
                    {resena.comentario}
                  </p>
                  <span className="text-[length:var(--text-caption-bold-size)] text-text-secondary">
                    {new Date(resena.fecha).toLocaleDateString("es-AR", {
                      day: "numeric",
                      month: "long",
                      year: "numeric",
                    })}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
