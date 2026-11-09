import type { FiltroEstadoSolicitud } from "../../services/solicitudAdminService";

type Props = {
  seleccionado: FiltroEstadoSolicitud;
  onChange: (estado: FiltroEstadoSolicitud) => void;
};

const filtros: Array<{ value: FiltroEstadoSolicitud; label: string }> = [
  { value: "PENDIENTE", label: "Pendientes" },
  { value: "APROBADO", label: "Aprobados" },
  { value: "RECHAZADO", label: "Rechazados" },
  { value: "TODOS", label: "Todos" },
];

export default function SolicitudesFiltros({ seleccionado, onChange }: Props) {
  return (
    <nav
      aria-label="Filtrar solicitudes por estado"
      className="mb-3 flex max-w-full flex-nowrap gap-0.5 overflow-x-auto rounded-full bg-bg-surface-secondary p-1 lg:inline-flex lg:flex-wrap lg:overflow-visible [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
    >
      {filtros.map(({ value, label }) => {
        const activo = seleccionado === value;
        return (
          <button
            key={value}
            type="button"
            aria-pressed={activo}
            onClick={() => onChange(value)}
            className={`shrink-0 rounded-full px-2 py-1.5 text-xs font-semibold leading-5 transition-colors sm:px-3 sm:text-sm ${
              activo
                ? "bg-white text-text-primary shadow-sm"
                : "text-text-secondary hover:text-text-primary"
            }`}
          >
            {label}
          </button>
        );
      })}
    </nav>
  );
}
