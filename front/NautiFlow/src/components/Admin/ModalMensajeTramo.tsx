import { useState } from "react";
import { BsCheck2, BsCopy } from "react-icons/bs";
import ModalDecision from "./ModalDecision";
import type { TramoVacio } from "../../services/tramoVacioService";

type Props = {
  tramo: TramoVacio;
  titulo?: string;
  onClose: () => void;
};

async function copiarTexto(texto: string) {
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(texto);
    return;
  }
  const textarea = document.createElement("textarea");
  textarea.value = texto;
  textarea.style.position = "fixed";
  textarea.style.opacity = "0";
  document.body.appendChild(textarea);
  textarea.select();
  document.execCommand("copy");
  document.body.removeChild(textarea);
}

export default function ModalMensajeTramo({ tramo, titulo = "Mensaje del tramo vacío", onClose }: Props) {
  const [copiado, setCopiado] = useState(false);

  const copiar = async () => {
    try {
      await copiarTexto(tramo.mensaje);
      setCopiado(true);
      setTimeout(() => setCopiado(false), 2000);
    } catch {
      setCopiado(false);
    }
  };

  return (
    <ModalDecision
      title={titulo}
      description="Copia el mensaje y compártelo con la flota para ofrecer el trayecto."
      onClose={onClose}
    >
      <p className="mt-4 whitespace-pre-wrap rounded-lg border border-border-secondary bg-bg-surface-primary p-3 text-sm leading-5 text-text-primary">
        {tramo.mensaje}
      </p>
      <div className="mt-6 flex justify-end gap-3">
        <button
          type="button"
          onClick={() => void copiar()}
          className="inline-flex items-center gap-2 rounded-full border border-border-secondary px-5 py-2.5 text-sm font-semibold text-text-primary transition-colors hover:bg-bg-surface-secondary"
        >
          {copiado ? <BsCheck2 className="h-4 w-4 text-emerald-600" aria-hidden="true" /> : <BsCopy className="h-4 w-4" aria-hidden="true" />}
          {copiado ? "Copiado" : "Copiar"}
        </button>
        <button
          type="button"
          onClick={onClose}
          className="rounded-full bg-bg-fill-primary px-5 py-2.5 text-sm font-semibold text-text-invert transition-colors hover:bg-bg-fill-primary-hover"
        >
          Hecho
        </button>
      </div>
    </ModalDecision>
  );
}
