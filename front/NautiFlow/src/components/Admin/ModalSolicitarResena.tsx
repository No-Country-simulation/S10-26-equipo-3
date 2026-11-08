import { useEffect, useRef, useState } from "react";
import { BsCheck2, BsCopy, BsSend, BsXLg } from "react-icons/bs";
import type { ViajeFinalizado } from "../../services/viajeAdminService";

type Props = {
  viajeFinalizado: ViajeFinalizado;
  onClose: () => void;
};

export default function ModalSolicitarResena({ viajeFinalizado, onClose }: Props) {
  const [copiado, setCopiado] = useState(false);
  const closeButtonRef = useRef<HTMLButtonElement>(null);

  const urlResena =
    typeof window !== "undefined"
      ? `${window.location.origin}/resena/${viajeFinalizado.tokenResena}`
      : `/resena/${viajeFinalizado.tokenResena}`;

  const mensajeCompleto = viajeFinalizado.mensajeResena
    ? `${viajeFinalizado.mensajeResena} ${urlResena}`.trim()
    : `Hola ${viajeFinalizado.nombreUsuario || ""}, nos gustaría conocer tu experiencia. Puedes dejarnos tu reseña aquí: ${urlResena}`.trim();

  const telefonoLimpio = (viajeFinalizado.whatsappUsuario ?? "").replace(/\D/g, "");
  const urlWhatsApp = telefonoLimpio
    ? `https://wa.me/${telefonoLimpio}?text=${encodeURIComponent(mensajeCompleto)}`
    : `https://wa.me/?text=${encodeURIComponent(mensajeCompleto)}`;

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
    };

    window.addEventListener("keydown", handleKeyDown);
    closeButtonRef.current?.focus();
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

  const handleCopiar = async () => {
    try {
      if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(urlResena);
      } else {
        const textarea = document.createElement("textarea");
        textarea.value = urlResena;
        textarea.style.position = "fixed";
        textarea.style.opacity = "0";
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand("copy");
        document.body.removeChild(textarea);
      }
      setCopiado(true);
      setTimeout(() => setCopiado(false), 2000);
    } catch {
      setCopiado(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 font-inter">
      <button
        type="button"
        aria-label="Cerrar modal"
        onClick={onClose}
        className="absolute inset-0 cursor-default"
      />
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-solicitar-resena-title"
        className="relative w-full max-w-[460px] rounded-xl border border-border-secondary bg-white p-5 sm:p-6 shadow-xl flex flex-col gap-4 text-text-primary"
      >
        {/* Header */}
        <div className="flex items-center justify-between w-full">
          <h2
            id="modal-solicitar-resena-title"
            className="text-xl font-semibold leading-6 text-text-primary"
          >
            Solicita una reseña
          </h2>
          <button
            ref={closeButtonRef}
            type="button"
            onClick={onClose}
            aria-label="Cerrar"
            className="grid h-8 w-8 place-items-center rounded-lg text-text-secondary hover:bg-bg-surface-secondary hover:text-text-primary transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary cursor-pointer"
          >
            <BsXLg className="h-4 w-4" aria-hidden="true" />
          </button>
        </div>

        {/* Body */}
        <div className="flex flex-col gap-2.5 w-full">
          <p className="text-sm font-normal leading-5 text-text-secondary">
            Copia o comparte este enlace antes de cerrar este mensaje. Después, no podrás volver a acceder a él.
          </p>

          <div className="flex items-center gap-2 w-full mt-1">
            {/* Input con link y botón de copiar */}
            <div className="flex flex-1 items-center justify-between gap-2 rounded-lg border border-border-secondary bg-white px-3 py-2 min-h-[40px] overflow-hidden">
              <span
                className="truncate text-sm text-text-primary font-normal select-all"
                title={urlResena}
              >
                {urlResena}
              </span>
              <button
                type="button"
                onClick={handleCopiar}
                aria-label={copiado ? "Enlace copiado" : "Copiar enlace"}
                title={copiado ? "Enlace copiado" : "Copiar enlace"}
                className="flex items-center justify-center p-1 rounded-md text-text-secondary hover:bg-bg-surface-secondary hover:text-text-primary transition-colors cursor-pointer shrink-0"
              >
                {copiado ? (
                  <span className="flex items-center gap-1 text-xs font-medium text-emerald-600">
                    <BsCheck2 className="size-4" aria-hidden="true" />
                    <span>Copiado</span>
                  </span>
                ) : (
                  <BsCopy className="size-4" aria-hidden="true" />
                )}
              </button>
            </div>

            {/* Botón Enviar / WhatsApp */}
            <a
              href={urlWhatsApp}
              target="_blank"
              rel="noopener noreferrer"
              aria-label="Enviar por WhatsApp"
              title="Compartir enlace por WhatsApp"
              className="size-10 rounded-full bg-bg-fill-primary text-text-invert flex items-center justify-center hover:bg-bg-fill-primary-hover transition-colors shadow-sm shrink-0 cursor-pointer"
            >
              <BsSend className="size-4 -translate-x-0.5" aria-hidden="true" />
            </a>
          </div>
        </div>

        {/* Footer */}
        <div className="flex justify-end w-full pt-1">
          <button
            type="button"
            onClick={onClose}
            className="rounded-full bg-bg-fill-primary px-6 py-2.5 text-sm font-semibold text-text-invert hover:bg-bg-fill-primary-hover transition-colors cursor-pointer"
          >
            Hecho
          </button>
        </div>
      </section>
    </div>
  );
}

