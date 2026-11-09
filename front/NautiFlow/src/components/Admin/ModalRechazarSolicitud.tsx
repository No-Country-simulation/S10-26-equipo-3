import { useState, type FormEvent } from "react";
import ModalDecision from "./ModalDecision";

const MAX_CARACTERES = 500;

type Props = {
  enviando: boolean;
  onCancel: () => void;
  onConfirm: (motivo: string) => void;
};

export default function ModalRechazarSolicitud({ enviando, onCancel, onConfirm }: Props) {
  const [motivo, setMotivo] = useState("");
  const motivoValido = motivo.trim().length > 0 && motivo.length <= MAX_CARACTERES;

  const manejarEnvio = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!motivoValido || enviando) return;
    onConfirm(motivo.trim());
  };

  return (
    <ModalDecision
      title="¿Rechazar solicitud?"
      description="Indica el motivo del rechazo para dejar constancia de tu decisión. Una vez rechazada, no podrás cambiar su estado."
      onClose={onCancel}
      closeDisabled={enviando}
    >
      <form onSubmit={manejarEnvio}>
        <label htmlFor="motivo-rechazo" className="mt-5 block text-sm font-semibold text-text-primary">
          Motivo del rechazo
        </label>
        <textarea
          id="motivo-rechazo"
          value={motivo}
          onChange={(event) => setMotivo(event.target.value)}
          placeholder="Escribe el motivo de tu rechazo aquí..."
          maxLength={MAX_CARACTERES}
          required
          disabled={enviando}
          rows={4}
          className="mt-2 w-full resize-y rounded-md border border-border-secondary bg-white px-3 py-2 text-sm leading-5 text-text-primary placeholder:text-text-disable focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-bg-fill-primary disabled:bg-bg-surface-secondary"
        />
        <p className="mt-1 text-right text-xs text-text-secondary">
          {motivo.length}/{MAX_CARACTERES}
        </p>
        <div className="mt-5 flex justify-end gap-3">
          <button
            type="button"
            onClick={onCancel}
            disabled={enviando}
            className="rounded-full border border-border-secondary bg-white px-5 py-2.5 text-sm font-semibold text-text-primary hover:bg-bg-surface-secondary disabled:cursor-not-allowed disabled:opacity-60"
          >
            Cancelar
          </button>
          <button
            type="submit"
            disabled={!motivoValido || enviando}
            className="rounded-full bg-bg-fill-primary px-5 py-2.5 text-sm font-semibold text-text-invert hover:bg-bg-fill-primary-hover disabled:cursor-not-allowed disabled:opacity-60"
          >
            {enviando ? "Rechazando…" : "Rechazar"}
          </button>
        </div>
      </form>
    </ModalDecision>
  );
}
