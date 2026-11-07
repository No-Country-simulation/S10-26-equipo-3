type Props = {
  estado: string;
};

const estilosPorEstado: Record<string, string> = {
  PENDIENTE: "bg-[#fb9c2d] text-white",
  APROBADO: "bg-[#205362] text-white",
  RECHAZADO: "bg-[#EA243E] text-white",
  EN_VIAJE: "!border-border-info bg-white text-bg-fill-primary",
  FINALIZADO: "!border-border-info bg-bg-fill-primary text-text-invert",
};

const etiquetasPorEstado: Record<string, string> = {
  PENDIENTE: "Pendiente",
  APROBADO: "Aprobado",
  RECHAZADO: "Rechazado",
  EN_VIAJE: "EN VIAJE",
  FINALIZADO: "FINALIZADO",
};

export default function EstadoBadge({ estado }: Props) {
  const enViaje = estado === "EN_VIAJE";
  return (
    <span
      className={`inline-flex min-h-6 min-w-0 items-center justify-center whitespace-nowrap rounded-full border border-border-secondary px-2.5 py-1 text-xs font-semibold leading-4 ${
        estilosPorEstado[estado] ?? "bg-bg-surface-secondary text-text-secondary"
      }`}
    >
      {enViaje && <span className="mr-1.5 h-2 w-2 animate-pulse rounded-full bg-bg-fill-primary" aria-hidden="true" />}
      {etiquetasPorEstado[estado] ?? estado}
    </span>
  );
}
