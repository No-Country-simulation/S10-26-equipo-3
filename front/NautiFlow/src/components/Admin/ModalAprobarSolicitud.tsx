import ModalDecision from "./ModalDecision";

type Props = {
  enviando: boolean;
  onCancel: () => void;
  onConfirm: () => void;
};

export default function ModalAprobarSolicitud({
  enviando,
  onCancel,
  onConfirm,
}: Props) {
  return (
    <ModalDecision
      title="¿Aprobar solicitud?"
      description="Al aprobarla, la solicitud quedará confirmada y ya no podrás cambiar su estado."
      onClose={onCancel}
      closeDisabled={enviando}
    >
      <div className="mt-6 flex justify-end gap-3">
        <button
          type="button"
          onClick={onCancel}
          disabled={enviando}
          className="rounded-full border border-border-secondary bg-white px-5 py-2.5 text-sm font-semibold text-text-primary hover:bg-bg-surface-secondary disabled:cursor-not-allowed disabled:opacity-60"
        >
          Cancelar
        </button>
        <button
          type="button"
          onClick={onConfirm}
          disabled={enviando}
          className="rounded-full bg-bg-fill-primary px-5 py-2.5 text-sm font-semibold text-text-invert hover:bg-bg-fill-primary-hover disabled:cursor-not-allowed disabled:opacity-60"
        >
          {enviando ? "Aprobando…" : "Aprobar"}
        </button>
      </div>
    </ModalDecision>
  );
}
