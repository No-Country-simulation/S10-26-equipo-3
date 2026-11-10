import { useEffect, useState } from "react";
import { BsArrowRight } from "react-icons/bs";
import { FaShip } from "react-icons/fa";
import {
  listarSeguimientoViajes,
  obtenerAdminDashboard,
  obtenerViajesFinalizadosPorMes,
  type AdminDashboardMetrics,
  type PaginaSeguimientoViajes,
  type ViajesFinalizadosPorMes,
  type ViajeSeguimiento,
} from "../../services/adminDashboardService";
import { listarTramosVacios, type TramoVacio } from "../../services/tramoVacioAdminService";

type Props = { token: string };

function formatDeparture(value: string): { date: string; time: string } {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return { date: "—", time: "" };
  const options = { timeZone: "America/Bogota" };
  return {
    date: new Intl.DateTimeFormat("es-CO", { ...options, day: "numeric", month: "short", year: "numeric" }).format(date),
    time: new Intl.DateTimeFormat("es-CO", { ...options, hour: "2-digit", minute: "2-digit" }).format(date),
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

function formatMonth(value: ViajesFinalizadosPorMes): string {
  return new Intl.DateTimeFormat("es-AR", { month: "short", timeZone: "UTC" })
    .format(new Date(Date.UTC(value.anio, value.mes - 1, 1)))
    .replace(".", "");
}

function completarUltimosDoceMeses(months: ViajesFinalizadosPorMes[]): ViajesFinalizadosPorMes[] {
  const currentParts = new Intl.DateTimeFormat("en-US", {
    timeZone: "America/Argentina/Buenos_Aires",
    year: "numeric",
    month: "numeric",
  }).formatToParts(new Date());
  const year = Number(currentParts.find((part) => part.type === "year")?.value);
  const month = Number(currentParts.find((part) => part.type === "month")?.value);
  const counts = new Map(months.map((item) => [`${item.anio}-${item.mes}`, item.cantidad]));

  return Array.from({ length: 12 }, (_, index) => {
    const date = new Date(Date.UTC(year, month - 1 - 11 + index, 1));
    const anio = date.getUTCFullYear();
    const mes = date.getUTCMonth() + 1;
    return { anio, mes, cantidad: counts.get(`${anio}-${mes}`) ?? 0 };
  });
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
          {props.value === null ? "—" : props.value.toLocaleString("es-CO")}
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

function ReturnTrackingRow({ tramo }: { tramo: TramoVacio }) {
  return (
    <tr className="border-b border-border-secondary last:border-b-0 hover:bg-bg-surface-primary">
      <td className="whitespace-nowrap px-5 py-3">
        <span className="font-semibold text-text-primary">{tramo.lanchaNombre}</span>
        <span className="mt-1 block text-xs text-text-secondary">{tramo.lancheroNombre}</span>
      </td>
      <td className="px-5 py-3">
        <div className="flex min-w-52 items-center gap-2 text-sm">
          <span className="max-w-36 truncate font-medium text-text-primary" title={tramo.zonaOrigen}>{tramo.zonaOrigen}</span>
          <BsArrowRight className="h-4 w-4 shrink-0 text-text-disable" aria-hidden="true" />
          <span className="max-w-36 truncate font-medium text-text-primary" title={tramo.zonaDestino}>{tramo.zonaDestino}</span>
        </div>
      </td>
      <td className="whitespace-nowrap px-5 py-3">
        <span className="block font-medium text-text-primary">{formatDeparture(tramo.fecha + "T12:00:00").date}</span>
        <span className="mt-1 block text-xs text-text-secondary">{tramo.horaRetorno}</span>
      </td>
      <td className="px-5 py-3">
        <span className="inline-flex whitespace-nowrap rounded-full bg-violet-100 px-3 py-1 text-xs font-semibold text-violet-700">
          RETORNO ACTIVO
        </span>
      </td>
      <td className="px-5 py-3">
        {tramo.turistaNombreCompleto ? (
          <>
            <span className="block font-medium text-text-primary">{tramo.turistaNombreCompleto}</span>
            {tramo.turistaWhatsapp && <span className="mt-1 block text-xs text-text-secondary">{tramo.turistaWhatsapp}</span>}
          </>
        ) : (
          <span className="text-sm text-text-secondary">Sin interesado</span>
        )}
      </td>
    </tr>
  );
}

function MobileReturnTrackingCard({ tramo }: { tramo: TramoVacio }) {
  const date = formatDeparture(tramo.fecha + "T12:00:00").date;

  return (
    <article className="rounded-xl border border-border-secondary bg-white p-3">
      <div className="flex items-start justify-between gap-3">
        <div className="flex min-w-0 items-center gap-2.5">
          <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-violet-50 text-violet-700">
            <FaShip className="h-4 w-4" aria-hidden="true" />
          </span>
          <div className="min-w-0">
            <h3 className="truncate text-sm font-semibold text-text-primary">{tramo.lanchaNombre}</h3>
            <p className="truncate text-xs text-text-secondary">{tramo.lancheroNombre}</p>
          </div>
        </div>
        <span className="inline-flex shrink-0 whitespace-nowrap rounded-full bg-violet-100 px-2.5 py-1 text-xs font-semibold text-violet-700">
          RETORNO ACTIVO
        </span>
      </div>
      <div className="mt-3 rounded-lg bg-bg-surface-primary px-3 py-2.5">
        <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-text-secondary">Ruta de retorno</p>
        <div className="flex min-w-0 items-center gap-2 text-sm">
          <span className="min-w-0 flex-1 break-words font-medium text-text-primary">{tramo.zonaOrigen}</span>
          <BsArrowRight className="h-4 w-4 shrink-0 text-text-disable" aria-hidden="true" />
          <span className="min-w-0 flex-1 break-words text-right font-medium text-text-primary">{tramo.zonaDestino}</span>
        </div>
      </div>
      <dl className="mt-3 grid grid-cols-2 gap-3">
        <div>
          <dt className="text-xs text-text-secondary">Fecha y hora</dt>
          <dd className="mt-1 text-sm font-medium text-text-primary">{date} · {tramo.horaRetorno}</dd>
        </div>
        <div>
          <dt className="text-xs text-text-secondary">Turista interesado</dt>
          <dd className="mt-1 break-words text-sm font-medium text-text-primary">
            {tramo.turistaNombreCompleto || "Sin interesado"}
          </dd>
          {tramo.turistaWhatsapp && <dd className="mt-1 break-words text-xs text-text-secondary">{tramo.turistaWhatsapp}</dd>}
        </div>
      </dl>
    </article>
  );
}

export default function DashboardPage({ token }: Props) {
  const [metrics, setMetrics] = useState<AdminDashboardMetrics | null>(null);
  const [metricsError, setMetricsError] = useState<string | null>(null);
  const [tramosVaciosCount, setTramosVaciosCount] = useState<number | null>(null);
  const [tramosVacios, setTramosVacios] = useState<TramoVacio[] | null>(null);
  const [tramosVaciosError, setTramosVaciosError] = useState<string | null>(null);
  const [tripsPage, setTripsPage] = useState<PaginaSeguimientoViajes | null>(null);
  const [tripsError, setTripsError] = useState<string | null>(null);
  const [monthlyTrips, setMonthlyTrips] = useState<ViajesFinalizadosPorMes[] | null>(null);
  const [monthlyTripsError, setMonthlyTripsError] = useState<string | null>(null);
  const [trackingView, setTrackingView] = useState<"viajes" | "retornos">("viajes");
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
    void listarTramosVacios(token, { estado: "ACTIVO" }, controller.signal).then((tramos) => {
      setTramosVacios(tramos);
      setTramosVaciosCount(tramos.length);
      setTramosVaciosError(null);
    }).catch((error: unknown) => {
      if (!controller.signal.aborted) {
        setTramosVacios(null);
        setTramosVaciosError(error instanceof Error ? error.message : "No se pudieron cargar los tramos vacíos.");
      }
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

  useEffect(() => {
    const controller = new AbortController();
    void obtenerViajesFinalizadosPorMes(token, controller.signal).then((result) => {
      setMonthlyTrips(result);
      setMonthlyTripsError(null);
    }).catch((error: unknown) => {
      if (!controller.signal.aborted) {
        setMonthlyTripsError(error instanceof Error ? error.message : "No se pudieron cargar los viajes por mes.");
      }
    });
    return () => controller.abort();
  }, [token, reload]);

  const retry = () => {
    setMetricsError(null);
    setTramosVaciosError(null);
    setTramosVaciosCount(null);
    setTripsError(null);
    setMonthlyTripsError(null);
    setReload((value) => value + 1);
  };
  const summaryError = metricsError ?? tramosVaciosError;
  const chartMonths = monthlyTrips ? completarUltimosDoceMeses(monthlyTrips).slice(-6) : null;
  const highestMonthlyTrips = chartMonths
    ? Math.max(0, ...chartMonths.map((item) => Number.isFinite(item.cantidad) ? Math.max(item.cantidad, 0) : 0))
    : 0;

  return (
    <div className="mx-auto w-full max-w-7xl space-y-5 pt-2 sm:space-y-6 sm:pt-3">
      <section aria-labelledby="dashboard-summary-title" className="space-y-4">
        <h2 id="dashboard-summary-title" className="text-xl font-bold leading-7 text-text-primary">Resumen general</h2>
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4 xl:grid-cols-4">
          <MetricCard title="Lanchas verificadas" value={metrics?.lanchasVerificadas ?? null} icon="/icons/barco.svg" accent="bg-emerald-50 text-emerald-700" />
          <MetricCard title="Solicitudes pendientes" value={metrics?.solicitudesPendientes ?? null} icon="/icons/documentos.svg" accent="bg-amber-50 text-amber-700" />
          <MetricCard title="Viajes activos" value={metrics?.viajesActivos ?? null} icon="/icons/route.svg" accent="bg-sky-50 text-sky-700" />
          <MetricCard title="Tramos vacíos" value={tramosVaciosCount} icon="/icons/ubicacion.svg" accent="bg-violet-50 text-violet-700" />
        </div>
        {summaryError && (
          <div className="flex flex-wrap items-center justify-between gap-2 text-sm text-text-error">
            <p role="alert">{summaryError}</p>
            <button type="button" onClick={retry} className="font-semibold underline">Reintentar</button>
          </div>
        )}
      </section>

      <section aria-labelledby="tracking-section-title" className="space-y-2.5">
        <div>
          <h2 id="tracking-section-title" className="text-xl font-bold leading-7 text-text-primary sm:text-2xl sm:leading-8">
            Control y seguimiento de viajes
          </h2>
        </div>
        <article className="overflow-hidden rounded-xl border border-border-secondary bg-white">
          <header className="flex flex-col gap-3 border-b border-border-secondary px-4 py-3 sm:flex-row sm:items-center sm:justify-between sm:px-6">
            <div className="flex min-w-0 items-center gap-3">
              <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-bg-fill-primary/10 text-bg-fill-primary">
                <FaShip className="h-5 w-5" aria-hidden="true" />
              </span>
              <div className="min-w-0 pt-0.5">
                <h3 className="text-base font-semibold leading-6 text-text-primary">Control y seguimiento</h3>
                <p className="mt-1 text-sm leading-5 text-text-secondary">
                  {trackingView === "viajes" ? "Seguimiento de viajes y su estado." : "Retornos vacíos publicados y disponibles."}
                </p>
              </div>
            </div>
            <div role="tablist" aria-label="Tipo de seguimiento" className="flex w-full rounded-full bg-bg-surface-secondary p-1 sm:w-auto">
              <button
                type="button"
                role="tab"
                id="dashboard-tab-viajes"
                aria-selected={trackingView === "viajes"}
                aria-controls="dashboard-panel-viajes"
                onClick={() => setTrackingView("viajes")}
                className={`flex min-h-9 flex-1 items-center justify-center gap-2 rounded-full px-3 text-sm font-semibold transition-colors sm:flex-none ${trackingView === "viajes" ? "bg-white text-text-primary shadow-sm" : "text-text-secondary hover:text-text-primary"}`}
              >
                Viajes
                <span className="rounded-full bg-bg-surface-primary px-2 py-0.5 text-xs tabular-nums">
                  {tripsPage?.totalElements ?? "—"}
                </span>
              </button>
              <button
                type="button"
                role="tab"
                id="dashboard-tab-retornos"
                aria-selected={trackingView === "retornos"}
                aria-controls="dashboard-panel-retornos"
                onClick={() => setTrackingView("retornos")}
                className={`flex min-h-9 flex-1 items-center justify-center gap-2 rounded-full px-3 text-sm font-semibold transition-colors sm:flex-none ${trackingView === "retornos" ? "bg-white text-text-primary shadow-sm" : "text-text-secondary hover:text-text-primary"}`}
              >
                Retornos
                <span className="rounded-full bg-bg-surface-primary px-2 py-0.5 text-xs tabular-nums">
                  {tramosVacios?.length ?? "—"}
                </span>
              </button>
            </div>
          </header>

          <div
            role="tabpanel"
            id={trackingView === "viajes" ? "dashboard-panel-viajes" : "dashboard-panel-retornos"}
            aria-labelledby={trackingView === "viajes" ? "dashboard-tab-viajes" : "dashboard-tab-retornos"}
          >
            <div className="space-y-2.5 p-3 md:hidden">
              {trackingView === "viajes" && (
                tripsError ? (
                  <p role="alert" className="py-4 text-center text-sm text-text-error">No se pudo cargar el seguimiento de viajes.</p>
                ) : !tripsPage ? (
                  <p role="status" className="py-4 text-center text-sm text-text-secondary">Cargando seguimiento...</p>
                ) : tripsPage.content.length === 0 ? (
                  <p className="py-4 text-center text-sm text-text-secondary">No hay viajes para seguimiento.</p>
                ) : tripsPage.content.map((trip) => <MobileTrackingCard key={trip.id} trip={trip} />)
              )}
              {trackingView === "retornos" && (
                tramosVaciosError ? (
                  <p role="alert" className="py-4 text-center text-sm text-text-error">No se pudieron cargar los retornos.</p>
                ) : !tramosVacios ? (
                  <p role="status" className="py-4 text-center text-sm text-text-secondary">Cargando retornos...</p>
                ) : tramosVacios.length === 0 ? (
                  <p className="py-4 text-center text-sm text-text-secondary">No hay retornos activos para seguimiento.</p>
                ) : tramosVacios.map((tramo) => <MobileReturnTrackingCard key={tramo.id} tramo={tramo} />)
              )}
            </div>
            <div className="hidden overflow-x-auto md:block">
              <table className="w-full min-w-[760px] border-collapse text-left text-sm">
                <thead className="bg-bg-surface-primary">
                  <tr className="border-b border-border-secondary">
                    <th scope="col" className="whitespace-nowrap px-5 py-2.5 text-xs font-semibold uppercase tracking-wide text-text-secondary">Lancha</th>
                    <th scope="col" className="whitespace-nowrap px-5 py-2.5 text-xs font-semibold uppercase tracking-wide text-text-secondary">Ruta</th>
                    <th scope="col" className="whitespace-nowrap px-5 py-2.5 text-xs font-semibold uppercase tracking-wide text-text-secondary">Fecha y hora</th>
                    <th scope="col" className="whitespace-nowrap px-5 py-2.5 text-xs font-semibold uppercase tracking-wide text-text-secondary">Estado</th>
                    <th scope="col" className="whitespace-nowrap px-5 py-2.5 text-xs font-semibold uppercase tracking-wide text-text-secondary">
                      {trackingView === "viajes" ? "Última ubicación" : "Turista interesado"}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {trackingView === "viajes" && (
                    tripsError ? (
                      <tr><td colSpan={5} role="alert" className="px-5 py-10 text-center text-sm text-text-error">No se pudo cargar el seguimiento de viajes.</td></tr>
                    ) : !tripsPage ? (
                      <tr><td colSpan={5} role="status" className="px-5 py-10 text-center text-sm text-text-secondary">Cargando seguimiento...</td></tr>
                    ) : tripsPage.content.length === 0 ? (
                      <tr><td colSpan={5} className="px-5 py-10 text-center text-sm text-text-secondary">No hay viajes para seguimiento.</td></tr>
                    ) : tripsPage.content.map((trip) => <TrackingRow key={trip.id} trip={trip} />)
                  )}
                  {trackingView === "retornos" && (
                    tramosVaciosError ? (
                      <tr><td colSpan={5} role="alert" className="px-5 py-10 text-center text-sm text-text-error">No se pudieron cargar los retornos.</td></tr>
                    ) : !tramosVacios ? (
                      <tr><td colSpan={5} role="status" className="px-5 py-10 text-center text-sm text-text-secondary">Cargando retornos...</td></tr>
                    ) : tramosVacios.length === 0 ? (
                      <tr><td colSpan={5} className="px-5 py-10 text-center text-sm text-text-secondary">No hay retornos activos para seguimiento.</td></tr>
                    ) : tramosVacios.map((tramo) => <ReturnTrackingRow key={tramo.id} tramo={tramo} />)
                  )}
                </tbody>
              </table>
            </div>
          </div>
          {trackingView === "viajes" && tripsPage && tripsPage.totalPages > 1 && (
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
        {trackingView === "viajes" && tripsError && <button type="button" onClick={retry} className="text-sm font-semibold text-bg-fill-primary underline">Reintentar carga de viajes</button>}
      </section>

      <section aria-labelledby="monthly-trips-title" className="space-y-2.5">
        <article className="rounded-xl border border-border-secondary bg-white p-3 sm:p-5">
          <header className="mb-3 flex items-center justify-between gap-3">
            <h2 id="monthly-trips-title" className="text-base font-semibold leading-6 text-text-primary">
              Actividad de viaje
            </h2>
            <p className="shrink-0 text-right text-xs text-text-secondary sm:text-sm">Últimos 6 meses</p>
          </header>
          {monthlyTripsError ? (
            <div className="flex flex-wrap items-center justify-between gap-3">
              <p role="alert" className="text-sm text-text-error">{monthlyTripsError}</p>
              <button type="button" onClick={retry} className="text-sm font-semibold text-bg-fill-primary underline">Reintentar</button>
            </div>
          ) : !monthlyTrips ? (
            <p role="status" className="py-10 text-center text-sm text-text-secondary">Cargando actividad mensual...</p>
          ) : (
            <div className="mx-auto w-full max-w-4xl px-1 pb-1">
              <div
                role="img"
                aria-label={"Viajes finalizados por mes durante los últimos 6 meses. Total: " +
                  (chartMonths ?? []).reduce((total, item) => total + (Number.isFinite(item.cantidad) ? Math.max(item.cantidad, 0) : 0), 0).toLocaleString("es-AR")}
                className="grid grid-cols-6 gap-4 sm:gap-8"
              >
                {chartMonths?.map((item, index) => {
                  const value = Number.isFinite(item.cantidad) ? Math.max(item.cantidad, 0) : 0;
                  const barHeight = highestMonthlyTrips > 0 ? value / highestMonthlyTrips * 76 : 0;
                  const labelOffset = barHeight > 0 ? `calc(${barHeight}% + 0.25rem)` : "0.25rem";
                  const startsYear = index === 0 || chartMonths[index - 1].anio !== item.anio;
                  return (
                    <div key={item.anio + "-" + item.mes} className="min-w-0">
                      <div className="relative h-32 sm:h-36">
                        <span
                          className="absolute left-1/2 -translate-x-1/2 whitespace-nowrap text-xs font-semibold tabular-nums text-text-primary"
                          style={{ bottom: labelOffset }}
                        >
                          {value}
                        </span>
                        {barHeight > 0 && (
                          <div
                            className="absolute inset-x-2 bottom-0 rounded-t-md bg-bg-fill-primary transition-[height] sm:inset-x-3"
                            style={{ height: barHeight + "%" }}
                            aria-hidden="true"
                          />
                        )}
                      </div>
                      <div className="mt-2 flex min-h-8 flex-col items-center justify-start">
                        <span className="w-full truncate text-center text-xs font-medium capitalize text-text-secondary" title={`${formatMonth(item)} ${item.anio}`}>
                          {formatMonth(item)}
                        </span>
                        {startsYear && <span className="text-xs text-text-disable">{item.anio}</span>}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </article>
      </section>
    </div>
  );
}
