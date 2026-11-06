import arrowCircleRight from "../assets/landing/arrow-circle-right.svg";

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
      className="relative flex flex-col items-center justify-center gap-2.5 h-40 w-[360px] shrink-0 rounded-l overflow-clip px-4 py-2.5 cursor-pointer"
      onClick={onClick}
    >
      {/* Background image */}
      <img
        src={imagen}
        alt={nombre}
        className="absolute inset-0 size-full max-w-none object-cover pointer-events-none rounded-l"
      />

      {/* Gradient overlay */}
      <div className="absolute inset-0 rounded-l bg-gradient-to-b from-[rgba(217,217,217,0)] to-bg-surface-invert" />

      {/* Title */}
      <p
        className="absolute font-semibold text-[length:var(--text-subtitle-sm-size)] leading-[var(--text-heading-lg-line-height)] text-white whitespace-nowrap"
        style={{ bottom: "70px", left: "16px", transform: "translateY(50%)" }}
      >
        {nombre}
      </p>

      {/* Description */}
      <p
        className="absolute font-normal text-[length:var(--text-body-sm-regular-size)] leading-[var(--text-body-sm-regular-line-height)] text-white w-[260px]"
        style={{ bottom: "28px", left: "16px", transform: "translateY(50%)" }}
      >
        {descripcion}
      </p>

      {/* Arrow icon */}
      <div
        className="absolute size-6"
        style={{ right: "12px", top: "115px" }}
      >
        <img
          src={arrowCircleRight}
          alt=""
          className="absolute inset-0 size-full max-w-none block"
        />
      </div>
    </div>
  );
}
