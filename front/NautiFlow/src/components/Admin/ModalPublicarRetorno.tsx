import { useState, type FormEvent } from "react";
import ModalDecision from "./ModalDecision";
import { publicarRetornoVacio, type TramoVacio } from "../../services/tramoVacioService";
import type { ViajeSeguimiento } from "../../services/adminDashboardService";

type Props = {
  token: string;
  viaje: ViajeSeguimiento;
  onClose: () => void;
  onPublicado: (tramo: TramoVacio) => void;
};

export default function ModalPublicarRetorno({ token, viaje, onClose, onPublicado }: Props) {
  const [hora, setHora] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const publicar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!hora) {
      setError("Indica la hora de retorno.");
      return;
    }
    setEnviando(true);
    setError(null);
    try {
      onPublicado(await publicarRetornoVacio(token, viaje.id, hora));
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No se pudo publicar el retorno.");
    } finally {
      setEnviando(false);
    }
  };

  return (
    <ModalDecision
      title="Publicar retorno vacío"
      description={`La lancha ${viaje.nombreEmbarcacion} vuelve vacía. Indica a qué hora sale el retorno.`}
      onClose={onClose}
      closeDisabled={enviando}
    >
      <form className="mt-5 space-y-4" onSubmit={(event) => void publicar(event)} noValidate>
        <div>
          <label htmlFor="retorno-zona-origen" className="block text-sm font-semibold text-text-primary">Zona de origen</label>
          <input
            id="retorno-zona-origen"
            type="text"
            value={viaje.zonaDestino}
            readOnly
            className="mt-1.5 w-full rounded-lg border border-border-secondary bg-bg-surface-primary px-3 py-2.5 text-sm text-text-secondary"
          />
        </div>
        <div>
          <label htmlFor="retorno-hora" className="block text-sm font-semibold text-text-primary">Hora de retorno</label>
          <input
            id="retorno-hora"
            type="time"
            value={hora}
            onChange={(event) => setHora(event.target.value)}
            required
            aria-invalid={error !== null && !hora}
            aria-describedby={error ? "retorno-error" : undefined}
            className="mt-1.5 w-full rounded-lg border border-border-secondary px-3 py-2.5 text-sm text-text-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary"
          />
        </div>
        {error && <p id="retorno-error" role="alert" className="text-sm text-text-error">{error}</p>}
        <div className="flex justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={onClose}
            disabled={enviando}
            className="rounded-full border border-border-secondary px-5 py-2.5 text-sm font-semibold text-text-primary disabled:opacity-60"
          >
            Cancelar
          </button>
          <button
            type="submit"
            disabled={enviando}
            className="rounded-full bg-bg-fill-primary px-5 py-2.5 text-sm font-semibold text-text-invert disabled:opacity-60"
          >
            {enviando ? "Publicando…" : "Publicar"}
          </button>
        </div>
      </form>
    </ModalDecision>
  );
}
