import arrowCircleRight from "../../assets/landing/arrow-circle-right.svg";

type DestinoCardProps = {
  nombre: string;
  descripcion: string;
  imagen: string;
  onClick?: () => void;
};

export default function DestinoCard({
  nombre,
  descripcion,
  imagen,
  onClick,
}: DestinoCardProps) {
  return (
    <div
      className="relative flex flex-col justify-end h-44 w-full max-w-[380px] mx-auto rounded-xl overflow-hidden cursor-pointer group shadow-sm transition-transform duration-200 hover:-translate-y-1"
      onClick={onClick}
    >
      {/* Background image */}
      <img
        src={imagen}
        alt={nombre}
        className="absolute inset-0 size-full object-cover pointer-events-none transition-transform duration-300 group-hover:scale-105"
      />

      {/* Gradient overlay */}
      <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/30 to-transparent" />

      {/* Content */}
      <div className="relative z-10 p-4 flex items-end justify-between gap-3 w-full">
        <div className="flex flex-col gap-0.5 min-w-0 flex-1">
          <p className="font-semibold text-base sm:text-lg text-white truncate">
            {nombre}
          </p>
          <p className="font-normal text-xs sm:text-sm text-white/80 line-clamp-2">
            {descripcion}
          </p>
        </div>

        <div className="size-6 shrink-0 transition-transform duration-200 group-hover:translate-x-1">
          <img
            src={arrowCircleRight}
            alt=""
            className="size-full block"
          />
        </div>
      </div>
    </div>
  );
}
