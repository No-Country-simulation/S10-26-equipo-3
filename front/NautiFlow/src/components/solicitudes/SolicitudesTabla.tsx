import type { SolicitudResumen } from "../../services/solicitudAdminService";
import EstadoBadge from "./EstadoBadge";

type Props = {
  solicitudes: SolicitudResumen[];
  onVerDetalles: (id: number) => void;
  onAprobar: (solicitud: SolicitudResumen) => void;
  onRechazar: (solicitud: SolicitudResumen) => void;
};

type EstadoSolicitudControlProps = {
  solicitud: SolicitudResumen;
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

function EstadoSolicitudControl({
  solicitud,
  onAprobar,
  onRechazar,
}: EstadoSolicitudControlProps) {
  if (solicitud.estado !== "PENDIENTE") {
    return <EstadoBadge estado={solicitud.estado} />;
  }

  return (
    <select
      value={solicitud.estado}
      onChange={(event) => {
        if (event.target.value === "APROBADO") onAprobar(solicitud);
        if (event.target.value === "RECHAZADO") onRechazar(solicitud);
      }}
      aria-label={`Cambiar estado de la solicitud ${solicitud.id}`}
      className="w-full cursor-pointer rounded-full border border-border-secondary bg-white px-2 py-1.5 text-xs font-semibold text-text-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary lg:max-w-[132px]"
    >
      <option value="PENDIENTE">Pendiente</option>
      <option value="APROBADO">Aprobar</option>
      <option value="RECHAZADO">Rechazar</option>
    </select>
  );
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
    <>
      <ul className="grid gap-3 lg:hidden" aria-label="Solicitudes">
        {solicitudesOrdenadas.map((solicitud) => (
          <li key={solicitud.id}>
            <article className="rounded-[11px] border border-border-secondary bg-white p-3 sm:p-4">
              <div className="flex min-h-8 items-center justify-between gap-3">
                <p className="text-sm font-bold text-text-primary">Solicitud Nro. {solicitud.id}</p>
                <div className="w-auto min-w-0 shrink-0">
                  <EstadoSolicitudControl
                    solicitud={solicitud}
                    onAprobar={onAprobar}
                    onRechazar={onRechazar}
                  />
                </div>
              </div>

              <div className="mt-2.5 border-t border-border-secondary pt-2.5">
                <p className="break-words text-[15px] font-semibold leading-5 text-text-primary">
                  {solicitud.nombreCompleto}
                </p>
              </div>

              <dl className="mt-2.5 grid grid-cols-2 gap-2">
                <div>
                  <dt className="text-xs font-medium text-text-secondary">Fecha de registro</dt>
                  <dd className="mt-0.5 text-sm text-text-primary">
                    {formatearFecha(solicitud.fechaRegistro)}
                  </dd>
                </div>
                <div>
                  <dt className="text-xs font-medium text-text-secondary">Fecha de decisión</dt>
                  <dd className="mt-0.5 text-sm text-text-primary">
                    {solicitud.fechaDecision
                      ? formatearFecha(solicitud.fechaDecision)
                      : "—"}
                  </dd>
                </div>
              </dl>

              <button
                type="button"
                onClick={() => onVerDetalles(solicitud.id)}
                className="mt-3 h-11 w-full rounded-lg border border-border-secondary bg-bg-surface-primary px-4 text-sm font-medium text-text-primary transition-colors hover:bg-bg-surface-secondary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary"
                aria-label={`Ver detalles de la solicitud ${solicitud.id}`}
              >
                Ver detalles
              </button>
            </article>
          </li>
        ))}
      </ul>

      <div className="hidden overflow-x-auto lg:block">
        <table className="w-full min-w-[960px] table-fixed border-collapse text-left">
          <colgroup>
            <col className="w-[9%]" />
            <col className="w-[23%]" />
            <col className="w-[20%]" />
            <col className="w-[20%]" />
            <col className="w-[14%]" />
            <col className="w-[14%]" />
          </colgroup>
          <thead className="text-sm font-semibold text-text-secondary">
            <tr>
              <th scope="col" className="whitespace-nowrap border-b border-border-secondary px-3 py-2 leading-5">Nro</th>
              <th scope="col" className="whitespace-nowrap border-b border-border-secondary px-3 py-2 leading-5">Nombre Completo</th>
              <th scope="col" className="whitespace-nowrap border-b border-border-secondary px-3 py-2 leading-5">Fecha de registro</th>
              <th scope="col" className="whitespace-nowrap border-b border-border-secondary px-3 py-2 leading-5">Fecha de decisión</th>
              <th scope="col" className="whitespace-nowrap border-b border-border-secondary px-2 py-2 text-center leading-5">Estado</th>
              <th scope="col" className="whitespace-nowrap border-b border-border-secondary px-2 py-2 leading-5">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {solicitudesOrdenadas.map((solicitud) => (
              <tr key={solicitud.id} className="border-b border-border-secondary text-sm text-text-primary last:border-b-0 2xl:text-[15px]">
                <td className="whitespace-nowrap px-3 py-2.5">{solicitud.id}</td>
                <td className="overflow-hidden text-ellipsis whitespace-nowrap px-3 py-2.5 font-medium">{solicitud.nombreCompleto}</td>
                <td className="whitespace-nowrap px-3 py-2.5 capitalize">
                  {formatearFecha(solicitud.fechaRegistro)}
                </td>
                <td className="whitespace-nowrap px-3 py-2.5 text-center text-text-secondary">
                  {solicitud.fechaDecision
                    ? formatearFecha(solicitud.fechaDecision)
                    : "—"}
                </td>
                <td className="px-2 py-2.5 text-center">
                  <EstadoSolicitudControl
                    solicitud={solicitud}
                    onAprobar={onAprobar}
                    onRechazar={onRechazar}
                  />
                </td>
                <td className="px-2 py-2.5">
                  <button
                    type="button"
                    onClick={() => onVerDetalles(solicitud.id)}
                    className="whitespace-nowrap text-sm font-semibold text-[#205362] hover:underline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary"
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
    </>
  );
}
