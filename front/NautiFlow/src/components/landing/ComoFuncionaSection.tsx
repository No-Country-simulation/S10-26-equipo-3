import type { IconType } from "react-icons";
import { BsSearch, BsCalendar, BsStar } from "react-icons/bs";
import waterAnimation from "../../assets/landing/water-animation.svg";

type Paso = {
  icono: IconType;
  titulo: string;
  descripcion: string;
};

const PASOS: Paso[] = [
  {
    icono: BsSearch,
    titulo: "1. Explora",
    descripcion: "Consulta reseñas y experiencias reales",
  },
  {
    icono: BsCalendar,
    titulo: "2. Elige",
    descripcion: "Planifica tu destino, fecha y horario con nosotros",
  },
  {
    icono: BsStar,
    titulo: "3. Califica",
    descripcion: "Comparte tu experiencia y califica tu viaje",
  },
];

export default function ComoFuncionaSection() {
  return (
    <section className="relative flex flex-col gap-8 md:gap-12 items-center justify-center w-full overflow-clip px-4 sm:px-8 md:px-10 py-10 md:py-16 shrink-0">
      {/* Título */}
      <h2 className="relative z-10 font-bold text-2xl sm:text-3xl md:text-[length:var(--text-heading-2xl-size)] leading-snug md:leading-[var(--text-heading-2xl-line-height)] text-bg-fill-primary text-center w-full min-w-full">
        ¿Cómo funciona NautiFlow?
      </h2>

      {/* Pasos */}
      <div className="relative z-10 flex flex-col md:flex-row gap-8 md:gap-12 items-center justify-center w-full max-w-6xl mx-auto p-2.5">
        {PASOS.map((paso) => (
          <div
            key={paso.titulo}
            className="flex flex-col gap-3 items-center p-2.5 w-full max-w-xs md:w-80 shrink-0"
          >
            {/* Icono */}
            <div className="flex items-center justify-center rounded-full bg-bg-fill-primary overflow-clip p-2.5 size-16 shrink-0 shadow-sm">
              <paso.icono aria-hidden="true" className="size-7 shrink-0 text-text-invert" />
            </div>

            {/* Título */}
            <p className="font-bold text-xl sm:text-2xl md:text-[length:var(--text-heading-3xl-size)] leading-[var(--text-heading-3xl-line-height)] text-bg-fill-primary text-center whitespace-nowrap">
              {paso.titulo}
            </p>

            {/* Descripción */}
            <p className="font-semibold text-sm sm:text-base md:text-[length:var(--text-body-md-semibold-size)] leading-[var(--text-body-md-semibold-line-height)] text-bg-fill-primary text-center">
              {paso.descripcion}
            </p>
          </div>
        ))}
      </div>

      {/* Water animation de fondo */}
      <div className="absolute inset-x-0 bottom-[0px] w-full h-[207px] pointer-events-none z-0 overflow-hidden [transform:rotateX(180deg)]">
        <img
          src={waterAnimation}
          alt=""
          className="size-full object-cover block"
        />
      </div>
    </section>
  );
}
