
import { useEffect, useState } from "react";
import { BsArrowLeftRight, BsArrowRight, BsCheckLg } from "react-icons/bs";
import { FaShip } from "react-icons/fa";
import ModalDecision from "../../components/Admin/ModalDecision";
import Notification, { type NotificationKind } from "../../components/Admin/Notification";
import SolicitudesPaginacion from "../../components/solicitudes/SolicitudesPaginacion";
import EstadoBadge from "../../components/solicitudes/EstadoBadge";
import { listarSeguimientoViajes, type ViajeSeguimiento } from "../../services/adminDashboardService";
import { finalizarViaje, type ViajeFinalizado } from "../../services/viajeAdminService";
import ModalSolicitarResena from "../../components/Admin/ModalSolicitarResena";
import PublicarTramoVacioModal from "../../components/Admin/PublicarTramoVacioModal";
import TramosVaciosPanel from "./TramosVaciosPanel";
import type { TramoVacio } from "../../services/tramoVacioAdminService";

type Props = { token: string };

function fechaYHora(value: string): { date: string; time: string } {
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return { date: value, time: "" };
  const options = { timeZone: "America/Bogota" };
  return {
    date: new Intl.DateTimeFormat("es-CO", { ...options, day: "numeric", month: "short", year: "numeric" }).format(parsed),
    time: new Intl.DateTimeFormat("es-CO", { ...options, hour: "2-digit", minute: "2-digit" }).format(parsed),
  };
}

function Ruta({ trip }: { trip: ViajeSeguimiento }) {
  return <div className="flex min-w-0 items-center gap-1.5 text-sm">
    <span className="min-w-0 flex-1 break-words font-medium leading-5 text-text-primary">{trip.zonaOrigen}</span>
    <BsArrowRight className="h-4 w-4 shrink-0 text-text-disable" aria-hidden="true" />
    <span className="min-w-0 flex-1 break-words text-right font-medium leading-5 text-text-primary">{trip.zonaDestino}</span>
  </div>;
}

