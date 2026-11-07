import { useEffect, useState } from "react";
import waterAnimation from "../../assets/landing/water-animation.svg";
import LanchaCard from "./LanchaCard";
import { BsChevronLeft, BsChevronRight } from "react-icons/bs";
import {
  lanchasService,
  type LanchaDestacada,
} from "../../services/lanchasService";

type Estado =
  | { tipo: "cargando" }
  | { tipo: "error"; mensaje: string }
  | { tipo: "vacio" }
  | { tipo: "ok"; lanchas: LanchaDestacada[]; totalPages: number };

export default function LanchasDestacadasSection() {
  const [estado, setEstado] = useState<Estado>({ tipo: "cargando" });
  const [page, setPage] = useState(0);
  const PAGE_SIZE = 3;

  useEffect(() => {
    setEstado({ tipo: "cargando" });

    lanchasService
      .getLanchasDestacadas(page, PAGE_SIZE)
      .then((data) => {
        if (data.content.length === 0) {
          setEstado({ tipo: "vacio" });
        } else {
          setEstado({
            tipo: "ok",
            lanchas: data.content,
            totalPages: data.totalPages,
          });
        }
      })
      .catch((err: unknown) => {
        const mensaje =
          err instanceof Error ? err.message : "Error inesperado.";
        setEstado({ tipo: "error", mensaje });
      });
  }, [page]);

  const totalPages = estado.tipo === "ok" ? estado.totalPages : 0;

  return (
    <section id="lanchas-destacadas" className="flex flex-col gap-8 md:gap-12 items-center w-full overflow-clip shrink-0 ">
      <div className="flex items-center justify-center w-full">
        <img
          src={waterAnimation}
          alt=""
          className="size-full max-w-none block object-cover"
        />
      </div>

      {/* Encabezado */}
      <div className="flex flex-col items-center w-full max-w-[516px] px-4 shrink-0">
        <h2 className="font-bold text-2xl sm:text-3xl md:text-[length:var(--text-heading-2xl-size)] leading-snug md:leading-[var(--text-heading-2xl-line-height)] text-bg-fill-primary text-center w-full">
          Encuentra la lancha perfecta para tu próxima aventura
        </h2>
      </div>

      {/* Estados: cargando / error / vacío / ok */}
      {estado.tipo === "cargando" && (
        <div className="flex items-center justify-center w-full py-16">
          <span className="text-text-secondary text-[length:var(--text-body-md-regular-size)] animate-pulse">
            Cargando lanchas…
          </span>
        </div>
      )}

      {estado.tipo === "error" && (
        <div className="flex items-center justify-center w-full py-16">
          <span className="text-text-error text-[length:var(--text-body-md-regular-size)]">
            {estado.mensaje}
          </span>
        </div>
      )}

      {estado.tipo === "vacio" && (
        <div className="flex items-center justify-center w-full py-16">
          <span className="text-text-secondary text-[length:var(--text-body-md-regular-size)]">
            No hay lanchas destacadas por el momento.
          </span>
        </div>
      )}

      {estado.tipo === "ok" && (
        <>
          {/* Carrusel */}
          <div className="flex gap-2 sm:gap-4 items-center justify-center px-2 sm:px-6 md:px-[60px] w-full max-w-7xl mx-auto shrink-0">
            {/* Chevron left */}
            <button
              type="button"
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              aria-label="Página anterior"
              className="shrink-0 size-9 sm:size-10 flex items-center justify-center rounded-full bg-white/90 shadow-sm border border-border-primary text-text-primary disabled:opacity-30 transition-opacity cursor-pointer hover:bg-white"
            >
              <BsChevronLeft aria-hidden="true" className="size-4 sm:size-5 shrink-0" />
            </button>

            {/* Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-5 flex-1 min-w-0 items-stretch">
              {estado.lanchas.map((lancha) => (
                <LanchaCard
                  key={lancha.idLancha}
                  nombre={lancha.nombreEmbarcacion}
                  lanchero={lancha.lancheroNombreCompleto}
                  rating={lancha.promedioCalificacion}
                  reseñas={lancha.totalResenas}
                  capacidad={0}
                  verificado={lancha.verificada}
                  esNuevo={lancha.totalResenas === 0}
                  imagen={lancha.fotoPrincipalUrl}
                />
              ))}
            </div>

            {/* Chevron right */}
            <button
              type="button"
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              aria-label="Página siguiente"
              className="shrink-0 size-9 sm:size-10 flex items-center justify-center rounded-full bg-white/90 shadow-sm border border-border-primary text-text-primary disabled:opacity-30 transition-opacity cursor-pointer hover:bg-white"
            >
              <BsChevronRight aria-hidden="true" className="size-4 sm:size-5 shrink-0" />
            </button>
          </div>

          {/* Dots de paginación */}
          <div className="flex items-center justify-center gap-2">
            {Array.from({ length: totalPages }).map((_, index) => (
              <button
                key={index}
                type="button"
                onClick={() => setPage(index)}
                aria-label={`Ir a la página ${index + 1}`}
                className={`rounded-full transition-all duration-300 cursor-pointer ${page === index
                    ? "h-2 w-5 bg-bg-fill-primary"
                    : "size-2 bg-border-secondary hover:bg-border-tertiary"
                  }`}
              />
            ))}
          </div>
        </>
      )}
    </section>
  );
}
