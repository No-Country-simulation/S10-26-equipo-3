import type { SolicitudResumen } from "../../services/solicitudAdminService";
import EstadoBadge from "./EstadoBadge";

type Props = {
  solicitudes: SolicitudResumen[];
  onVerDetalles: (id: number) => void;
  onAprobar: (solicitud: SolicitudResumen) => void;
  onRechazar: (solicitud: SolicitudResumen) => void;
};

function formatearFecha(fecha: string): string {
  const fechaCalendario = /^\d{4}-\d{2}-\d{2}/.test(fecha)
    ? fecha.slice(0, 10)
    : fecha;
  const parsed = /^\d{4}-\d{2}-\d{2}$/.test(fechaCalendario)
    ? new Date(`${fechaCalendario}T00:00:00`)
    : new Date(fechaCalendario);
  return Number.isNaN(parsed.getTime())
    ? fecha
    : new Intl.DateTimeFormat("es-ES", {
        day: "numeric",
        month: "short",
        year: "numeric",
      }).format(parsed);
}

export default function SolicitudesTabla({
  solicitudes,
  onVerDetalles,
  onAprobar,
  onRechazar,
}: Props) {
  const solicitudesOrdenadas = [...solicitudes].sort((a, b) => {
    const fechaA = new Date(a.fechaRegistro).getTime();
    const fechaB = new Date(b.fechaRegistro).getTime();

    if (Number.isNaN(fechaA) && Number.isNaN(fechaB)) return 0;
    if (Number.isNaN(fechaA)) return 1;
    if (Number.isNaN(fechaB)) return -1;

    return fechaA - fechaB;
  });

  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[980px] border-collapse text-left">
        <thead className="text-base font-semibold text-text-secondary">
          <tr>
            <th scope="col" className="whitespace-nowrap border-b border-border-secondary px-4 py-[10px] leading-5">Nro</th>
            <th scope="col" className="whitespace-nowrap border-b border-border-secondary px-4 py-[10px] leading-5">Nombre Completo</th>
            <th scope="col" className="whitespace-nowrap border-b border-border-secondary px-4 py-[10px] leading-5">Fecha de registro</th>
            <th scope="col" className="whitespace-nowrap border-b border-border-secondary px-4 py-[10px] leading-5">Fecha de decisión</th>
            <th scope="col" className="whitespace-nowrap border-b border-border-secondary px-4 py-[10px] text-center leading-5">Estado</th>
            <th scope="col" className="whitespace-nowrap border-b border-border-secondary px-4 py-[10px] leading-5">Acciones</th>
          </tr>
        </thead>
        <tbody>
          {solicitudesOrdenadas.map((solicitud) => (
            <tr key={solicitud.id} className="border-b border-border-secondary text-sm text-text-primary last:border-b-0">
              <td className="whitespace-nowrap px-4 py-[14px]">{solicitud.id}</td>
              <td className="whitespace-nowrap px-4 py-[14px] font-medium">{solicitud.nombreCompleto}</td>
              <td className="whitespace-nowrap px-4 py-[14px] capitalize">
                {formatearFecha(solicitud.fechaRegistro)}
              </td>
              <td className="whitespace-nowrap px-4 py-[14px] text-center text-text-secondary">
                {solicitud.fechaDecision
                  ? formatearFecha(solicitud.fechaDecision)
                  : "—"}
              </td>
              <td className="px-4 py-[14px] text-center">
                {solicitud.estado === "PENDIENTE" ? (
                  <select
                    value={solicitud.estado}
                    onChange={(event) => {
                      if (event.target.value === "APROBADO") onAprobar(solicitud);
                      if (event.target.value === "RECHAZADO") onRechazar(solicitud);
                    }}
                    aria-label={`Cambiar estado de la solicitud ${solicitud.id}`}
                    className="min-w-[136px] cursor-pointer rounded-full border border-border-secondary bg-white px-3 py-1.5 text-xs font-semibold text-text-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary"
                  >
                    <option value="PENDIENTE">Pendiente</option>
                    <option value="APROBADO">Aprobar</option>
                    <option value="RECHAZADO">Rechazar</option>
                  </select>
                ) : (
                  <EstadoBadge estado={solicitud.estado} />
                )}
              </td>
              <td className="px-4 py-[14px]">
                <button
                  type="button"
                  onClick={() => onVerDetalles(solicitud.id)}
                  className="whitespace-nowrap font-semibold text-[#205362] hover:underline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary"
                  aria-label={`Ver detalles de la solicitud ${solicitud.id}`}
                >
                  Ver detalles
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