function BotonCompletar({ onClick, finishing }: { onClick: () => void; finishing: boolean }) {
  return <button
    type="button"
    onClick={onClick}
    disabled={finishing}
    aria-busy={finishing}
    className="inline-flex h-9 w-full items-center justify-center gap-1.5 whitespace-nowrap rounded-lg bg-bg-fill-primary px-3 text-xs font-semibold text-text-invert transition-colors hover:bg-bg-fill-primary-hover focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary disabled:cursor-not-allowed disabled:opacity-60"
  >
    {finishing ? <span className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-text-invert/40 border-t-text-invert" aria-hidden="true" /> : <BsCheckLg className="h-3.5 w-3.5" aria-hidden="true" />}
    {finishing ? "Finalizando…" : "Completar viaje"}
  </button>;
}

function BotonPublicarRetorno({ onClick }: { onClick: () => void }) {
  return <button
    type="button"
    onClick={onClick}
    className="inline-flex h-9 w-full items-center justify-center gap-1.5 whitespace-nowrap rounded-lg border border-border-secondary bg-bg-surface-primary px-3 text-xs font-semibold text-text-primary transition-colors hover:bg-bg-surface-secondary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary"
  >
    <BsArrowLeftRight className="h-3.5 w-3.5" aria-hidden="true" />
    Publicar retorno
  </button>;
}

function ViajeCard({ trip, index, onFinish, onPublish, finishing }: { trip: ViajeSeguimiento; index: number; onFinish: () => void; onPublish: () => void; finishing: boolean }) {
  const departure = fechaYHora(trip.fechaHoraInicio);
  return <article className="rounded-[11px] border border-border-secondary bg-white p-4">
    <div className="flex items-start justify-between gap-3">
      <div className="flex min-w-0 items-center gap-2.5">
        <span className="grid h-9 w-9 shrink-0 place-items-center rounded-lg bg-bg-surface-primary text-bg-fill-primary"><FaShip aria-hidden="true" /></span>
        <div className="min-w-0"><p className="text-xs text-text-secondary">Viaje Nro. {index + 1}</p><h2 className="truncate text-sm font-semibold text-text-primary">{trip.nombreEmbarcacion}</h2></div>
      </div>
      <EstadoBadge estado={trip.estado} />
    </div>
    <div className="mt-3 rounded-lg bg-bg-surface-primary p-3"><p className="mb-2 text-xs font-semibold uppercase tracking-wide text-text-secondary">Ruta</p><Ruta trip={trip} /></div>
    <dl className="mt-3 grid grid-cols-2 gap-3 text-sm">
      <div><dt className="text-xs text-text-secondary">Turista</dt><dd className="mt-1 break-words text-text-primary">{trip.turistaNombreCompleto}</dd></div>
      <div><dt className="text-xs text-text-secondary">WhatsApp</dt><dd className="mt-1 break-words text-text-primary">{trip.turistaWhatsapp}</dd></div>
      <div><dt className="text-xs text-text-secondary">Última ubicación</dt><dd className="mt-1 break-words text-text-primary">{trip.ultimaUbicacionConfirmada ?? "—"}</dd></div>
      <div><dt className="text-xs text-text-secondary">Fecha y hora</dt><dd className="mt-1 text-text-primary">{departure.date} {departure.time}</dd></div>
    </dl>
    {(trip.estado === "EN_VIAJE" || trip.estado === "FINALIZADO") && <div className="mt-3 border-t border-border-secondary pt-3"><p className="mb-2 text-xs font-semibold text-text-secondary">Acciones</p><div className="flex flex-col gap-2">{trip.estado === "EN_VIAJE" && <BotonCompletar onClick={onFinish} finishing={finishing} />}<BotonPublicarRetorno onClick={onPublish} /></div></div>}
  </article>;
}

export default function ViajesPage({ token }: Props) {
  const [activeTab, setActiveTab] = useState<"viajes" | "tramos-vacios">("viajes");
  const [estadoFiltro, setEstadoFiltro] = useState("EN_VIAJE");
  const [page, setPage] = useState(0);
  const [reload, setReload] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [trips, setTrips] = useState<ViajeSeguimiento[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [selected, setSelected] = useState<ViajeSeguimiento | null>(null);
  const [finishing, setFinishing] = useState(false);
  const [notice, setNotice] = useState<{ id: number; kind: NotificationKind; title: string; message: string } | null>(null);
  const [viajeFinalizadoReciente, setViajeFinalizadoReciente] = useState<ViajeFinalizado | null>(null);
  const [viajeParaRetorno, setViajeParaRetorno] = useState<ViajeSeguimiento | null>(null);
  const [reloadTramos, setReloadTramos] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    void listarSeguimientoViajes(token, page, controller.signal, estadoFiltro).then((result) => {
      if (controller.signal.aborted) return;
      setTrips(result.content);
      setTotalPages(result.totalPages);
    }).catch((cause: unknown) => {
      if (!controller.signal.aborted) setError(cause instanceof Error ? cause.message : "No se pudieron cargar los viajes.");
    }).finally(() => {
      if (!controller.signal.aborted) setLoading(false);
    });
    return () => controller.abort();
  }, [token, page, reload, estadoFiltro]);

  // /admin/viajes/seguimiento ya devuelve el historial ordenado por fecha descendente.
  const ordered = trips;
  const retry = () => {
    setLoading(true);
    setError(null);
    setReload((value) => value + 1);
  };
  const cambiarFiltroEstado = (estado: string) => {
    if (estado === estadoFiltro) return;
    setLoading(true);
    setError(null);
    setPage(0);
    setEstadoFiltro(estado);
  };
  const completar = async () => {
    if (!selected || finishing) return;
    setFinishing(true);
    try {
      const resultado = await finalizarViaje(token, selected.id);
      setSelected(null);
      setNotice({ id: Date.now(), kind: "success", title: "Viaje finalizado", message: "El viaje se marcó como completado correctamente." });
      retry();
      setViajeFinalizadoReciente(resultado);
    } catch (cause) {
      setNotice({ id: Date.now(), kind: "error", title: "No se pudo finalizar el viaje", message: cause instanceof Error ? cause.message : "Inténtalo nuevamente." });
    } finally { setFinishing(false); }
  };
  const retornoPublicado = (tramo: TramoVacio) => {
    setViajeParaRetorno(null);
    setReloadTramos((value) => value + 1);
    setActiveTab("tramos-vacios");
    setNotice({
      id: Date.now(),
      kind: "success",
      title: "Retorno publicado",
      message: `Retorno ${tramo.id} de ${tramo.zonaOrigen} a ${tramo.zonaDestino} publicado correctamente.`,
    });
  };

  return <div className="w-full pt-4 font-inter text-text-primary">
    <div className="mx-auto w-[96%] max-w-[1400px]">
      <header className="mb-5"><h1 className="text-2xl font-bold leading-8">{activeTab === "viajes" ? "Viajes asignados" : "Tramos vacíos"}</h1><p className="mt-1 text-sm text-text-secondary">{activeTab === "viajes" ? "Consulta los viajes registrados y su estado actual." : "Consulta, comparte y gestiona los retornos publicados."}</p></header>
      <nav role="tablist" aria-label="Secciones de viajes" className="mb-3 flex max-w-full flex-nowrap gap-0.5 overflow-x-auto rounded-full bg-bg-surface-secondary p-1 lg:inline-flex lg:flex-wrap lg:overflow-visible [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
        <button type="button" role="tab" id="tab-viajes" aria-pressed={activeTab === "viajes"} aria-selected={activeTab === "viajes"} aria-controls="panel-viajes" onClick={() => setActiveTab("viajes")} className={`shrink-0 rounded-full px-2 py-1.5 text-xs font-semibold leading-5 transition-colors sm:px-3 sm:text-sm ${activeTab === "viajes" ? "bg-white text-text-primary shadow-sm" : "text-text-secondary hover:text-text-primary"}`}>Viajes</button>
        <button type="button" role="tab" id="tab-tramos-vacios" aria-pressed={activeTab === "tramos-vacios"} aria-selected={activeTab === "tramos-vacios"} aria-controls="panel-tramos-vacios" onClick={() => setActiveTab("tramos-vacios")} className={`shrink-0 rounded-full px-2 py-1.5 text-xs font-semibold leading-5 transition-colors sm:px-3 sm:text-sm ${activeTab === "tramos-vacios" ? "bg-white text-text-primary shadow-sm" : "text-text-secondary hover:text-text-primary"}`}>Tramos vacíos</button>
      </nav>
      {activeTab === "viajes" && <nav aria-label="Filtrar viajes por estado" className="mb-3 flex max-w-full flex-nowrap gap-0.5 overflow-x-auto rounded-full bg-bg-surface-secondary p-1 lg:inline-flex lg:flex-wrap lg:overflow-visible [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
        {[{ value: "TODOS", label: "Todos" }, { value: "EN_VIAJE", label: "En viaje" }, { value: "FINALIZADO", label: "Finalizados" }].map(({ value, label }) => <button key={value} type="button" aria-pressed={estadoFiltro === value} onClick={() => cambiarFiltroEstado(value)} className={`shrink-0 rounded-full px-2 py-1.5 text-xs font-semibold leading-5 transition-colors sm:px-3 sm:text-sm ${estadoFiltro === value ? "bg-white text-text-primary shadow-sm" : "text-text-secondary hover:text-text-primary"}`}>{label}</button>)}
      </nav>}
      {activeTab === "tramos-vacios" ? <TramosVaciosPanel token={token} reloadKey={reloadTramos} /> : <section role="tabpanel" id="panel-viajes" aria-labelledby="tab-viajes" className="overflow-visible rounded-none border-0 bg-transparent lg:overflow-hidden lg:rounded-[9px] lg:border lg:border-border-secondary lg:bg-white">
        {loading ? <div role="status" className="flex items-center justify-center gap-3 bg-white px-4 py-8 text-sm text-text-secondary lg:bg-transparent lg:py-12"><span className="h-5 w-5 animate-spin rounded-full border-2 border-bg-fill-secondary border-t-bg-fill-primary" />Cargando viajes…</div>
          : error ? <div className="rounded-[11px] bg-white px-4 py-8 text-center lg:bg-transparent"><p role="alert" className="text-sm text-text-error">{error}</p><button type="button" onClick={retry} className="mt-4 rounded-full bg-bg-fill-primary px-5 py-2.5 text-sm font-semibold text-text-invert">Reintentar</button></div>
            : ordered.length === 0 ? <div className="flex min-h-[300px] flex-col items-center rounded-[11px] border border-border-secondary bg-white px-5 pb-6 pt-7 text-center lg:min-h-[440px] lg:rounded-none lg:border-0 lg:bg-transparent lg:pt-10"><h2 className="text-xl font-semibold">Sin viajes asignados</h2><p className="mt-2 text-sm text-text-secondary">No hay viajes asignados para mostrar</p><img src="/solicitudes/sinresultados.png" alt="No hay resultados" className="mt-5 h-auto w-full max-w-[560px] object-contain" /></div>
              : <>
                <div className="grid gap-3 md:hidden">{ordered.map((trip, index) => <ViajeCard key={trip.id} trip={trip} index={page * 10 + index + 1} onFinish={() => setSelected(trip)} onPublish={() => setViajeParaRetorno(trip)} finishing={finishing && selected?.id === trip.id} />)}</div>
                <div className="hidden overflow-x-auto md:block"><table className="w-full min-w-[1160px] table-fixed border-collapse text-left text-sm"><colgroup><col className="w-[4%]" /><col className="w-[11%]" /><col className="w-[15%]" /><col className="w-[10%]" /><col className="w-[14%]" /><col className="w-[12%]" /><col className="w-[12%]" /><col className="w-[8%]" /><col className="w-[14%]" /></colgroup><thead className="bg-bg-surface-primary text-xs font-semibold text-text-secondary"><tr>{["Nro", "Lancha", "Turista", "WhatsApp", "Ruta", "Última ubicación", "Fecha y hora", "Estado", "Acciones"].map((label) => <th key={label} scope="col" className="border-b border-border-secondary px-2.5 py-2.5 text-left font-semibold sm:px-3">{label}</th>)}</tr></thead><tbody>{ordered.map((trip, index) => { const departure = fechaYHora(trip.fechaHoraInicio); const isFinishing = finishing && selected?.id === trip.id; return <tr key={trip.id} className="border-b border-border-secondary text-text-primary transition-colors last:border-0 hover:bg-bg-surface-primary"><td className="whitespace-nowrap px-2.5 py-2.5 text-text-secondary sm:px-3">{page * 10 + index + 1}</td><td className="px-2.5 py-2.5 font-semibold sm:px-3">{trip.nombreEmbarcacion}</td><td className="break-words px-2.5 py-2.5 sm:px-3">{trip.turistaNombreCompleto}</td><td className="whitespace-nowrap px-2.5 py-2.5 text-sm sm:px-3">{trip.turistaWhatsapp}</td><td className="px-2.5 py-2.5 sm:px-3"><Ruta trip={trip} /></td><td className="break-words px-2.5 py-2.5 text-sm sm:px-3">{trip.ultimaUbicacionConfirmada ?? "—"}</td><td className="px-2.5 py-2.5 sm:px-3"><span className="block whitespace-nowrap font-medium">{departure.date}</span>{departure.time && <span className="mt-0.5 block text-xs text-text-secondary">{departure.time}</span>}</td><td className="px-2.5 py-2.5 text-center sm:px-3"><EstadoBadge estado={trip.estado} /></td><td className="px-2.5 py-2.5 sm:px-3">{trip.estado === "EN_VIAJE" ? <div className="flex flex-col gap-1.5"><BotonCompletar onClick={() => setSelected(trip)} finishing={isFinishing} /><BotonPublicarRetorno onClick={() => setViajeParaRetorno(trip)} /></div> : trip.estado === "FINALIZADO" ? <BotonPublicarRetorno onClick={() => setViajeParaRetorno(trip)} /> : <span className="text-text-secondary">—</span>}</td></tr>; })}</tbody></table></div>
              </>}
      </section>}
      {activeTab === "viajes" && !loading && !error && ordered.length > 0 && <SolicitudesPaginacion page={page} totalPages={totalPages} onChange={(nextPage) => { setLoading(true); setPage(nextPage); }} />}
    </div>
    {selected && <ModalDecision title="¿Finalizar viaje?" description={`Confirma que el viaje de ${selected.turistaNombreCompleto} ha terminado. Esta acción liberará la lancha.`} onClose={() => { if (!finishing) setSelected(null); }} closeDisabled={finishing}><div className="mt-6 flex justify-end gap-3"><button type="button" onClick={() => setSelected(null)} disabled={finishing} className="rounded-full border border-border-secondary px-5 py-2.5 text-sm font-semibold text-text-primary disabled:opacity-60">Cancelar</button><button type="button" onClick={() => void completar()} disabled={finishing} className="rounded-full bg-bg-fill-primary px-5 py-2.5 text-sm font-semibold text-text-invert disabled:opacity-60">{finishing ? "Finalizando…" : "Confirmar"}</button></div></ModalDecision>}
    {viajeParaRetorno && <PublicarTramoVacioModal token={token} viajeId={viajeParaRetorno.id} lanchaNombre={viajeParaRetorno.nombreEmbarcacion} zonaOrigen={viajeParaRetorno.zonaOrigen} zonaDestino={viajeParaRetorno.zonaDestino} onClose={() => setViajeParaRetorno(null)} onPublished={retornoPublicado} onError={(message) => setNotice({ id: Date.now(), kind: "error", title: "No se pudo publicar el retorno", message })} />}
    {viajeFinalizadoReciente && (
      <ModalSolicitarResena
        viajeFinalizado={viajeFinalizadoReciente}
        onClose={() => setViajeFinalizadoReciente(null)}
      />
    )}
    {notice && <div className="pointer-events-none fixed right-4 top-4 z-[60]"><Notification key={notice.id} kind={notice.kind} title={notice.title} message={notice.message} onClose={() => setNotice(null)} /></div>}
  </div>;
}
