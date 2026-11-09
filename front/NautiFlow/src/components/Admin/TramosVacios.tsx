import { useEffect, useState } from "react";
import { BsArrowRight, BsChatText, BsClock, BsXLg } from "react-icons/bs";
import { FaShip } from "react-icons/fa";
import ModalDecision from "./ModalDecision";
import ModalMensajeTramo from "./ModalMensajeTramo";
import { listarTramosVacios, quitarTramoVacio, type TramoVacio } from "../../services/tramoVacioService";
import { ZONA_HORARIA } from "../../config/api";

type Props = {
  token: string;
  onNotice: (kind: "success" | "error", title: string, message: string) => void;
};

function horaYDia(value: string): { hora: string; dia: string } {
  const fecha = new Date(value);
  if (Number.isNaN(fecha.getTime())) return { hora: value, dia: "" };
  return {
    hora: new Intl.DateTimeFormat("es-AR", { timeZone: ZONA_HORARIA, hour: "2-digit", minute: "2-digit", hourCycle: "h23" }).format(fecha),
    dia: new Intl.DateTimeFormat("es-AR", { timeZone: ZONA_HORARIA, weekday: "short", day: "numeric", month: "short" }).format(fecha),
  };
}

function TramoCard({ tramo, ahora, onVerMensaje, onQuitar }: { tramo: TramoVacio; ahora: number; onVerMensaje: () => void; onQuitar: () => void }) {
  const { hora, dia } = horaYDia(tramo.fechaHoraRetorno);
  const pasado = new Date(tramo.fechaHoraRetorno).getTime() < ahora;
  return (
    <article className={`relative rounded-[11px] border border-border-secondary bg-white p-4 transition-opacity ${pasado ? "opacity-55" : ""}`}>
      <button
        type="button"
        onClick={onQuitar}
        aria-label={`Quitar el tramo de ${tramo.nombreEmbarcacion}`}
        className="absolute right-3 top-3 grid h-8 w-8 place-items-center rounded-md text-text-secondary hover:bg-bg-surface-secondary hover:text-text-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary"
      >
        <BsXLg className="h-4 w-4" aria-hidden="true" />
      </button>
      <div className="flex min-w-0 items-center gap-2.5 pr-10">
        <span className="grid h-9 w-9 shrink-0 place-items-center rounded-lg bg-bg-surface-primary text-bg-fill-primary"><FaShip aria-hidden="true" /></span>
        <div className="min-w-0">
          <h3 className="truncate text-sm font-semibold text-text-primary">{tramo.nombreEmbarcacion}</h3>
          <p className="text-xs text-text-secondary">Capacidad: {tramo.capacidad}</p>
        </div>
      </div>
      <div className="mt-3 rounded-lg bg-bg-surface-primary p-3">
        <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-text-secondary">Trayecto vacío</p>
        <div className="flex min-w-0 items-center gap-1.5 text-sm">
          <span className="min-w-0 flex-1 break-words font-semibold text-text-primary">{tramo.zonaOrigen}</span>
          <BsArrowRight className="h-4 w-4 shrink-0 text-text-disable" aria-hidden="true" />
          <span className="min-w-0 flex-1 break-words text-right text-text-primary">{tramo.zonaDestino}</span>
        </div>
      </div>
      <div className="mt-3 flex flex-wrap items-center justify-between gap-2">
        <p className="inline-flex items-center gap-1.5 text-sm text-text-primary">
          <BsClock className="h-4 w-4 text-text-secondary" aria-hidden="true" />
          <span className="font-semibold">{hora}</span>
          <span className="text-text-secondary">· {dia}</span>
          {pasado && <span className="ml-1 rounded-full bg-bg-surface-secondary px-2 py-0.5 text-xs font-semibold text-text-secondary">Hora pasada</span>}
        </p>
        <button
          type="button"
          onClick={onVerMensaje}
          className="inline-flex h-9 items-center gap-1.5 rounded-lg border border-border-secondary px-3 text-xs font-semibold text-text-primary transition-colors hover:bg-bg-surface-secondary"
        >
          <BsChatText className="h-3.5 w-3.5" aria-hidden="true" />
          Ver mensaje
        </button>
      </div>
    </article>
  );
}

