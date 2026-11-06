import searchIcon from "../assets/landing/search-sm.svg";
import calendarIcon from "../assets/landing/calendar.svg";
import starIcon from "../assets/landing/star-2.svg";
import WaterAnimation from "./WaterAnimation";

type Paso = {
  icono: string;
  titulo: string;
  descripcion: string;
};

const PASOS: Paso[] = [
  {
    icono: searchIcon,
    titulo: "1. Explora",
    descripcion: "Consulta reseñas y experiencias reales",
  },
  {
    icono: calendarIcon,
    titulo: "2. Elige",
    descripcion: "Planifica tu destino, fecha y horario con nosotros",
  },
  {
    icono: starIcon,
    titulo: "3. Califica",
    descripcion: "Comparte tu experiencia y califica tu viaje",
  },
];

export default function ComoFuncionaSection() {
  return (
    <section className="relative flex flex-col gap-8 items-center justify-center w-full overflow-clip px-10 py-12 shrink-0">
      {/* Título */}
      <h2 className="font-bold text-[length:var(--text-heading-2xl-size)] leading-[var(--text-heading-2xl-line-height)] text-bg-fill-primary text-center w-full min-w-full">
        ¿Cómo funciona NautiFlow?
      </h2>

      {/* Pasos */}
      <div className="flex gap-12 items-center justify-center w-full overflow-clip p-2.5 shrink-0">
        {PASOS.map((paso) => (
          <div
            key={paso.titulo}
            className="flex flex-col gap-3 items-center overflow-clip p-2.5 w-80 shrink-0"
          >
            {/* Icono */}
            <div className="flex items-center justify-center rounded-full bg-bg-fill-primary overflow-clip p-2.5 size-16 shrink-0">
              <div className="relative size-7 shrink-0">
                <img
                  src={paso.icono}
                  alt=""
                  className="absolute inset-0 size-full max-w-none block"
                />
              </div>
            </div>

            {/* Título */}
            <p className="font-bold text-[length:var(--text-heading-3xl-size)] leading-[var(--text-heading-3xl-line-height)] text-bg-fill-primary text-center whitespace-nowrap">
              {paso.titulo}
            </p>

            {/* Descripción */}
            <p className="font-semibold text-[length:var(--text-body-md-semibold-size)] leading-[var(--text-body-md-semibold-line-height)] text-bg-fill-primary text-center">
              {paso.descripcion}
            </p>
          </div>
        ))}
      </div>

      {/* Water animation de fondo */}
      <div className="absolute left-0 overflow-clip" style={{ top: "103px", width: "1280px", height: "207px" }}>
        <WaterAnimation />
      </div>
    </section>
  );
}
