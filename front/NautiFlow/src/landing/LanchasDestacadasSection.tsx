import { useState } from "react";
import chevronLeftIcon from "../assets/landing/chevron-left.svg";
import chevronRightIcon from "../assets/landing/chevron-right.svg";
import seccionesDotsIcon from "../assets/landing/secciones-dots.svg";
import WaterAnimation from "./WaterAnimation";
import LanchaCard from "./LanchaCard";
import lanchaCadImg from "../assets/landing/lancha-card.png";

const LANCHAS = [
  {
    id: 1,
    nombre: "La Porteña III",
    lanchero: "Juan Pablo Gómez González",
    rating: 4.0,
    reseñas: 36,
    capacidad: 12,
    verificado: true,
    esNuevo: false,
    imagen: lanchaCadImg,
  },
  {
    id: 2,
    nombre: "La Porteña III",
    lanchero: "Juan Pablo Gómez González",
    rating: 4.0,
    reseñas: 36,
    capacidad: 12,
    verificado: true,
    esNuevo: false,
    imagen: lanchaCadImg,
  },
  {
    id: 3,
    nombre: "La Porteña III",
    lanchero: "Juan Pablo Gómez González",
    rating: 4.0,
    reseñas: 36,
    capacidad: 12,
    verificado: true,
    esNuevo: true,
    imagen: lanchaCadImg,
  },
];

export default function LanchasDestacadasSection() {
  const [activeIndex, setActiveIndex] = useState(0);
  const visibleCount = 3;

  const handlePrev = () => setActiveIndex((i) => Math.max(0, i - 1));
  const handleNext = () =>
    setActiveIndex((i) => Math.min(LANCHAS.length - visibleCount, i + 1));

  return (
    <section className="flex flex-col gap-12 items-center w-full overflow-clip shrink-0">
      {/* Wave decorativa invertida */}
      <div className="flex items-center justify-center w-full">
        <WaterAnimation flipped />
      </div>

      {/* Encabezado */}
      <div className="flex flex-col items-center w-[516px] shrink-0">
        <h2 className="font-bold text-[length:var(--text-heading-2xl-size)] leading-[var(--text-heading-2xl-line-height)] text-bg-fill-primary text-center w-full">
          Encuentra la lancha perfecta para tu próxima aventura
        </h2>
      </div>

      {/* Carrusel */}
      <div className="flex gap-1.5 items-center justify-center px-[60px] w-full shrink-0">
        {/* Chevron left */}
        <button
          type="button"
          onClick={handlePrev}
          disabled={activeIndex === 0}
          aria-label="Anterior"
          className="shrink-0 disabled:opacity-30 transition-opacity"
        >
          <div className="relative h-8 w-7">
            <img
              src={chevronLeftIcon}
              alt=""
              className="absolute inset-0 size-full max-w-none block"
            />
          </div>
        </button>

        {/* Cards */}
        <div className="flex flex-1 gap-5 items-center min-w-0">
          {LANCHAS.slice(activeIndex, activeIndex + visibleCount).map(
            (lancha) => (
              <LanchaCard key={lancha.id} {...lancha} />
            )
          )}
        </div>

        {/* Chevron right */}
        <button
          type="button"
          onClick={handleNext}
          disabled={activeIndex >= LANCHAS.length - visibleCount}
          aria-label="Siguiente"
          className="shrink-0 disabled:opacity-30 transition-opacity"
        >
          <div className="relative h-8 w-7">
            <img
              src={chevronRightIcon}
              alt=""
              className="absolute inset-0 size-full max-w-none block"
            />
          </div>
        </button>
      </div>

      {/* Dots de paginación */}
      <div className="relative shrink-0" style={{ height: "10px", width: "46px" }}>
        <img
          src={seccionesDotsIcon}
          alt=""
          className="absolute inset-0 size-full max-w-none block"
        />
      </div>
    </section>
  );
}
