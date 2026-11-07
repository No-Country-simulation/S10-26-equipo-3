type Props = {
  estado: string;
};

const estilosPorEstado: Record<string, string> = {
  PENDIENTE: "bg-[#fb9c2d] text-white",
  APROBADO: "bg-[#205362] text-white",
  RECHAZADO: "bg-[#EA243E] text-white",
};

const etiquetasPorEstado: Record<string, string> = {
  PENDIENTE: "Pendiente",
  APROBADO: "Aprobado",
  RECHAZADO: "Rechazado",
};

export default function EstadoBadge({ estado }: Props) {
  return (
    <span
      className={`inline-flex min-h-[30px] min-w-[136px] items-center justify-center rounded-full border border-border-secondary px-3 py-1.5 text-xs font-semibold leading-4 ${
        estilosPorEstado[estado] ?? "bg-bg-surface-secondary text-text-secondary"
      }`}
    >
      {etiquetasPorEstado[estado] ?? estado}
    </span>
  );
}
