import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import ModalAprobarSolicitud from "../../components/Admin/ModalAprobarSolicitud";
import ModalRechazarSolicitud from "../../components/Admin/ModalRechazarSolicitud";
import Notification, { type NotificationKind } from "../../components/Admin/Notification";
import SolicitudDetalle from "../../components/solicitudes/SolicitudDetalle";
import SolicitudesFiltros from "../../components/solicitudes/SolicitudesFiltros";
import SolicitudesHeader from "../../components/solicitudes/SolicitudesHeader";
import SolicitudesPaginacion from "../../components/solicitudes/SolicitudesPaginacion";
import SolicitudesTabla from "../../components/solicitudes/SolicitudesTabla";
import {
  aprobarSolicitud,
  listarSolicitudes,
  obtenerSolicitudDetalle,
  rechazarSolicitud,
  type FiltroEstadoSolicitud,
  type SolicitudDetalle as SolicitudDetalleData,
  type SolicitudResumen,
} from "../../services/solicitudAdminService";

type Props = {
  token: string;
};

type DecisionSeleccionada = {
  solicitudId: number;
  accion: "aprobar" | "rechazar";
} | null;

type Aviso = {
  id: number;
  kind: NotificationKind;
  title: string;
  message: string;
};

const ERROR_TITLE = "¡Uh oh! Algo salió mal";
const ERROR_MESSAGE = "No se pudo completar la acción. Inténtalo nuevamente.";

