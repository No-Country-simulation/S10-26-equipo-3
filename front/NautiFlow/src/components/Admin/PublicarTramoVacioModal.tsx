import { useState, type FormEvent } from "react";
import { BsArrowLeftRight } from "react-icons/bs";
import ModalDecision from "./ModalDecision";
import {
  publicarTramoVacio,
  type TramoVacio,
} from "../../services/tramoVacioAdminService";

type Props = {
  token: string;
  viajeId: number;
  lanchaNombre: string;
  zonaOrigen: string;
  zonaDestino: string;
  onClose: () => void;
  onPublished: (tramo: TramoVacio) => void;
  onError: (message: string) => void;
};

const inputClass = "mt-1.5 min-h-11 w-full rounded-lg border border-border-secondary bg-white px-3 text-sm text-text-primary outline-none focus:border-border-info focus:ring-2 focus:ring-border-info/20";

export default function PublicarTramoVacioModal({
  token,
  viajeId,
  lanchaNombre,
  zonaOrigen,
  zonaDestino,
  onClose,
  onPublished,
  onError,
}: Props) {
  const [horaRetorno, setHoraRetorno] = useState("");
  const [turistaNombreCompleto, setTuristaNombreCompleto] = useState("");
  const [turistaWhatsapp, setTuristaWhatsapp] = useState("");
  const [enviando, setEnviando] = useState(false);

  const publicar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (enviando) return;
    setEnviando(true);
    try {
      const tramo = await publicarTramoVacio(token, viajeId, {
        horaRetorno,
        ...(turistaNombreCompleto.trim() && { turistaNombreCompleto: turistaNombreCompleto.trim() }),
        ...(turistaWhatsapp.trim() && { turistaWhatsapp: turistaWhatsapp.trim() }),
      });
      onPublished(tramo);
    } catch (cause) {
      onError(cause instanceof Error ? cause.message : "No se pudo publicar el retorno.");
    } finally {
      setEnviando(false);
    }
  };

  return (
    <ModalDecision
      title="Publicar retorno vacío"
      description={`La lancha ${lanchaNombre} volverá desde ${zonaDestino} hacia ${zonaOrigen}.`}
      onClose={onClose}
      closeDisabled={enviando}
    >
      <form className="mt-5 space-y-4" onSubmit={(event) => void publicar(event)}>
        <div>
          <label htmlFor="hora-retorno" className="text-sm font-semibold text-text-primary">
            Hora de retorno
          </label>
          <input
            id="hora-retorno"
            type="time"
            required
            value={horaRetorno}
            onChange={(event) => setHoraRetorno(event.target.value)}
            className={inputClass}
          />
        </div>
        <div>
          <label htmlFor="turista-retorno" className="text-sm font-semibold text-text-primary">
            Turista interesado <span className="font-normal text-text-secondary">(opcional)</span>
          </label>
          <input
            id="turista-retorno"
            type="text"
            maxLength={201}
            autoComplete="name"
            value={turistaNombreCompleto}
            onChange={(event) => setTuristaNombreCompleto(event.target.value)}
            placeholder="Nombre completo"
            className={inputClass}
          />
        </div>
        <div>
          <label htmlFor="whatsapp-retorno" className="text-sm font-semibold text-text-primary">
            WhatsApp <span className="font-normal text-text-secondary">(opcional)</span>
          </label>
          <input
            id="whatsapp-retorno"
            type="tel"
            maxLength={20}
            autoComplete="tel"
            value={turistaWhatsapp}
            onChange={(event) => setTuristaWhatsapp(event.target.value)}
            placeholder="+549..."
            className={inputClass}
          />
        </div>
        <div className="flex justify-end gap-3 pt-1">
          <button
            type="button"
            onClick={onClose}
            disabled={enviando}
            className="min-h-10 rounded-full border border-border-secondary px-5 text-sm font-semibold text-text-primary transition-colors hover:bg-bg-surface-secondary disabled:opacity-60"
          >
            Cancelar
          </button>
          <button
            type="submit"
            disabled={enviando}
            className="inline-flex min-h-10 items-center gap-2 rounded-full bg-bg-fill-primary px-5 text-sm font-semibold text-text-invert transition-colors hover:bg-bg-fill-primary-hover disabled:cursor-not-allowed disabled:opacity-60"
          >
            <BsArrowLeftRight aria-hidden="true" />
            {enviando ? "Publicando…" : "Publicar retorno"}
          </button>
        </div>
      </form>
    </ModalDecision>
  );
}
