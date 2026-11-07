import { useCallback, useEffect, useRef, useState, type PointerEvent, type WheelEvent } from "react";
import type { DocumentoSolicitud } from "../../services/solicitudAdminService";
import "./EmbarcacionGalleryModal.css";

type Props = {
  photographs: DocumentoSolicitud[];
  initialIndex: number;
  onClose: () => void;
  viewerLabel?: string;
};

type Size = {
  width: number;
  height: number;
};

const MIN_ZOOM = 0.5;
const MAX_ZOOM = 3;
const ZOOM_STEP = 0.25;

export default function EmbarcacionGalleryModal({
  photographs,
  initialIndex,
  onClose,
  viewerLabel = "Galería de fotografías de la embarcación",
}: Props) {
  const dialogRef = useRef<HTMLElement>(null);
  const closeButtonRef = useRef<HTMLButtonElement>(null);
  const viewportRef = useRef<HTMLDivElement>(null);
  const imageRef = useRef<HTMLImageElement>(null);
  const dragStartRef = useRef<{ x: number; y: number; scrollLeft: number; scrollTop: number } | null>(null);
  const [index, setIndex] = useState(() => Math.min(Math.max(initialIndex, 0), photographs.length - 1));
  const [zoom, setZoom] = useState(1);
  const [baseSize, setBaseSize] = useState<Size | null>(null);
  const [viewportSize, setViewportSize] = useState<Size>({ width: 0, height: 0 });
  const [loading, setLoading] = useState(true);
  const [failed, setFailed] = useState(false);
  const [fullscreen, setFullscreen] = useState(false);
  const [dragging, setDragging] = useState(false);
  const photograph = photographs[index];

  const fitImage = useCallback(() => {
    const image = imageRef.current;
    const viewport = viewportRef.current;
    if (!image?.naturalWidth || !image.naturalHeight || !viewport) return;

    const maxWidth = Math.max(1, viewport.clientWidth - 48);
    const maxHeight = Math.max(1, viewport.clientHeight - 48);
    const scale = Math.min(maxWidth / image.naturalWidth, maxHeight / image.naturalHeight, 1);
    setBaseSize({
      width: image.naturalWidth * scale,
      height: image.naturalHeight * scale,
    });
  }, []);

  const navigate = useCallback((direction: -1 | 1) => {
    if (photographs.length < 2) return;
    setIndex((current) => (current + direction + photographs.length) % photographs.length);
    setZoom(1);
    setBaseSize(null);
    setLoading(true);
    setFailed(false);
    viewportRef.current?.scrollTo({ left: 0, top: 0 });
  }, [photographs.length]);

  const closeGallery = useCallback(() => {
    if (document.fullscreenElement === dialogRef.current) {
      void document.exitFullscreen();
    }
    onClose();
  }, [onClose]);

  const toggleFullscreen = async () => {
    const dialog = dialogRef.current;
    if (!dialog) return;

    try {
      if (document.fullscreenElement === dialog) {
        await document.exitFullscreen();
      } else if (document.fullscreenElement) {
        await document.exitFullscreen();
        await dialog.requestFullscreen();
      } else {
        await dialog.requestFullscreen();
      }
    } catch {
      setFullscreen(document.fullscreenElement === dialog);
    }
  };

  const adjustToFit = () => {
    setZoom(1);
    viewportRef.current?.scrollTo({ left: 0, top: 0, behavior: "smooth" });
    fitImage();
  };

  const handleImageLoad = () => {
    setLoading(false);
    setFailed(false);
    fitImage();
  };

  const handleWheel = (event: WheelEvent<HTMLDivElement>) => {
    event.preventDefault();
    setZoom((current) => Math.min(
      MAX_ZOOM,
      Math.max(MIN_ZOOM, current + (event.deltaY < 0 ? ZOOM_STEP : -ZOOM_STEP)),
    ));
  };

  const handlePointerDown = (event: PointerEvent<HTMLDivElement>) => {
    if (zoom <= 1 || event.pointerType !== "mouse" || !viewportRef.current) return;
    dragStartRef.current = {
      x: event.clientX,
      y: event.clientY,
      scrollLeft: viewportRef.current.scrollLeft,
      scrollTop: viewportRef.current.scrollTop,
    };
    event.currentTarget.setPointerCapture(event.pointerId);
    setDragging(true);
  };

  const handlePointerMove = (event: PointerEvent<HTMLDivElement>) => {
    if (!dragStartRef.current || !viewportRef.current) return;
    viewportRef.current.scrollLeft = dragStartRef.current.scrollLeft - (event.clientX - dragStartRef.current.x);
    viewportRef.current.scrollTop = dragStartRef.current.scrollTop - (event.clientY - dragStartRef.current.y);
  };

  const stopDragging = () => {
    dragStartRef.current = null;
    setDragging(false);
  };

  useEffect(() => {
    const viewport = viewportRef.current;
    if (!viewport) return;

    const observer = new ResizeObserver(() => {
      setViewportSize({ width: viewport.clientWidth, height: viewport.clientHeight });
      fitImage();
    });
    observer.observe(viewport);
    return () => observer.disconnect();
  }, [fitImage]);

  useEffect(() => {
    const previousOverflow = document.body.style.overflow;
    const previousFocus = document.activeElement instanceof HTMLElement ? document.activeElement : null;
    document.body.style.overflow = "hidden";
    closeButtonRef.current?.focus();

    const handleKeyDown = (event: globalThis.KeyboardEvent) => {
      if (event.key === "Escape") {
        if (document.fullscreenElement === dialogRef.current) return;
        event.preventDefault();
        closeGallery();
      } else if (event.key === "ArrowLeft") {
        event.preventDefault();
        navigate(-1);
      } else if (event.key === "ArrowRight") {
        event.preventDefault();
        navigate(1);
      } else if (event.key === "Tab") {
        const dialog = dialogRef.current;
        const focusable = dialog?.querySelectorAll<HTMLElement>(
          'button:not([disabled]), [tabindex]:not([tabindex="-1"])',
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
      }
    };
    const handleFullscreenChange = () => {
      setFullscreen(document.fullscreenElement === dialogRef.current);
    };

    window.addEventListener("keydown", handleKeyDown);
    document.addEventListener("fullscreenchange", handleFullscreenChange);
    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener("keydown", handleKeyDown);
      document.removeEventListener("fullscreenchange", handleFullscreenChange);
      previousFocus?.focus();
    };
  }, [closeGallery, navigate]);

  if (!photograph) return null;

  const displayedSize = baseSize
    ? { width: baseSize.width * zoom, height: baseSize.height * zoom }
    : null;

  return (
    <div
      className="embarcacion-gallery__backdrop"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) closeGallery();
      }}
    >
      <section
        ref={dialogRef}
        className={`embarcacion-gallery__dialog${fullscreen ? " embarcacion-gallery__dialog--fullscreen" : ""}`}
        role="dialog"
        aria-modal="true"
        aria-label={viewerLabel}
        tabIndex={-1}
      >
        <button
          ref={closeButtonRef}
          type="button"
          className="embarcacion-gallery__close"
          aria-label="Cerrar galería"
          onClick={closeGallery}
        >
          <svg aria-hidden="true" viewBox="0 0 24 24" fill="none">
            <path d="m6 6 12 12M18 6 6 18" stroke="currentColor" strokeLinecap="round" strokeWidth="2" />
          </svg>
        </button>

        <div
          ref={viewportRef}
          className={`embarcacion-gallery__viewport${zoom > 1 ? " embarcacion-gallery__viewport--zoomed" : ""}${dragging ? " embarcacion-gallery__viewport--dragging" : ""}`}
          onPointerDown={handlePointerDown}
          onPointerMove={handlePointerMove}
          onPointerUp={stopDragging}
          onPointerCancel={stopDragging}
          onLostPointerCapture={stopDragging}
          onWheel={handleWheel}
        >
          <div
            className="embarcacion-gallery__stage"
            style={displayedSize ? {
              width: Math.max(displayedSize.width, viewportSize.width),
              height: Math.max(displayedSize.height, viewportSize.height),
            } : undefined}
          >
            {loading && (
              <div className="embarcacion-gallery__loading" role="status">
                <span className="embarcacion-gallery__spinner" />
                Cargando fotografía…
              </div>
            )}
            {failed ? (
              <p className="embarcacion-gallery__error" role="alert">
                No se pudo cargar esta fotografía.
              </p>
            ) : (
              <img
                key={photograph.url}
                ref={imageRef}
                className={`embarcacion-gallery__image${loading ? " embarcacion-gallery__image--loading" : ""}`}
                src={photograph.url}
                alt={`Fotografía ${index + 1} de ${photographs.length} de la embarcación`}
                style={displayedSize ? {
                  width: displayedSize.width,
                  height: displayedSize.height,
                } : undefined}
                onLoad={handleImageLoad}
                onError={() => {
                  setLoading(false);
                  setFailed(true);
                }}
                draggable={false}
              />
            )}
          </div>
        </div>

        {photographs.length > 1 && (
          <>
            <button
              type="button"
              className="embarcacion-gallery__navigation embarcacion-gallery__navigation--previous"
              aria-label="Fotografía anterior"
              onClick={() => navigate(-1)}
            >
              <span aria-hidden="true">‹</span>
            </button>
            <button
              type="button"
              className="embarcacion-gallery__navigation embarcacion-gallery__navigation--next"
              aria-label="Fotografía siguiente"
              onClick={() => navigate(1)}
            >
              <span aria-hidden="true">›</span>
            </button>
          </>
        )}

        <div className="embarcacion-gallery__position" aria-live="polite">
          {index + 1} de {photographs.length}
        </div>

        <div className="embarcacion-gallery__controls" aria-label="Controles de visualización">
          <button
            type="button"
            aria-label="Reducir zoom"
            title="Reducir zoom"
            disabled={zoom <= MIN_ZOOM}
            onClick={() => setZoom((current) => Math.max(MIN_ZOOM, current - ZOOM_STEP))}
          >
            −
          </button>
          <span className="embarcacion-gallery__zoom-value" aria-live="polite">
            {Math.round(zoom * 100)}%
          </span>
          <button
            type="button"
            aria-label="Aumentar zoom"
            title="Aumentar zoom"
            disabled={zoom >= MAX_ZOOM}
            onClick={() => setZoom((current) => Math.min(MAX_ZOOM, current + ZOOM_STEP))}
          >
            +
          </button>
          <button
            type="button"
            aria-label="Ajustar imagen al visor"
            title="Ajustar imagen"
            onClick={adjustToFit}
          >
            <svg aria-hidden="true" viewBox="0 0 24 24" fill="none">
              <path d="M8 3H3v5m0-5 6 6m7-6h5v5m0-5-6 6M3 16v5h5m-5 0 6-6m7 6h5v-5m0 5-6-6" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" />
            </svg>
          </button>
          <button
            type="button"
            aria-label={fullscreen ? "Salir de pantalla completa" : "Pantalla completa"}
            title={fullscreen ? "Salir de pantalla completa" : "Pantalla completa"}
            onClick={() => void toggleFullscreen()}
          >
            <svg aria-hidden="true" viewBox="0 0 24 24" fill="none">
              {fullscreen ? (
                <path d="M9 4v5H4m11-5v5h5M9 20v-5H4m11 5v-5h5" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" />
              ) : (
                <path d="M4 9V4h5m6 0h5v5M4 15v5h5m6 0h5v-5" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" />
              )}
            </svg>
          </button>
        </div>
      </section>
    </div>
  );
}
