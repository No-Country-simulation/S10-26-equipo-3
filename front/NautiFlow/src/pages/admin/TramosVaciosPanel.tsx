import { useEffect, useState, type Dispatch, type SetStateAction } from "react";
import {
  BsArrowLeftRight,
  BsArrowRight,
  BsCalendar3,
  BsCheck2,
  BsClipboard,
  BsClock,
  BsPerson,
  BsSend,
  BsWhatsapp,
} from "react-icons/bs";
import ModalDecision from "../../components/Admin/ModalDecision";
import Notification, { type NotificationKind } from "../../components/Admin/Notification";
import {
  cancelarTramoVacio,
  finalizarTramoVacio,
  listarTramosVacios,
  obtenerMensajeTramoVacio,
  type FiltrosTramosVacios,
  type TramoVacio,
} from "../../services/tramoVacioAdminService";
import { formatReturnTime } from "../../utils/formatReturnTime";

type Props = {
  token: string;
  reloadKey: number;
};

type Aviso = {
  id: number;
  kind: NotificationKind;
  title: string;
  message: string;
};

type SeleccionCierre = { tramo: TramoVacio; accion: "cancelar" | "finalizar" };

function fechaLegible(fecha: string): string {
  const date = new Date(`${fecha}T00:00:00Z`);
  if (Number.isNaN(date.getTime())) return fecha;
  return new Intl.DateTimeFormat("es-AR", {
    timeZone: "UTC",
    day: "numeric",
    month: "short",
    year: "numeric",
  }).format(date);
}

function abrirWhatsApp(tramo: TramoVacio) {
  const url = `https://wa.me/?text=${encodeURIComponent(tramo.mensaje)}`;
  window.open(url, "_blank", "noopener,noreferrer");
}

