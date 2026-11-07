import { useEffect, useRef, useState, type ReactNode } from "react";
import { FiAlertCircle, FiCheckCircle, FiEye } from "react-icons/fi";
import type {
  DocumentoSolicitud,
  SolicitudDetalle as SolicitudDetalleData,
} from "../../services/solicitudAdminService";
import EmbarcacionGalleryModal from "./EmbarcacionGalleryModal";
import DocumentoViewerModal from "./DocumentoViewerModal";
import "./SolicitudDetalle.css";

type Props = {
  id: number;
  estado: string | null;
  detalle: SolicitudDetalleData | null;
  loading: boolean;
  error: string | null;
  onBack: () => void;
  onRetry: () => void;
  onAprobar: () => void;
  onRechazar: () => void;
  focusResultVersion: number;
};

const nombresDocumento: Record<string, string> = {
  LICENCIA: "Licencia",
  SEGURO: "Seguro",
  ANTECEDENTES: "Antecedentes penales",
};

function nombreDocumento(documento: DocumentoSolicitud): string {
  return nombresDocumento[documento.tipo] ??
    documento.tipo.toLowerCase().replaceAll("_", " ");
}

function esImagenDocumento(url: string): boolean {
  try {
    return /\.(png|jpe?g|webp)$/i.test(new URL(url).pathname);
  } catch {
    return false;
  }
}

const estilosEstado: Record<string, string> = {
  PENDIENTE: "solicitud-detalle__status--pending",
  APROBADO: "solicitud-detalle__status--approved",
  RECHAZADO: "solicitud-detalle__status--rejected",
};

const etiquetasEstado: Record<string, string> = {
  PENDIENTE: "Pendiente",
  APROBADO: "Aprobado",
  RECHAZADO: "Rechazado",
};

function Campo({
  etiqueta,
  children,
}: {
  etiqueta: string;
  children: ReactNode;
}) {
  return (
    <div className="solicitud-detalle__field">
      <span className="solicitud-detalle__field-label">{etiqueta}</span>
      <div className="solicitud-detalle__field-value">{children}</div>
    </div>
  );
}

