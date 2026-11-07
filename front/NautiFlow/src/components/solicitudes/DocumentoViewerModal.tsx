import { useCallback, useEffect, useRef, useState, type PointerEvent, type SyntheticEvent, type WheelEvent } from "react";
import "./DocumentoViewerModal.css";

type Props = {
  isOpen: boolean;
  onClose: () => void;
  documentUrl: string;
  documentTitle: string;
  fileType?: string;
};

type PreviewKind = "pdf" | "image" | "unsupported";

const TIPOS_PREVISUALIZABLES: Record<string, PreviewKind> = {
  "application/pdf": "pdf",
  "image/png": "image",
  "image/jpeg": "image",
  "image/jpg": "image",
  "image/webp": "image",
};

function getPreviewKind(...values: Array<string | null | undefined>): PreviewKind {
  for (const value of values) {
    const mime = value?.split(";")[0].trim().toLowerCase();
    if (mime && TIPOS_PREVISUALIZABLES[mime]) return TIPOS_PREVISUALIZABLES[mime];
  }
  return "unsupported";
}

function getUrlMime(url: string): string | null {
  try {
    const extension = new URL(url).pathname.match(/\.([a-z0-9]+)$/i)?.[1]?.toLowerCase();
    if (extension === "pdf") return "application/pdf";
    if (extension === "png") return "image/png";
    if (extension === "jpg" || extension === "jpeg") return "image/jpeg";
    if (extension === "webp") return "image/webp";
    return null;
  } catch {
    return null;
  }
}