function DatosTramo({
  tramo,
  copiarMensaje,
  mensajeCopiado,
  setSeleccionado,
}: {
  tramo: TramoVacio;
  copiarMensaje: (tramo: TramoVacio) => Promise<void>;
  mensajeCopiado: number | null;
  setSeleccionado: Dispatch<SetStateAction<SeleccionCierre | null>>;
}) {
  return (
    <article className="flex h-full flex-col overflow-hidden rounded-xl border border-border-secondary bg-white shadow-sm transition-shadow hover:shadow-md">
      <div className="flex items-start justify-between gap-2.5 border-b border-border-secondary p-3 sm:p-4">
        <div className="flex min-w-0 items-center gap-2.5">
          <span className="grid h-10 w-10 shrink-0 place-items-center rounded-xl bg-bg-info-tertiary text-bg-fill-primary">
            <BsArrowLeftRight className="h-5 w-5" aria-hidden="true" />
          </span>
          <div className="min-w-0">
            <h3 className="truncate text-base font-semibold text-text-primary">{tramo.lanchaNombre}</h3>
            <p className="mt-0.5 truncate text-sm text-text-secondary">{tramo.lancheroNombre}</p>
          </div>
        </div>
        <div className="flex shrink-0 flex-col items-end gap-1.5">
          <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${tramo.estado === "ACTIVO" ? "bg-bg-success-tertiary text-text-success" : "bg-bg-surface-secondary text-text-secondary"}`}>
            {tramo.estado === "ACTIVO" ? "Disponible" : "Inactivo"}
          </span>
          <span className="text-xs text-text-secondary">Retorno #{tramo.id}</span>
        </div>
      </div>

      <div className="flex flex-1 flex-col p-3 sm:p-4">
        <div className="rounded-xl bg-bg-surface-primary p-3">
          <div className="mb-2 flex items-center justify-between gap-3">
            <p className="text-xs font-semibold uppercase tracking-wide text-text-secondary">Recorrido de vuelta</p>
            <span className="inline-flex items-center gap-1.5 text-xs font-medium text-text-secondary">
              <BsCalendar3 aria-hidden="true" />
              {fechaLegible(tramo.fecha)}
            </span>
          </div>
          <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-2">
            <div className="min-w-0">
              <p className="text-xs text-text-secondary">Desde</p>
              <p className="mt-1 break-words text-sm font-semibold text-text-primary">{tramo.zonaOrigen}</p>
            </div>
            <span className="grid h-8 w-8 place-items-center rounded-full bg-white text-bg-fill-primary shadow-sm">
              <BsArrowRight className="h-4 w-4" aria-hidden="true" />
            </span>
            <div className="min-w-0 text-right">
              <p className="text-xs text-text-secondary">Hasta</p>
              <p className="mt-1 break-words text-sm font-semibold text-text-primary">{tramo.zonaDestino}</p>
            </div>
          </div>
        </div>

        <div className="mt-3 grid grid-cols-2 gap-2.5">
          <div className="rounded-lg border border-border-secondary p-2.5">
            <p className="inline-flex items-center gap-1.5 text-xs text-text-secondary">
              <BsClock aria-hidden="true" />
              Hora de retorno
            </p>
            <p className="mt-1 text-sm font-semibold text-text-primary">{formatReturnTime(tramo.horaRetorno)}</p>
          </div>
          <div className="rounded-lg border border-border-secondary p-2.5">
            <p className="text-xs text-text-secondary">Viaje asociado</p>
            <p className="mt-1 text-sm font-semibold text-text-primary">N.º {tramo.viajeId}</p>
          </div>
          <div className="col-span-2 rounded-lg border border-border-secondary p-2.5">
            <p className="inline-flex items-center gap-1.5 text-xs text-text-secondary">
              <BsPerson aria-hidden="true" />
              Turista interesado
            </p>
            <p className="mt-1 break-words text-sm font-medium text-text-primary">
              {tramo.turistaNombreCompleto ?? "Sin asignar"}
            </p>
            {tramo.turistaWhatsapp && (
              <a
                href={`https://wa.me/${tramo.turistaWhatsapp.replace(/\D/g, "")}`}
                target="_blank"
                rel="noopener noreferrer"
                className="mt-1 inline-flex items-center gap-1.5 text-sm font-medium text-bg-fill-primary hover:underline"
              >
                <BsWhatsapp aria-hidden="true" />
                {tramo.turistaWhatsapp}
              </a>
            )}
          </div>
        </div>

        <div className="mt-3 flex-1 rounded-lg border border-border-info bg-bg-info-tertiary p-2.5">
          <p className="text-xs font-semibold text-text-info">Mensaje para compartir</p>
          <p className="mt-1 text-sm leading-5 text-text-primary">{tramo.mensaje}</p>
        </div>

        <div className="mt-3 flex flex-wrap gap-2">
          <button type="button" onClick={() => void copiarMensaje(tramo)} className="inline-flex min-h-10 flex-1 items-center justify-center gap-2 rounded-full border border-border-secondary px-3 text-sm font-semibold text-text-primary transition-colors hover:bg-bg-surface-secondary">
            {mensajeCopiado === tramo.id ? <BsCheck2 aria-hidden="true" /> : <BsClipboard aria-hidden="true" />}
            {mensajeCopiado === tramo.id ? "Copiado" : "Copiar mensaje"}
          </button>
          <button type="button" onClick={() => abrirWhatsApp(tramo)} className="inline-flex min-h-10 flex-1 items-center justify-center gap-2 rounded-full bg-bg-fill-primary px-3 text-sm font-semibold text-text-invert transition-colors hover:bg-bg-fill-primary-hover">
            <BsSend aria-hidden="true" />
            Compartir
          </button>
          {tramo.estado === "ACTIVO" && (
            <div className="flex w-full gap-2 border-t border-border-secondary pt-2.5">
              <button type="button" onClick={() => setSeleccionado({ tramo, accion: "cancelar" })} className="min-h-10 flex-1 rounded-full border border-border-secondary px-3 text-sm font-semibold text-text-secondary transition-colors hover:bg-bg-surface-secondary hover:text-text-primary">
                Cancelar retorno
              </button>
              <button type="button" onClick={() => setSeleccionado({ tramo, accion: "finalizar" })} className="min-h-10 flex-1 rounded-full border border-border-info px-3 text-sm font-semibold text-bg-fill-primary transition-colors hover:bg-bg-info-tertiary">
                Finalizar retorno
              </button>
            </div>
          )}
        </div>
      </div>
    </article>
  );
}

