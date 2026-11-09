import { useEffect, useState } from "react";
import { BsArrowRight } from "react-icons/bs";
import { FaShip } from "react-icons/fa";
import {
  listarSeguimientoViajes,
  obtenerAdminDashboard,
  type AdminDashboardMetrics,
  type PaginaSeguimientoViajes,
  type ViajeSeguimiento,
} from "../../services/adminDashboardService";
import ActividadViajesChart from "../../components/Admin/ActividadViajesChart";
import { ZONA_HORARIA } from "../../config/api";

type Props = { token: string };

function formatDeparture(value: string): { date: string; time: string } {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return { date: "—", time: "" };
  const options = { timeZone: ZONA_HORARIA };
  return {
    date: new Intl.DateTimeFormat("es-AR", { ...options, day: "numeric", month: "short", year: "numeric" }).format(date),
    time: new Intl.DateTimeFormat("es-AR", { ...options, hour: "2-digit", minute: "2-digit" }).format(date),
  };
}

function statusLabel(status: string): string {
  return status === "EN_VIAJE" ? "EN CURSO" : status.replaceAll("_", " ");
}

function statusClass(status: string): string {
  if (status === "EN_VIAJE") return "bg-orange-100 text-orange-700";
  if (status === "FINALIZADO") return "bg-green-100 text-green-700";
  return "bg-bg-surface-secondary text-text-secondary";
}

function MetricCard(props: { title: string; value: number | null; icon: string; accent: string }) {
  return (
    <article className="flex h-full min-h-36 flex-col gap-4 rounded-xl border border-border-secondary bg-white p-3 transition-colors sm:p-4">
      <div className="flex min-w-0 items-center gap-2">
        <span className={"flex h-9 w-9 shrink-0 items-center justify-center rounded-xl " + props.accent}>
          <img src={props.icon} alt="" className="h-5 w-5" aria-hidden="true" />
        </span>
        <p className="min-w-0 text-sm font-semibold leading-5 text-text-secondary">{props.title}</p>
      </div>
      <div className="mt-3 flex min-w-0 items-end justify-between gap-2">
        <p className="text-3xl font-bold leading-none tracking-tight text-text-primary sm:text-4xl">
          {props.value === null ? "—" : props.value.toLocaleString("es-AR")}
        </p>
      </div>
    </article>
  );
}

function TrackingRow({ trip }: { trip: ViajeSeguimiento }) {
  const departure = formatDeparture(trip.fechaHoraInicio);
  const lastLocation = trip.estado === "EN_VIAJE"
    ? trip.zonaOrigen
    : trip.ultimaUbicacionConfirmada || "Sin registro";

  return (
    <tr className="border-b border-border-secondary last:border-b-0 hover:bg-bg-surface-primary">
      <td className="whitespace-nowrap px-5 py-3">
        <span className="font-semibold text-text-primary">{trip.nombreEmbarcacion}</span>
      </td>
      <td className="px-5 py-3">
        <div className="flex min-w-52 items-center gap-2 text-sm">
          <span className="max-w-36 truncate font-medium text-text-primary" title={trip.zonaOrigen}>{trip.zonaOrigen}</span>
          <BsArrowRight className="h-4 w-4 shrink-0 text-text-disable" aria-hidden="true" />
          <span className="max-w-36 truncate font-medium text-text-primary" title={trip.zonaDestino}>{trip.zonaDestino}</span>
        </div>
      </td>
      <td className="whitespace-nowrap px-5 py-3">
        <span className="block font-medium text-text-primary">{departure.date}</span>
        {departure.time && <span className="mt-1 block text-xs text-text-secondary">{departure.time}</span>}
      </td>
      <td className="px-5 py-3">
        <span className={"inline-flex whitespace-nowrap rounded-full px-3 py-1 text-xs font-semibold " + statusClass(trip.estado)}>
          {statusLabel(trip.estado)}
        </span>
      </td>
      <td className="whitespace-nowrap px-5 py-3 text-sm text-text-secondary">{lastLocation}</td>
    </tr>
  );
}