export default function TramosVacios({ token, onNotice }: Props) {
  const [tramos, setTramos] = useState<TramoVacio[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [recarga, setRecarga] = useState(0);
  const [mensaje, setMensaje] = useState<TramoVacio | null>(null);
  const [aQuitar, setAQuitar] = useState<TramoVacio | null>(null);
  const [quitando, setQuitando] = useState(false);
  const [ahora] = useState(() => Date.now());

  useEffect(() => {
    const controller = new AbortController();
    void listarTramosVacios(token, controller.signal).then((resultado) => {
      if (!controller.signal.aborted) setTramos(resultado);
    }).catch((cause: unknown) => {
      if (!controller.signal.aborted) setError(cause instanceof Error ? cause.message : "No se pudieron cargar los tramos vacíos.");
    }).finally(() => {
      if (!controller.signal.aborted) setCargando(false);
    });
    return () => controller.abort();
  }, [token, recarga]);

  const reintentar = () => {
    setCargando(true);
    setError(null);
    setRecarga((value) => value + 1);
  };

  const quitar = async () => {
    if (!aQuitar || quitando) return;
    setQuitando(true);
    try {
      await quitarTramoVacio(token, aQuitar.id);
      setTramos((actuales) => actuales.filter((tramo) => tramo.id !== aQuitar.id));
      onNotice("success", "Tramo quitado", `Se quitó el retorno de ${aQuitar.nombreEmbarcacion}.`);
      setAQuitar(null);
    } catch (cause) {
      onNotice("error", "No se pudo quitar el tramo", cause instanceof Error ? cause.message : "Inténtalo nuevamente.");
    } finally {
      setQuitando(false);
    }
  };

  if (cargando) {
    return <div role="status" className="flex items-center justify-center gap-3 bg-white px-4 py-8 text-sm text-text-secondary lg:bg-transparent lg:py-12"><span className="h-5 w-5 animate-spin rounded-full border-2 border-bg-fill-secondary border-t-bg-fill-primary" />Cargando tramos vacíos…</div>;
  }

  if (error) {
    return <div className="rounded-[11px] bg-white px-4 py-8 text-center lg:bg-transparent"><p role="alert" className="text-sm text-text-error">{error}</p><button type="button" onClick={reintentar} className="mt-4 rounded-full bg-bg-fill-primary px-5 py-2.5 text-sm font-semibold text-text-invert">Reintentar</button></div>;
  }

  return (
    <>
      {tramos.length === 0 ? (
        <div className="flex min-h-[300px] flex-col items-center rounded-[11px] border border-border-secondary bg-white px-5 pb-6 pt-7 text-center sm:min-h-[540px] sm:pt-12 lg:min-h-[480px] lg:rounded-none lg:border-0 lg:bg-transparent lg:px-6 lg:pb-10 lg:pt-10 2xl:min-h-[540px] 2xl:pt-12">
          <h2 className="text-xl font-semibold text-text-primary">Sin tramos vacíos</h2>
          <p className="mt-2 text-sm text-text-secondary">Publica un retorno desde un viaje para ofrecerlo a la flota.</p>
          <img src="/solicitudes/sinresultados.png" alt="Ilustración que indica que no hay tramos vacíos" className="mt-5 h-auto w-full max-w-[560px] object-contain sm:mt-8 md:max-w-[760px] lg:max-w-[900px]" />
        </div>
      ) : (
        <div className="grid gap-3 p-0 sm:grid-cols-2 lg:p-4 xl:grid-cols-3">
          {tramos.map((tramo) => (
            <TramoCard key={tramo.id} tramo={tramo} ahora={ahora} onVerMensaje={() => setMensaje(tramo)} onQuitar={() => setAQuitar(tramo)} />
          ))}
        </div>
      )}
      {mensaje && <ModalMensajeTramo tramo={mensaje} onClose={() => setMensaje(null)} />}
      {aQuitar && (
        <ModalDecision
          title="¿Quitar tramo vacío?"
          description={`El retorno de ${aQuitar.nombreEmbarcacion} desde ${aQuitar.zonaOrigen} dejará de mostrarse.`}
          onClose={() => { if (!quitando) setAQuitar(null); }}
          closeDisabled={quitando}
        >
          <div className="mt-6 flex justify-end gap-3">
            <button type="button" onClick={() => setAQuitar(null)} disabled={quitando} className="rounded-full border border-border-secondary px-5 py-2.5 text-sm font-semibold text-text-primary disabled:opacity-60">Cancelar</button>
            <button type="button" onClick={() => void quitar()} disabled={quitando} className="rounded-full bg-bg-fill-primary px-5 py-2.5 text-sm font-semibold text-text-invert disabled:opacity-60">{quitando ? "Quitando…" : "Quitar"}</button>
          </div>
        </ModalDecision>
      )}
    </>
  );
}