export default function TramosVaciosPanel({ token, reloadKey }: Props) {
  const [fechaDesde, setFechaDesde] = useState("");
  const [fechaHasta, setFechaHasta] = useState("");
  const [filtros, setFiltros] = useState<FiltrosTramosVacios>({ estado: "ACTIVO" });
  const [tramos, setTramos] = useState<TramoVacio[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadLocal, setReloadLocal] = useState(0);
  const [seleccionado, setSeleccionado] = useState<SeleccionCierre | null>(null);
  const [guardando, setGuardando] = useState(false);
  const [aviso, setAviso] = useState<Aviso | null>(null);
  const [mensajeCopiado, setMensajeCopiado] = useState<number | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    void listarTramosVacios(token, filtros, controller.signal).then(setTramos).catch((cause: unknown) => {
      if (!controller.signal.aborted) {
        setError(cause instanceof Error ? cause.message : "No se pudieron cargar los tramos vacíos.");
      }
    }).finally(() => {
      if (!controller.signal.aborted) setCargando(false);
    });
    return () => controller.abort();
  }, [token, filtros, reloadKey, reloadLocal]);

  const recargar = () => {
    setCargando(true);
    setError(null);
    setReloadLocal((value) => value + 1);
  };
  const aplicarFiltros = () => {
    setCargando(true);
    setError(null);
    setFiltros({
      fechaDesde: fechaDesde || undefined,
      fechaHasta: fechaHasta || undefined,
      estado: filtros.estado ?? "ACTIVO",
    });
  };
  const cambiarEstado = (estado: TramoVacio["estado"]) => {
    setCargando(true);
    setError(null);
    setFiltros((actuales) => ({ ...actuales, estado }));
  };

  const copiarMensaje = async (tramo: TramoVacio) => {
    try {
      const mensaje = await obtenerMensajeTramoVacio(token, tramo.id);
      if (!navigator.clipboard?.writeText) {
        throw new Error("El navegador no permite copiar el mensaje desde esta página.");
      }
      await navigator.clipboard.writeText(mensaje);
      setMensajeCopiado(tramo.id);
      setAviso({ id: Date.now(), kind: "success", title: "Mensaje copiado", message: "Ya puedes pegarlo y compartirlo por WhatsApp." });
      window.setTimeout(() => setMensajeCopiado((id) => id === tramo.id ? null : id), 2000);
    } catch (cause) {
      setAviso({ id: Date.now(), kind: "error", title: "No se pudo copiar", message: cause instanceof Error ? cause.message : "Inténtalo nuevamente." });
    }
  };

  const cerrarTramo = async () => {
    if (!seleccionado || guardando) return;
    setGuardando(true);
    try {
      if (seleccionado.accion === "cancelar") {
        await cancelarTramoVacio(token, seleccionado.tramo.id);
      } else {
        await finalizarTramoVacio(token, seleccionado.tramo.id);
      }
      setAviso({
        id: Date.now(),
        kind: "success",
        title: seleccionado.accion === "cancelar" ? "Retorno cancelado" : "Retorno finalizado",
        message: "El registro se conservó en el historial.",
      });
      setSeleccionado(null);
      recargar();
    } catch (cause) {
      setAviso({ id: Date.now(), kind: "error", title: "No se pudo cerrar el retorno", message: cause instanceof Error ? cause.message : "Inténtalo nuevamente." });
    } finally {
      setGuardando(false);
    }
  };

  const validarRango = Boolean(fechaDesde && fechaHasta && fechaDesde > fechaHasta);

  return (
    <section role="tabpanel" id="panel-tramos-vacios" aria-labelledby="tab-tramos-vacios" className="space-y-5">
      <div className="overflow-hidden rounded-xl border border-border-secondary bg-white">
        <div className="flex flex-col gap-4 border-b border-border-secondary bg-bg-surface-primary p-4 sm:flex-row sm:items-center sm:justify-between sm:p-5">
          <div>
            <div className="flex items-center gap-2">
              <span className="grid h-9 w-9 place-items-center rounded-lg bg-bg-info-tertiary text-bg-fill-primary">
                <BsArrowLeftRight aria-hidden="true" />
              </span>
              <h2 className="text-lg font-semibold text-text-primary">Gestión de retornos</h2>
            </div>
            <p className="mt-2 text-sm text-text-secondary">Revisa horarios, comparte el trayecto y administra su disponibilidad.</p>
          </div>
          <span className="inline-flex w-fit items-center gap-2 rounded-full border border-border-secondary bg-white px-3 py-1.5 text-sm font-medium text-text-secondary">
            <span className="h-2 w-2 rounded-full bg-bg-fill-primary" aria-hidden="true" />
            {tramos.length} {tramos.length === 1 ? "resultado" : "resultados"}
          </span>
        </div>
        <div className="space-y-4 p-4 sm:p-5">
          <div className="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
            <div>
              <p className="text-sm font-semibold text-text-primary">Filtrar por estado</p>
              <div className="mt-2 inline-flex rounded-full bg-bg-surface-secondary p-1" aria-label="Filtrar retornos por estado">
                {(["ACTIVO", "INACTIVO"] as const).map((estado) => (
                  <button key={estado} type="button" aria-pressed={filtros.estado === estado} onClick={() => cambiarEstado(estado)} className={`rounded-full px-4 py-2 text-sm font-semibold transition-colors ${filtros.estado === estado ? "bg-white text-text-primary shadow-sm" : "text-text-secondary hover:text-text-primary"}`}>
                    {estado === "ACTIVO" ? "Disponibles" : "Historial"}
                  </button>
                ))}
              </div>
            </div>
            <div>
              <p className="text-sm font-semibold text-text-primary">Rango de fechas</p>
              <div className="mt-2 grid gap-3 sm:grid-cols-2 xl:flex xl:items-end">
                <label className="text-xs font-semibold text-text-secondary">
                  Desde
                  <input type="date" value={fechaDesde} onChange={(event) => setFechaDesde(event.target.value)} className="mt-1 block min-h-10 w-full rounded-lg border border-border-secondary bg-white px-3 text-sm text-text-primary focus:border-border-info focus:outline-none focus:ring-2 focus:ring-border-info/20" />
                </label>
                <label className="text-xs font-semibold text-text-secondary">
                  Hasta
                  <input type="date" value={fechaHasta} onChange={(event) => setFechaHasta(event.target.value)} className="mt-1 block min-h-10 w-full rounded-lg border border-border-secondary bg-white px-3 text-sm text-text-primary focus:border-border-info focus:outline-none focus:ring-2 focus:ring-border-info/20" />
                </label>
                <div className="flex gap-2 sm:col-span-2 xl:col-span-1">
                  <button type="button" onClick={aplicarFiltros} disabled={validarRango} className="min-h-10 flex-1 rounded-full bg-bg-fill-primary px-4 text-sm font-semibold text-text-invert transition-colors hover:bg-bg-fill-primary-hover disabled:cursor-not-allowed disabled:opacity-50">
                    Aplicar
                  </button>
                  <button type="button" onClick={() => { setFechaDesde(""); setFechaHasta(""); setCargando(true); setError(null); setFiltros((actuales) => ({ estado: actuales.estado })); }} disabled={!fechaDesde && !fechaHasta} className="min-h-10 flex-1 rounded-full border border-border-secondary px-4 text-sm font-semibold text-text-secondary transition-colors hover:bg-bg-surface-secondary disabled:cursor-not-allowed disabled:opacity-50">
                    Limpiar
                  </button>
                </div>
              </div>
            </div>
          </div>
          {validarRango && <p role="alert" className="text-sm text-text-error">La fecha inicial no puede ser posterior a la fecha final.</p>}
        </div>
      </div>

      <div>
        {cargando ? (
          <div role="status" className="flex items-center justify-center gap-3 rounded-xl border border-border-secondary bg-white px-4 py-16 text-sm text-text-secondary">
            <span className="h-5 w-5 animate-spin rounded-full border-2 border-bg-fill-secondary border-t-bg-fill-primary" />
            Cargando tramos vacíos…
          </div>
        ) : error ? (
          <div className="rounded-xl border border-border-secondary bg-white px-4 py-12 text-center">
            <p role="alert" className="text-sm text-text-error">{error}</p>
            <button type="button" onClick={recargar} className="mt-4 rounded-full bg-bg-fill-primary px-5 py-2.5 text-sm font-semibold text-text-invert">Reintentar</button>
          </div>
        ) : tramos.length === 0 ? (
          <div className="rounded-xl border border-border-secondary bg-white px-4 py-16 text-center">
            <span className="mx-auto grid h-14 w-14 place-items-center rounded-full bg-bg-info-tertiary text-bg-fill-primary">
              <BsArrowLeftRight className="h-6 w-6" aria-hidden="true" />
            </span>
            <h3 className="mt-4 text-lg font-semibold text-text-primary">{filtros.estado === "ACTIVO" ? "Aún no hay retornos disponibles" : "No hay retornos en el historial"}</h3>
            <p className="mx-auto mt-2 max-w-prose text-sm leading-5 text-text-secondary">Cuando publiques un tramo vacío, aparecerá aquí para que puedas compartirlo y gestionar su estado.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 items-stretch gap-4 xl:grid-cols-2">
            {tramos.map((tramo) => (
              <DatosTramo
                key={tramo.id}
                tramo={tramo}
                copiarMensaje={copiarMensaje}
                mensajeCopiado={mensajeCopiado}
                setSeleccionado={setSeleccionado}
              />
            ))}
          </div>
        )}
      </div>

      {seleccionado && (
        <ModalDecision
          title={seleccionado.accion === "cancelar" ? "¿Cancelar este retorno?" : "¿Finalizar este retorno?"}
          description={seleccionado.accion === "cancelar"
            ? "El retorno dejará de estar disponible, pero quedará en el historial. El viaje original no se modificará."
            : "El viaje original debe estar finalizado. El retorno dejará de estar disponible y se conservará en el historial."}
          onClose={() => { if (!guardando) setSeleccionado(null); }}
          closeDisabled={guardando}
        >
          <div className="mt-6 flex justify-end gap-3">
            <button type="button" onClick={() => setSeleccionado(null)} disabled={guardando} className="min-h-10 rounded-full border border-border-secondary px-5 text-sm font-semibold text-text-primary disabled:opacity-60">
              Volver
            </button>
            <button type="button" onClick={() => void cerrarTramo()} disabled={guardando} className="min-h-10 rounded-full bg-bg-fill-primary px-5 text-sm font-semibold text-text-invert disabled:opacity-60">
              {guardando ? "Guardando…" : seleccionado.accion === "cancelar" ? "Confirmar cancelación" : "Confirmar finalización"}
            </button>
          </div>
        </ModalDecision>
      )}
      {aviso && <div className="pointer-events-none fixed right-4 top-4 z-[60]"><Notification key={aviso.id} kind={aviso.kind} title={aviso.title} message={aviso.message} onClose={() => setAviso(null)} /></div>}
    </section>
  );
}