export default function SolicitudesPage({ token }: Props) {
  const navigate = useNavigate();
  const { id: routeId } = useParams<{ id: string }>();
  const [searchParams, setSearchParams] = useSearchParams();
  const estadoUrl = searchParams.get("estado");
  const estadoInicial: FiltroEstadoSolicitud = estadoUrl === "PENDIENTE" || estadoUrl === "APROBADO" || estadoUrl === "RECHAZADO" ? estadoUrl : "TODOS";
  const pageUrl = Number(searchParams.get("page"));
  const [estado, setEstado] = useState<FiltroEstadoSolicitud>(estadoInicial);
  const [page, setPage] = useState(Number.isInteger(pageUrl) && pageUrl >= 0 ? pageUrl : 0);
  const [solicitudes, setSolicitudes] = useState<SolicitudResumen[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadVersion, setReloadVersion] = useState(0);
  const [retryVersion, setRetryVersion] = useState(0);
  const solicitudSeleccionadaId = routeId && /^\d+$/.test(routeId) ? Number(routeId) : null;
  const [detalle, setDetalle] = useState<SolicitudDetalleData | null>(null);
  const [detalleLoading, setDetalleLoading] = useState(solicitudSeleccionadaId !== null);
  const [detalleError, setDetalleError] = useState<string | null>(null);
  const [detalleRetryVersion, setDetalleRetryVersion] = useState(0);
  const [focusResultVersion, setFocusResultVersion] = useState(0);
  const [seleccion, setSeleccion] = useState<DecisionSeleccionada>(null);
  const [enviandoDecision, setEnviandoDecision] = useState(false);
  const decisionEnCurso = useRef(false);
  const siguienteAvisoId = useRef(0);
  const [avisos, setAvisos] = useState<Aviso[]>([]);

  const mostrarAviso = (aviso: Omit<Aviso, "id">) => {
    const id = siguienteAvisoId.current++;
    setAvisos((actuales) => [...actuales, { ...aviso, id }]);
  };

  const cerrarAviso = (id: number) => {
    setAvisos((actuales) => actuales.filter((aviso) => aviso.id !== id));
  };

  useEffect(() => {
    const controller = new AbortController();
    void listarSolicitudes(token, page, estado, controller.signal)
      .then((resultado) => {
        setSolicitudes(resultado.content);
        setTotalPages(resultado.totalPages);
        setError(null);
      })
      .catch((cause: unknown) => {
        if (!controller.signal.aborted) {
          setError(cause instanceof Error
            ? cause.message
            : "No se pudieron cargar las solicitudes.");
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });

    return () => controller.abort();
  }, [estado, page, reloadVersion, retryVersion, token]);

  useEffect(() => {
    if (solicitudSeleccionadaId === null) return;
    const controller = new AbortController();
    void obtenerSolicitudDetalle(token, solicitudSeleccionadaId, controller.signal)
      .then((resultado) => {
        if (!controller.signal.aborted) {
          setDetalle(resultado);
          setDetalleError(null);
        }
      })
      .catch((cause: unknown) => {
        if (!controller.signal.aborted) {
          setDetalleError(cause instanceof Error
            ? cause.message
            : "No se pudo cargar el detalle de la solicitud.");
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setDetalleLoading(false);
      });

    return () => controller.abort();
  }, [solicitudSeleccionadaId, detalleRetryVersion, token]);

  const cambiarFiltro = (nuevoEstado: FiltroEstadoSolicitud) => {
    if (nuevoEstado === estado) return;
    setEstado(nuevoEstado);
    setPage(0);
    setSolicitudes([]);
    setTotalPages(0);
    setError(null);
    navigate(`/admin/solicitudes?estado=${nuevoEstado}&page=0`);
    setDetalle(null);
    setDetalleError(null);
    setLoading(true);
  };

  const cambiarPagina = (nuevaPagina: number) => {
    if (nuevaPagina < 0 || nuevaPagina >= totalPages || nuevaPagina === page) return;
    setPage(nuevaPagina);
    setSolicitudes([]);
    setError(null);
    setSearchParams({ estado, page: String(nuevaPagina) });
    setDetalle(null);
    setDetalleError(null);
    setLoading(true);
  };

  const reintentar = () => {
    setError(null);
    setLoading(true);
    setRetryVersion((version) => version + 1);
  };

  const prepararDetalle = useCallback((id: number) => {
    navigate(`/admin/solicitudes/${id}?estado=${estado}&page=${page}`);
    setDetalle(null);
    setDetalleError(null);
    setDetalleLoading(true);
  }, [estado, navigate, page]);

  const volverTabla = useCallback(() => {
    navigate(`/admin/solicitudes?estado=${estado}&page=${page}`);
    setDetalle(null);
    setDetalleError(null);
    setDetalleLoading(false);
  }, [estado, navigate, page]);

  const reintentarDetalle = useCallback(() => {
    setDetalleError(null);
    setDetalleLoading(true);
    setDetalleRetryVersion((version) => version + 1);
  }, []);

  const cerrarModal = useCallback(() => {
    if (!decisionEnCurso.current) setSeleccion(null);
  }, []);

  const completarDecision = async (motivo?: string) => {
    if (!seleccion || decisionEnCurso.current) return;
    const decision = seleccion;
    decisionEnCurso.current = true;
    setEnviandoDecision(true);

    try {
      if (decision.accion === "aprobar") {
        await aprobarSolicitud(token, decision.solicitudId);
      } else {
        await rechazarSolicitud(token, decision.solicitudId, motivo ?? "");
      }
      setSeleccion(null);

      mostrarAviso(decision.accion === "aprobar"
        ? {
            kind: "success",
            title: "¡Solicitud aprobada!",
            message: "La lancha ya está publicada y el lanchero será notificado.",
          }
        : {
            kind: "success",
            title: "¡Solicitud rechazada!",
            message: "El motivo del rechazo fue enviado al lanchero.",
          });

      if (solicitudSeleccionadaId === decision.solicitudId) {
        setDetalleLoading(true);
        setDetalleError(null);
        try {
          const detalleActualizado = await obtenerSolicitudDetalle(
            token,
            decision.solicitudId,
            new AbortController().signal,
          );
          setDetalle(detalleActualizado);
          setFocusResultVersion((version) => version + 1);
        } catch (cause) {
          setDetalleError(cause instanceof Error
            ? cause.message
            : "No se pudo actualizar el detalle de la solicitud.");
        } finally {
          setDetalleLoading(false);
        }
      }

      setLoading(true);
      setReloadVersion((version) => version + 1);
    } catch {
      mostrarAviso({
        kind: "error",
        title: ERROR_TITLE,
        message: ERROR_MESSAGE,
      });
    } finally {
      decisionEnCurso.current = false;
      setEnviandoDecision(false);
    }
  };

  return (
    <div className="w-full pt-6 font-inter text-text-primary">
      <div className={`mx-auto w-full ${solicitudSeleccionadaId === null ? "max-w-[1026px]" : "max-w-[1112px]"}`}>
        {solicitudSeleccionadaId === null ? (
          <>
            <SolicitudesHeader />
            <SolicitudesFiltros seleccionado={estado} onChange={cambiarFiltro} />

            <section className="overflow-hidden rounded-[9px] border border-border-secondary bg-white">
              {loading ? (
                <div role="status" className="flex items-center justify-center gap-3 px-6 py-12 text-sm text-text-secondary">
                  <span className="h-5 w-5 animate-spin rounded-full border-2 border-bg-fill-secondary border-t-bg-fill-primary" />
                  Cargando solicitudes…
                </div>
              ) : error ? (
                <div className="px-6 py-10 text-center">
                  <p role="alert" className="text-sm text-text-error">{error}</p>
                  <button
                    type="button"
                    onClick={reintentar}
                    className="mt-4 rounded-full bg-bg-fill-primary px-5 py-2.5 text-sm font-semibold text-text-invert hover:bg-bg-fill-primary-hover"
                  >
                    Reintentar
                  </button>
                </div>
              ) : solicitudes.length === 0 ? (
                estado === "PENDIENTE" ? (
                  <div className="flex min-h-[480px] flex-col items-center px-6 pb-10 pt-10 text-center sm:min-h-[540px] sm:pt-12">
                    <h2 className="text-xl font-semibold text-text-primary">Sin solicitudes</h2>
                    <p className="mt-2 text-sm text-text-secondary">
                      No hay solicitudes para mostrar
                    </p>
                    <img
                      src="/solicitudes/sinresultados.png"
                      alt="Ilustración de una lancha y una lupa indicando que no hay resultados"
                      className="mt-8 h-auto w-full max-w-[560px] object-contain md:max-w-[760px] lg:max-w-[900px]"
                    />
                  </div>
                ) : (
                  <p className="px-6 py-12 text-center text-sm text-text-secondary">
                    No hay solicitudes para el filtro seleccionado.
                  </p>
                )
              ) : (
                <SolicitudesTabla
                  solicitudes={solicitudes}
                  onVerDetalles={prepararDetalle}
                  onAprobar={(solicitud) => setSeleccion({ solicitudId: solicitud.id, accion: "aprobar" })}
                  onRechazar={(solicitud) => setSeleccion({ solicitudId: solicitud.id, accion: "rechazar" })}
                />
              )}
            </section>
            {!loading && !error && solicitudes.length > 0 && (
              <SolicitudesPaginacion
                page={page}
                totalPages={totalPages}
                disabled={loading}
                onChange={cambiarPagina}
              />
            )}
          </>
        ) : (
          <SolicitudDetalle
            id={solicitudSeleccionadaId}
            estado={detalle?.estado ?? solicitudes.find((solicitud) => solicitud.id === solicitudSeleccionadaId)?.estado ?? null}
            detalle={detalle}
            loading={detalleLoading}
            error={detalleError}
            onBack={volverTabla}
            onRetry={reintentarDetalle}
            onAprobar={() => setSeleccion({ solicitudId: solicitudSeleccionadaId, accion: "aprobar" })}
            onRechazar={() => setSeleccion({ solicitudId: solicitudSeleccionadaId, accion: "rechazar" })}
            focusResultVersion={focusResultVersion}
          />
        )}
      </div>
      {seleccion?.accion === "aprobar" && (
        <ModalAprobarSolicitud
          enviando={enviandoDecision}
          onCancel={cerrarModal}
          onConfirm={() => void completarDecision()}
        />
      )}
      {seleccion?.accion === "rechazar" && (
        <ModalRechazarSolicitud
          enviando={enviandoDecision}
          onCancel={cerrarModal}
          onConfirm={(motivo) => void completarDecision(motivo)}
        />
      )}
      <div className="pointer-events-none fixed right-4 top-4 z-[60] flex flex-col gap-3">
        {avisos.map((aviso) => (
          <Notification
            key={aviso.id}
            kind={aviso.kind}
            title={aviso.title}
            message={aviso.message}
            onClose={() => cerrarAviso(aviso.id)}
          />
        ))}
      </div>
    </div>
  );
}
