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
      className="mb-3 inline-flex max-w-full flex-wrap gap-1 rounded-full bg-bg-surface-secondary p-1"
    >
      {filtros.map(({ value, label }) => {
        const activo = seleccionado === value;
        return (
          <button
            key={value}
            type="button"
            aria-pressed={activo}
            onClick={() => onChange(value)}
            className={`rounded-full px-4 py-2 text-sm font-semibold transition-colors ${
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
