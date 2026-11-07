import { useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import Notification, { type NotificationKind } from "../../components/Admin/Notification";
import SolicitudesPaginacion from "../../components/solicitudes/SolicitudesPaginacion";
import {
  actualizarEstadoLanchero,
  listarLancheros,
  type EstadoSolicitudLanchero,
  type EstadoViajeEditable,
  type LancheroAdmin,
  type PaginaLancheros,
} from "../../services/lancheroAdminService";

type Props = { token: string };

type Aviso = {
  id: number;
  kind: NotificationKind;
  title: string;
  message: string;
};

const FILTROS: { value: EstadoSolicitudLanchero; label: string }[] = [
  { value: "TODOS", label: "Todos" },
  { value: "PENDIENTE", label: "Pendientes" },
  { value: "APROBADO", label: "Aprobados" },
  { value: "RECHAZADO", label: "Rechazados" },
];

function SelectorEstado({
  lanchero,
  actualizando,
  onChange,
}: {
  lanchero: LancheroAdmin;
  actualizando: boolean;
  onChange: (estado: EstadoViajeEditable) => void;
}) {
  const editable = lanchero.estadoSolicitud === "APROBADO" &&
    lanchero.estadoViaje !== "EN_VIAJE";
  const ayuda = lanchero.estadoViaje === "EN_VIAJE"
    ? "El estado cambia automáticamente cuando termina el viaje."
    : lanchero.estadoSolicitud !== "APROBADO"
      ? "Solo se puede cambiar el estado operativo de lancheros aprobados."
      : undefined;

  return (
    <div className="flex min-h-10 items-center gap-2">
      <select
        aria-label={`Estado de ${lanchero.nombreCompleto}`}
        aria-busy={actualizando}
        title={ayuda}
        disabled={!editable || actualizando}
        value={lanchero.estadoViaje}
        onChange={(event) => {
          const value = event.currentTarget.value;
          if (value === "ACTIVO" || value === "INACTIVO") onChange(value);
        }}
        className="min-h-10 min-w-32 cursor-pointer rounded-full border border-bg-fill-primary bg-bg-fill-primary px-2 py-1.5 text-xs font-semibold text-text-invert focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary disabled:cursor-not-allowed disabled:opacity-70"
      >
        <option value="ACTIVO">Activo</option>
        <option value="INACTIVO">Inactivo</option>
        <option value="EN_VIAJE" disabled>En viaje</option>
      </select>
      {actualizando && (
        <span
          className="h-4 w-4 shrink-0 animate-spin rounded-full border-2 border-bg-surface-tertiary border-t-bg-fill-primary"
          role="status"
          aria-label="Actualizando estado"
        />
      )}
    </div>
  );
}

function FichaLanchero({
  lanchero,
  actualizando,
  onChangeEstado,
}: {
  lanchero: LancheroAdmin;
  actualizando: boolean;
  onChangeEstado: (estado: EstadoViajeEditable) => void;
}) {
  return (
    <article className="rounded-[11px] border border-border-secondary bg-white p-3 sm:p-4">
      <div className="flex min-h-8 items-center justify-between gap-3">
        <p className="text-sm font-bold text-text-primary">Lanchero Nro. {lanchero.id}</p>
        <div className="w-auto min-w-0 shrink-0">
          <SelectorEstado
            lanchero={lanchero}
            actualizando={actualizando}
            onChange={onChangeEstado}
          />
        </div>
      </div>
      <div className="mt-2.5 border-t border-border-secondary pt-2.5">
        <p className="break-words text-[15px] font-semibold leading-5 text-text-primary">
          {lanchero.nombreCompleto}
        </p>
      </div>
      <dl className="mt-2.5 grid grid-cols-2 gap-2">
        <div>
          <dt className="text-xs font-medium text-text-secondary">WhatsApp</dt>
          <dd className="mt-0.5 break-all text-sm text-text-primary">{lanchero.whatsapp}</dd>
        </div>
        <div>
          <dt className="text-xs font-medium text-text-secondary">Lancha</dt>
          <dd className="mt-0.5 break-words text-sm text-text-primary">{lanchero.nombreLancha}</dd>
        </div>
      </dl>
    </article>
  );
}

export default function LancherosPage({ token }: Props) {
  const [searchParams, setSearchParams] = useSearchParams();
  const estadoParam = searchParams.get("estado");
  const estadoFiltro: EstadoSolicitudLanchero =
    estadoParam === "TODOS" ||
    estadoParam === "PENDIENTE" ||
    estadoParam === "APROBADO" ||
    estadoParam === "RECHAZADO"
      ? estadoParam
      : "APROBADO";
  const pageParam = Number(searchParams.get("page"));
  const page = Number.isInteger(pageParam) && pageParam >= 0 ? pageParam : 0;
  const [pagina, setPagina] = useState<PaginaLancheros | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadVersion, setReloadVersion] = useState(0);
  const [actualizandoIds, setActualizandoIds] = useState<Set<number>>(() => new Set());
  const actualizacionesEnCurso = useRef(new Set<number>());
  const siguienteAvisoId = useRef(0);
  const [aviso, setAviso] = useState<Aviso | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    void listarLancheros(token, estadoFiltro, page, 10, controller.signal)
      .then((resultado) => {
        if (controller.signal.aborted) return;
        setPagina(resultado);
        if (resultado.page !== page) {
          setSearchParams((current) => {
            const next = new URLSearchParams(current);
            next.set("estado", estadoFiltro);
            next.set("page", String(resultado.page));
            return next;
          });
        }
        setError(null);
      })
      .catch((cause: unknown) => {
        if (!controller.signal.aborted) {
          setError(cause instanceof Error
            ? cause.message
            : "No se pudieron cargar los lancheros.");
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });

    return () => controller.abort();
  }, [estadoFiltro, page, reloadVersion, setSearchParams, token]);

  const cambiarFiltro = (estado: EstadoSolicitudLanchero) => {
    if (estado === estadoFiltro) return;
    setSearchParams((current) => {
      const next = new URLSearchParams(current);
      next.set("estado", estado);
      next.set("page", "0");
      return next;
    });
    setPagina(null);
    setError(null);
    setLoading(true);
  };

  const cambiarPagina = (nuevaPagina: number) => {
    if (!pagina || nuevaPagina < 0 || nuevaPagina >= pagina.totalPages || nuevaPagina === page) return;
    setSearchParams((current) => {
      const next = new URLSearchParams(current);
      next.set("estado", estadoFiltro);
      next.set("page", String(nuevaPagina));
      return next;
    });
    setPagina(null);
    setError(null);
    setLoading(true);
  };

  const reintentar = () => {
    setError(null);
    setLoading(true);
    setReloadVersion((version) => version + 1);
  };

  const cambiarEstadoLanchero = async (
    lanchero: LancheroAdmin,
    nuevoEstado: EstadoViajeEditable,
  ) => {
    if (actualizacionesEnCurso.current.has(lanchero.id) ||
        lanchero.estadoViaje === nuevoEstado ||
        lanchero.estadoSolicitud !== "APROBADO" ||
        lanchero.estadoViaje === "EN_VIAJE") {
      return;
    }

    actualizacionesEnCurso.current.add(lanchero.id);
    setActualizandoIds((ids) => new Set(ids).add(lanchero.id));
    try {
      const actualizado = await actualizarEstadoLanchero(token, lanchero.id, nuevoEstado);
      setPagina((actual) => actual && ({
        ...actual,
        content: actual.content.map((item) => item.id === lanchero.id
          ? { ...item, estadoViaje: actualizado }
          : item),
      }));
      setAviso({
        id: siguienteAvisoId.current++,
        kind: "success",
        title: "Estado actualizado",
        message: `El estado de ${lanchero.nombreCompleto} se actualizó correctamente.`,
      });
    } catch (cause) {
      setAviso({
        id: siguienteAvisoId.current++,
        kind: "error",
        title: "No se pudo actualizar el estado",
        message: cause instanceof Error ? cause.message : "Inténtalo nuevamente.",
      });
    } finally {
      actualizacionesEnCurso.current.delete(lanchero.id);
      setActualizandoIds((ids) => {
        const siguientes = new Set(ids);
        siguientes.delete(lanchero.id);
        return siguientes;
      });
    }
  };

  return (
    <div className="w-full pt-4 font-inter text-text-primary">
      <div className="mx-auto w-[96%] max-w-[1400px]">
        <header className="mb-4">
          <h1 className="text-xl font-bold leading-6 text-text-primary sm:text-2xl">Lancheros</h1>
          <p className="mt-1 text-sm leading-5 text-text-secondary">
            Consulta los lancheros y administra su estado operativo.
          </p>
        </header>

        <div className="mb-3">
          <nav
            aria-label="Filtrar lancheros por estado de solicitud"
            className="flex max-w-full flex-nowrap gap-0.5 overflow-x-auto rounded-full bg-bg-surface-secondary p-1 lg:inline-flex lg:flex-wrap lg:overflow-visible [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
          >
            {FILTROS.map(({ value, label }) => (
              <button
                key={value}
                type="button"
                aria-pressed={estadoFiltro === value}
                onClick={() => cambiarFiltro(value)}
                className={`shrink-0 rounded-full px-2 py-1.5 text-xs font-semibold leading-5 transition-colors sm:px-3 sm:text-sm ${
                  estadoFiltro === value
                    ? "bg-white text-text-primary shadow-sm"
                    : "text-text-secondary hover:text-text-primary"
                }`}
              >
                {label}
              </button>
            ))}
          </nav>
        </div>

        <section aria-label="Listado de lancheros" className="overflow-visible rounded-none border-0 bg-transparent lg:overflow-hidden lg:rounded-[9px] lg:border lg:border-border-secondary lg:bg-white">
          {loading ? (
            <div role="status" className="flex items-center justify-center gap-3 rounded-[11px] border border-border-secondary bg-white px-4 py-8 text-sm text-text-secondary lg:rounded-none lg:border-0 lg:bg-transparent lg:px-6 lg:py-12">
              <span className="h-5 w-5 animate-spin rounded-full border-2 border-bg-fill-secondary border-t-bg-fill-primary" aria-hidden="true" />
              Cargando lancheros…
            </div>
          ) : error ? (
            <div className="rounded-[11px] border border-border-secondary bg-white px-4 py-7 text-center lg:rounded-none lg:border-0 lg:bg-transparent lg:px-6 lg:py-10">
              <p role="alert" className="text-sm text-text-error">{error}</p>
              <button
                type="button"
                onClick={reintentar}
                className="mt-4 rounded-full bg-bg-fill-primary px-5 py-2.5 text-sm font-semibold text-text-invert transition-colors hover:bg-bg-fill-primary-hover"
              >
                Reintentar
              </button>
            </div>
          ) : !pagina || pagina.content.length === 0 ? (
            <div className="flex min-h-[300px] flex-col items-center rounded-[11px] border border-border-secondary bg-white px-5 pb-6 pt-7 text-center sm:min-h-[540px] sm:pt-12 lg:min-h-[480px] lg:rounded-none lg:border-0 lg:bg-transparent lg:px-6 lg:pb-10 lg:pt-10 2xl:min-h-[540px] 2xl:pt-12">
              <h2 className="text-xl font-semibold text-text-primary">Sin lancheros</h2>
              <p className="mt-1 text-sm text-text-secondary">
                No hay resultados para el estado de solicitud seleccionado.
              </p>
              <img
                src="/solicitudes/sinresultados.png"
                alt="Ilustración de una lancha y una lupa indicando que no hay resultados"
                className="mt-5 h-auto w-full max-w-[560px] object-contain sm:mt-8 md:max-w-[760px] lg:max-w-[900px]"
              />
            </div>
          ) : (
            <>
              <div className="grid gap-3 lg:hidden" aria-label="Lancheros">
                {pagina.content.map((lanchero) => (
                  <FichaLanchero
                    key={lanchero.id}
                    lanchero={lanchero}
                    actualizando={actualizandoIds.has(lanchero.id)}
                    onChangeEstado={(estado) => void cambiarEstadoLanchero(lanchero, estado)}
                  />
                ))}
              </div>
              <div className="hidden overflow-x-auto lg:block">
                <table className="w-full min-w-[960px] table-fixed border-collapse text-left">
                  <colgroup>
                    <col className="w-[28%]" />
                    <col className="w-[24%]" />
                    <col className="w-[24%]" />
                    <col className="w-[24%]" />
                  </colgroup>
                  <thead className="text-sm font-semibold text-text-secondary">
                    <tr>
                      {["Lanchero", "WhatsApp", "Lancha", "Estado"].map((label) => (
                        <th key={label} scope="col" className="whitespace-nowrap border-b border-border-secondary px-3 py-2 leading-5">
                          {label}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {pagina.content.map((lanchero) => (
                      <tr key={lanchero.id} className="border-b border-border-secondary text-sm text-text-primary last:border-b-0 2xl:text-[15px]">
                        <td className="overflow-hidden text-ellipsis whitespace-nowrap px-3 py-2.5 font-medium" title={lanchero.nombreCompleto}>{lanchero.nombreCompleto}</td>
                        <td className="whitespace-nowrap px-3 py-2.5">{lanchero.whatsapp}</td>
                        <td className="overflow-hidden text-ellipsis whitespace-nowrap px-3 py-2.5" title={lanchero.nombreLancha}>{lanchero.nombreLancha}</td>
                        <td className="px-2 py-2.5">
                          <SelectorEstado
                            lanchero={lanchero}
                            actualizando={actualizandoIds.has(lanchero.id)}
                            onChange={(estado) => void cambiarEstadoLanchero(lanchero, estado)}
                          />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </section>

        {!loading && !error && pagina && pagina.content.length > 0 && (
          <SolicitudesPaginacion
            page={pagina.page}
            totalPages={pagina.totalPages}
            disabled={loading}
            onChange={cambiarPagina}
          />
        )}
      </div>
      {aviso && (
        <div className="pointer-events-none fixed right-4 top-4 z-50 flex w-full max-w-sm flex-col gap-2">
          <div className="pointer-events-auto">
            <Notification
              key={aviso.id}
              kind={aviso.kind}
              title={aviso.title}
              message={aviso.message}
              onClose={() => setAviso(null)}
            />
          </div>
        </div>
      )}
    </div>
  );
}