function MobileTrackingCard({ trip }: { trip: ViajeSeguimiento }) {
  const departure = formatDeparture(trip.fechaHoraInicio);
  const lastLocation = trip.estado === "EN_VIAJE"
    ? trip.zonaOrigen
    : trip.ultimaUbicacionConfirmada || "Sin registro";

  return (
    <article className="rounded-xl border border-border-secondary bg-white p-3">
      <div className="flex items-start justify-between gap-3">
        <div className="flex min-w-0 items-center gap-2.5">
          <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-bg-fill-primary/10 text-bg-fill-primary">
            <FaShip className="h-4 w-4" aria-hidden="true" />
          </span>
          <h3 className="truncate text-sm font-semibold text-text-primary">{trip.nombreEmbarcacion}</h3>
        </div>
        <span className={"inline-flex shrink-0 whitespace-nowrap rounded-full px-2.5 py-1 text-xs font-semibold " + statusClass(trip.estado)}>
          {statusLabel(trip.estado)}
        </span>
      </div>

      <div className="mt-3 rounded-lg bg-bg-surface-primary px-3 py-2.5">
        <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-text-secondary">Ruta</p>
        <div className="flex min-w-0 items-center gap-2 text-sm">
          <span className="min-w-0 flex-1 break-words font-medium text-text-primary">{trip.zonaOrigen}</span>
          <BsArrowRight className="h-4 w-4 shrink-0 text-text-disable" aria-hidden="true" />
          <span className="min-w-0 flex-1 break-words text-right font-medium text-text-primary">{trip.zonaDestino}</span>
        </div>
      </div>

      <dl className="mt-3 grid grid-cols-2 gap-x-3 gap-y-3">
        <div className="min-w-0">
          <dt className="text-xs text-text-secondary">Salida</dt>
          <dd className="mt-1 text-sm font-medium text-text-primary">
            {departure.date}
            {departure.time && <span className="block text-xs font-normal text-text-secondary">{departure.time}</span>}
          </dd>
        </div>
        <div className="min-w-0">
          <dt className="text-xs text-text-secondary">Última ubicación</dt>
          <dd className="mt-1 break-words text-sm font-medium text-text-primary">{lastLocation}</dd>
        </div>
      </dl>
    </article>
  );
}
export default function DashboardPage({ token }: Props) {
  const [metrics, setMetrics] = useState<AdminDashboardMetrics | null>(null);
  const [metricsError, setMetricsError] = useState<string | null>(null);
  const [tripsPage, setTripsPage] = useState<PaginaSeguimientoViajes | null>(null);
  const [tripsError, setTripsError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [reload, setReload] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setMetricsError(null);
    void obtenerAdminDashboard(token, controller.signal).then(setMetrics).catch((error: unknown) => {
      if (!controller.signal.aborted) setMetricsError(error instanceof Error ? error.message : "No se pudieron cargar las métricas.");
    });
    return () => controller.abort();
  }, [token, reload]);

  useEffect(() => {
    const controller = new AbortController();
    setTripsError(null);
    void listarSeguimientoViajes(token, page, controller.signal).then(setTripsPage).catch((error: unknown) => {
      if (!controller.signal.aborted) setTripsError(error instanceof Error ? error.message : "No se pudieron cargar los viajes.");
    });
    return () => controller.abort();
  }, [token, page, reload]);

  const retry = () => { setMetricsError(null); setTripsError(null); setReload((value) => value + 1); };

  return (
    <div className="space-y-5 pt-2 sm:space-y-6 sm:pt-3 -mx-1 sm:-mx-2 lg:-mx-3">
      <section aria-labelledby="dashboard-summary-title" className="space-y-4">
        <h2 id="dashboard-summary-title" className="text-xl font-bold leading-7 text-text-primary">Resumen general</h2>
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4 xl:grid-cols-4">
          <MetricCard title="Lanchas verificadas" value={metrics?.lanchasVerificadas ?? null} icon="/icons/barco.svg" accent="bg-emerald-50 text-emerald-700" />
          <MetricCard title="Solicitudes pendientes" value={metrics?.solicitudesPendientes ?? null} icon="/icons/documentos.svg" accent="bg-amber-50 text-amber-700" />
          <MetricCard title="Viajes activos" value={metrics?.viajesActivos ?? null} icon="/icons/route.svg" accent="bg-sky-50 text-sky-700" />
          <MetricCard title="Tramos vacíos" value={metrics?.tramosVacios ?? null} icon="/icons/ubicacion.svg" accent="bg-violet-50 text-violet-700" />
        </div>
        {metricsError && (
          <div className="flex flex-wrap items-center justify-between gap-2 text-sm text-text-error">
            <p role="alert">{metricsError}</p>
            <button type="button" onClick={retry} className="font-semibold underline">Reintentar</button>
          </div>
        )}
      </section>

      <ActividadViajesChart actividad={metricsError ? [] : metrics?.actividadMensual ?? null} />

      <section aria-labelledby="tracking-section-title" className="space-y-2.5">
        <div>
          <h2 id="tracking-section-title" className="text-xl font-bold leading-7 text-text-primary sm:text-2xl sm:leading-8">
            Control y seguimiento de viajes
          </h2>
        </div>
        <article className="overflow-hidden rounded-xl border border-border-secondary bg-white">
          <header className="flex items-start gap-3 border-b border-border-secondary px-4 py-3 sm:px-6">
            <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-bg-fill-primary/10 text-bg-fill-primary">
              <FaShip className="h-5 w-5" aria-hidden="true" />
            </span>
            <div className="min-w-0 pt-0.5">
              <h3 className="text-base font-semibold leading-6 text-text-primary">Control de viajes</h3>
              <p className="mt-1 text-sm leading-5 text-text-secondary">Seguimiento de salidas y disponibilidad de lanchas.</p>
            </div>
          </header>

          {tripsError && <p role="alert" className="mx-4 mt-4 text-sm text-text-error sm:mx-6">{tripsError}</p>}
          <div className="space-y-2.5 p-3 md:hidden">
            {tripsError ? (
              <p className="py-4 text-center text-sm text-text-secondary">No se pudo cargar el seguimiento.</p>
            ) : !tripsPage ? (
              <p role="status" className="py-4 text-center text-sm text-text-secondary">Cargando seguimiento...</p>
            ) : tripsPage.content.length === 0 ? (
              <p className="py-4 text-center text-sm text-text-secondary">No hay viajes de seguimiento disponibles.</p>
            ) : tripsPage.content.map((trip) => <MobileTrackingCard key={trip.id} trip={trip} />)}
          </div>
          <div className="hidden overflow-x-auto md:block">
            <table className="w-full min-w-[760px] border-collapse text-left text-sm">
              <thead className="bg-bg-surface-primary">
                <tr className="border-b border-border-secondary">
                  <th scope="col" className="whitespace-nowrap px-5 py-2.5 text-xs font-semibold uppercase tracking-wide text-text-secondary">Lancha</th>
                  <th scope="col" className="whitespace-nowrap px-5 py-2.5 text-xs font-semibold uppercase tracking-wide text-text-secondary">Ruta</th>
                  <th scope="col" className="whitespace-nowrap px-5 py-2.5 text-xs font-semibold uppercase tracking-wide text-text-secondary">Fecha y hora de salida</th>
                  <th scope="col" className="whitespace-nowrap px-5 py-2.5 text-xs font-semibold uppercase tracking-wide text-text-secondary">Estado</th>
                  <th scope="col" className="whitespace-nowrap px-5 py-2.5 text-xs font-semibold uppercase tracking-wide text-text-secondary">Última ubicación</th>
                </tr>
              </thead>
              <tbody>
                {tripsError ? null
                  : !tripsPage ? <tr><td colSpan={5} role="status" className="px-5 py-10 text-center text-sm text-text-secondary">Cargando seguimiento...</td></tr>
                  : tripsPage.content.length === 0 ? <tr><td colSpan={5} className="px-5 py-10 text-center text-sm text-text-secondary">No hay viajes de seguimiento disponibles.</td></tr>
                  : tripsPage.content.map((trip) => <TrackingRow key={trip.id} trip={trip} />)}
              </tbody>
            </table>
          </div>
          {tripsPage && tripsPage.totalPages > 1 && (
            <div className="flex flex-wrap items-center justify-between gap-3 border-t border-border-secondary px-4 py-2.5 text-sm text-text-secondary sm:px-6">
              <span>Página {tripsPage.page + 1} de {tripsPage.totalPages}</span>
              <div className="flex gap-2">
                <button type="button" disabled={page === 0} onClick={() => setPage((value) => Math.max(0, value - 1))} className="min-h-9 rounded-lg border border-border-secondary px-3 font-semibold text-text-primary transition-colors hover:bg-bg-surface-secondary disabled:opacity-40">
                  Anterior
                </button>
                <button type="button" disabled={page + 1 >= tripsPage.totalPages} onClick={() => setPage((value) => value + 1)} className="min-h-9 rounded-lg border border-border-secondary px-3 font-semibold text-text-primary transition-colors hover:bg-bg-surface-secondary disabled:opacity-40">
                  Siguiente
                </button>
              </div>
            </div>
          )}
        </article>
        {tripsError && <button type="button" onClick={retry} className="text-sm font-semibold text-bg-fill-primary underline">Reintentar carga de viajes</button>}
      </section>
    </div>
  );
}