export default function SolicitudDetalle({
  id,
  estado,
  detalle,
  loading,
  error,
  onBack,
  onRetry,
  onAprobar,
  onRechazar,
  focusResultVersion,
}: Props) {
  const fotos = detalle?.documentos.filter((documento) => documento.tipo === "FOTO_LANCHA") ?? [];
  const documentos = detalle?.documentos.filter((documento) => documento.tipo !== "FOTO_LANCHA") ?? [];
  const [documentoSeleccionado, setDocumentoSeleccionado] = useState<DocumentoSolicitud | null>(null);
  const [indiceFotoSeleccionada, setIndiceFotoSeleccionada] = useState<number | null>(null);
  const resultadoRef = useRef<HTMLElement>(null);
  const ultimaVersionEnfocada = useRef(focusResultVersion);

  useEffect(() => {
    if (ultimaVersionEnfocada.current === focusResultVersion) return;
    ultimaVersionEnfocada.current = focusResultVersion;
    if (detalle?.estado !== "APROBADO" && detalle?.estado !== "RECHAZADO") return;

    resultadoRef.current?.focus({ preventScroll: true });
    resultadoRef.current?.scrollIntoView({ behavior: "smooth", block: "center" });
  }, [detalle, focusResultVersion]);

  const estadoResultado = detalle?.estado;
  const motivoRechazo = detalle?.motivoRechazo?.trim();

  return (
    <main className={`solicitud-detalle${estadoResultado === "APROBADO" || estadoResultado === "RECHAZADO" ? " solicitud-detalle--has-result" : ""}`}>
      <header className="solicitud-detalle__header">
        <button
          type="button"
          onClick={onBack}
          aria-label="Volver a solicitudes"
          className="solicitud-detalle__back"
        >
          <span aria-hidden="true">‹</span>
        </button>
        <h1 className="solicitud-detalle__title">Solicitud Nro. {id}</h1>
        {estado && (
          <span
            className={`solicitud-detalle__status ${
              estilosEstado[estado] ?? "solicitud-detalle__status--unknown"
            }`}
          >
            {etiquetasEstado[estado] ?? estado}
          </span>
        )}
      </header>

      {estadoResultado === "APROBADO" && (
        <aside
          ref={resultadoRef}
          className="solicitud-detalle__result solicitud-detalle__result--approved"
          role="status"
          aria-live="polite"
          tabIndex={-1}
        >
          <FiCheckCircle aria-hidden="true" className="solicitud-detalle__result-icon" />
          <div className="solicitud-detalle__result-content">
            <h2 className="solicitud-detalle__result-title">Solicitud aprobada</h2>
            <p className="solicitud-detalle__result-description">
              Esta solicitud ha sido aprobada correctamente. La información y documentación del lanchero han sido validadas.
            </p>
          </div>
        </aside>
      )}
      {estadoResultado === "RECHAZADO" && (
        <aside
          ref={resultadoRef}
          className="solicitud-detalle__result solicitud-detalle__result--rejected"
          role="status"
          aria-live="polite"
          tabIndex={-1}
        >
          <FiAlertCircle aria-hidden="true" className="solicitud-detalle__result-icon" />
          <div className="solicitud-detalle__result-content">
            <h2 className="solicitud-detalle__result-title">Solicitud rechazada</h2>
            <p className="solicitud-detalle__result-description">
              Esta solicitud ha sido rechazada debido a que la información o documentación presentada no cumple con los requisitos establecidos.
            </p>
            <div className="solicitud-detalle__rejection-reason">
              <h3 className="solicitud-detalle__reason-title">Motivo del rechazo:</h3>
              <p className="solicitud-detalle__reason-text">
                {motivoRechazo || "No se registró un motivo de rechazo."}
              </p>
            </div>
          </div>
        </aside>
      )}

      {loading ? (
        <div role="status" className="solicitud-detalle__state">
          <span className="solicitud-detalle__spinner" />
          Cargando detalle…
        </div>
      ) : error ? (
        <div className="solicitud-detalle__state solicitud-detalle__state--error">
          <p role="alert">{error}</p>
          <button type="button" onClick={onRetry} className="solicitud-detalle__retry">
            Reintentar
          </button>
        </div>
      ) : detalle ? (
        <>
          <section aria-labelledby="datos-lanchero" className="solicitud-detalle__section">
            <h2 id="datos-lanchero" className="solicitud-detalle__section-title">
              Datos lanchero
            </h2>
            <div className="solicitud-detalle__owner-grid">
              <Campo etiqueta="Nombre completo">{detalle.nombreCompleto || "—"}</Campo>
              <Campo etiqueta="WhatsApp">{detalle.whatsapp || "—"}</Campo>
              <Campo etiqueta="Correo electrónico">{detalle.correo || "—"}</Campo>
            </div>
          </section>

          <section aria-labelledby="datos-lancha" className="solicitud-detalle__section">
            <h2 id="datos-lancha" className="solicitud-detalle__section-title">
              Datos lancha
            </h2>
            {detalle.embarcacion ? (
              <div className="solicitud-detalle__boat-grid">
                <Campo etiqueta="Nombre de tu embarcación">
                  {detalle.embarcacion.nombre || "—"}
                </Campo>
                <Campo etiqueta="Capacidad">
                  {detalle.embarcacion.capacidadPasajeros ?? "—"}
                </Campo>
                <div className="solicitud-detalle__coverage">
                  <span className="solicitud-detalle__field-label">
                    Zonas de cobertura
                  </span>
                  <div className="solicitud-detalle__tags" aria-label="Zonas de cobertura">
                    {detalle.embarcacion.zonas.length > 0 ? (
                      detalle.embarcacion.zonas.map((zona) => (
                        <span key={zona} className="solicitud-detalle__tag">
                          {zona}
                        </span>
                      ))
                    ) : (
                      <span className="solicitud-detalle__empty">—</span>
                    )}
                  </div>
                </div>
              </div>
            ) : (
              <p className="solicitud-detalle__empty">
                No hay información de embarcación disponible.
              </p>
            )}

            <h3 className="solicitud-detalle__gallery-title">
              Imágenes de la embarcación ({fotos.length})
            </h3>
            {fotos.length > 0 ? (
              <div className="solicitud-detalle__gallery">
                {fotos.map((foto, index) => (
                  <button
                    type="button"
                    key={`${foto.url}-${index}`}
                    onClick={() => setIndiceFotoSeleccionada(index)}
                    aria-label={`Ver fotografía ${index + 1} de la embarcación`}
                    className="solicitud-detalle__photo-link"
                  >
                    <img
                      src={foto.url}
                      alt={`Embarcación, fotografía ${index + 1}`}
                      className="solicitud-detalle__photo"
                      loading="lazy"
                    />
                  </button>
                ))}
              </div>
            ) : (
              <p className="solicitud-detalle__empty">
                No hay imágenes de la embarcación disponibles.
              </p>
            )}
          </section>

          <section aria-labelledby="documentos-solicitud" className="solicitud-detalle__section">
            <h2 id="documentos-solicitud" className="solicitud-detalle__section-title">
              Documentos ({documentos.length})
            </h2>
            {documentos.length > 0 ? (
              <div className="solicitud-detalle__documents">
                {documentos.map((documento, index) => (
                  <article
                    key={`${documento.tipo}-${documento.url}-${index}`}
                    className="solicitud-detalle__document"
                  >
                    <h3 className="solicitud-detalle__document-title">
                      {nombreDocumento(documento)}
                    </h3>
                    <button
                      type="button"
                      onClick={() => setDocumentoSeleccionado(documento)}
                      aria-label={`Visualizar documento: ${nombreDocumento(documento)}`}
                      className="solicitud-detalle__document-link"
                    >
                      <FiEye aria-hidden="true" className="solicitud-detalle__document-eye" />
                      Visualizar
                    </button>
                  </article>
                ))}
              </div>
            ) : (
              <p className="solicitud-detalle__empty">
                No hay documentos disponibles para esta solicitud.
              </p>
            )}
          </section>

          {detalle.estado === "PENDIENTE" && (
            <footer className="solicitud-detalle__actions">
              <button
                type="button"
                onClick={onRechazar}
                className="solicitud-detalle__button solicitud-detalle__button--secondary"
              >
                Rechazar
              </button>
              <button
                type="button"
                onClick={onAprobar}
                className="solicitud-detalle__button solicitud-detalle__button--primary"
              >
                Aprobar solicitud
              </button>
            </footer>
          )}
        </>
      ) : (
        <p role="status" className="solicitud-detalle__state">
          No hay información disponible para esta solicitud.
        </p>
      )}
      {documentoSeleccionado && esImagenDocumento(documentoSeleccionado.url) && (
        <EmbarcacionGalleryModal
          photographs={[documentoSeleccionado]}
          initialIndex={0}
          viewerLabel={`Vista previa de ${nombreDocumento(documentoSeleccionado)}`}
          onClose={() => setDocumentoSeleccionado(null)}
        />
      )}
      {documentoSeleccionado && !esImagenDocumento(documentoSeleccionado.url) && (
        <DocumentoViewerModal
          isOpen
          onClose={() => setDocumentoSeleccionado(null)}
          documentUrl={documentoSeleccionado.url}
          documentTitle={nombreDocumento(documentoSeleccionado)}
        />
      )}
      {indiceFotoSeleccionada !== null && (
        <EmbarcacionGalleryModal
          photographs={fotos}
          initialIndex={indiceFotoSeleccionada}
          onClose={() => setIndiceFotoSeleccionada(null)}
        />
      )}
    </main>
  );
}