export default function DocumentoViewerModal({
  isOpen,
  onClose,
  documentUrl,
  documentTitle,
  fileType,
}: Props) {
  const closeButtonRef = useRef<HTMLButtonElement>(null);
  const viewerRef = useRef<HTMLDivElement>(null);
  const imageRef = useRef<HTMLImageElement>(null);
  const dragStartRef = useRef<{ x: number; y: number; scrollLeft: number; scrollTop: number } | null>(null);
  const [loadedUrl, setLoadedUrl] = useState<string | null>(null);
  const [failedUrl, setFailedUrl] = useState<string | null>(null);
  const [zoomState, setZoomState] = useState<{ url: string; scale: number }>({ url: "", scale: 1 });
  const [imageSize, setImageSize] = useState<{ width: number; height: number } | null>(null);
  const [dragging, setDragging] = useState(false);
  const previewKind = getPreviewKind(fileType, getUrlMime(documentUrl));
  const zoom = zoomState.url === documentUrl ? zoomState.scale : 1;

  const ajustarImagen = useCallback(() => {
    const viewer = viewerRef.current;
    const image = imageRef.current;
    if (!viewer || !image || !image.naturalWidth || !image.naturalHeight) return;

    const availableWidth = Math.max(1, viewer.clientWidth - 40);
    const availableHeight = Math.max(1, viewer.clientHeight - 40);
    const scaleToFit = Math.min(
      availableWidth / image.naturalWidth,
      availableHeight / image.naturalHeight,
      1,
    );
    setImageSize({
      width: image.naturalWidth * scaleToFit,
      height: image.naturalHeight * scaleToFit,
    });
  }, []);

  useEffect(() => {
    if (!isOpen) return;

    const previousOverflow = document.body.style.overflow;
    const activeElement = document.activeElement instanceof HTMLElement
      ? document.activeElement
      : null;
    document.body.style.overflow = "hidden";
    closeButtonRef.current?.focus();

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        event.preventDefault();
        onClose();
        return;
      }

      if (event.key !== "Tab") return;
      const dialog = document.querySelector<HTMLElement>(".documento-viewer__dialog");
      const focusable = dialog?.querySelectorAll<HTMLElement>(
        'button:not([disabled]), a[href], iframe, [tabindex]:not([tabindex="-1"])',
      );
      if (!focusable?.length) return;
      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener("keydown", handleKeyDown);
      activeElement?.focus();
    };
  }, [isOpen, onClose]);

  useEffect(() => {
    if (!isOpen || previewKind !== "image") return;
    window.addEventListener("resize", ajustarImagen);
    return () => window.removeEventListener("resize", ajustarImagen);
  }, [isOpen, documentUrl, previewKind, ajustarImagen]);

  if (!isOpen) return null;

  const loading = previewKind !== "unsupported" &&
    loadedUrl !== documentUrl &&
    failedUrl !== documentUrl;
  const documentReady = loadedUrl === documentUrl;

  const handleLoad = (event: SyntheticEvent<HTMLImageElement>) => {
    if (event.currentTarget instanceof HTMLImageElement) ajustarImagen();
    setLoadedUrl(documentUrl);
  };

  const handleImageWheel = (event: WheelEvent<HTMLDivElement>) => {
    event.preventDefault();
    const currentZoom = zoomState.url === documentUrl ? zoomState.scale : 1;
    const nextZoom = Math.min(3, Math.max(0.5, currentZoom + (event.deltaY < 0 ? 0.25 : -0.25)));
    setZoomState({ url: documentUrl, scale: nextZoom });
  };

  const comenzarArrastre = (event: PointerEvent<HTMLDivElement>) => {
    if (zoom <= 1 || event.pointerType !== "mouse" || !viewerRef.current) return;
    dragStartRef.current = {
      x: event.clientX,
      y: event.clientY,
      scrollLeft: viewerRef.current.scrollLeft,
      scrollTop: viewerRef.current.scrollTop,
    };
    event.currentTarget.setPointerCapture(event.pointerId);
    setDragging(true);
  };

  const moverArrastre = (event: PointerEvent<HTMLDivElement>) => {
    if (!dragStartRef.current || !viewerRef.current) return;
    viewerRef.current.scrollLeft = dragStartRef.current.scrollLeft - (event.clientX - dragStartRef.current.x);
    viewerRef.current.scrollTop = dragStartRef.current.scrollTop - (event.clientY - dragStartRef.current.y);
  };

  const terminarArrastre = () => {
    dragStartRef.current = null;
    setDragging(false);
  };

  return (
    <div
      className="documento-viewer__backdrop"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose();
      }}
    >
      <section
        className="documento-viewer__dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="documento-viewer-title"
      >
        <header className="documento-viewer__header">
          <h2 id="documento-viewer-title" className="documento-viewer__title">
            {documentTitle}
          </h2>
          <button
            ref={closeButtonRef}
            type="button"
            onClick={onClose}
            aria-label="Cerrar visor de documento"
            className="documento-viewer__close"
          >
            <svg aria-hidden="true" viewBox="0 0 24 24" fill="none">
              <path
                d="m6 6 12 12M18 6 6 18"
                stroke="currentColor"
                strokeLinecap="round"
                strokeWidth="2"
              />
            </svg>
          </button>
        </header>

        <div
          ref={viewerRef}
          className={`documento-viewer__content${zoom > 1 ? " documento-viewer__content--zoomed" : ""}${dragging ? " documento-viewer__content--dragging" : ""}`}
          onWheel={previewKind === "image" ? handleImageWheel : undefined}
          onPointerDown={previewKind === "image" ? comenzarArrastre : undefined}
          onPointerMove={previewKind === "image" ? moverArrastre : undefined}
          onPointerUp={previewKind === "image" ? terminarArrastre : undefined}
          onPointerCancel={previewKind === "image" ? terminarArrastre : undefined}
          onLostPointerCapture={previewKind === "image" ? terminarArrastre : undefined}
        >
          {loading && (
            <div className="documento-viewer__message" role="status">
              <span className="documento-viewer__spinner" />
              Cargando documento…
            </div>
          )}
          {failedUrl === documentUrl && (
            <p className="documento-viewer__message documento-viewer__message--error" role="alert">
              No se pudo cargar el documento desde su URL protegida.
            </p>
          )}
          {previewKind === "pdf" && (
            <iframe
              className={`documento-viewer__pdf${documentReady ? " documento-viewer__pdf--ready" : ""}`}
              src={documentUrl}
              title={documentTitle}
              onLoad={() => setLoadedUrl(documentUrl)}
              onError={() => setFailedUrl(documentUrl)}
            />
          )}
          {previewKind === "image" && (
            <div
              className={`documento-viewer__image-stage${documentReady ? " documento-viewer__image-stage--ready" : ""}`}
              style={imageSize ? {
                width: imageSize.width * zoom,
                height: imageSize.height * zoom,
              } : undefined}
            >
              <img
                ref={imageRef}
                className="documento-viewer__image"
                src={documentUrl}
                alt={documentTitle}
                onLoad={handleLoad}
                onError={() => setFailedUrl(documentUrl)}
              />
            </div>
          )}
          {previewKind === "unsupported" && (
            <p className="documento-viewer__message">
              No es posible previsualizar este formato.
            </p>
          )}
        </div>
      </section>
    </div>
  );
}
