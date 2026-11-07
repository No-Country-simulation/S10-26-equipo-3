import { useEffect, useRef, type ReactNode } from "react";
import { BsXLg } from "react-icons/bs";

type Props = {
  title: string;
  description: string;
  children: ReactNode;
  onClose: () => void;
  closeDisabled?: boolean;
};

export default function ModalDecision({
  title,
  description,
  children,
  onClose,
  closeDisabled = false,
}: Props) {
  const closeButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape" && !closeDisabled) onClose();
    };

    window.addEventListener("keydown", handleKeyDown);
    closeButtonRef.current?.focus();
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose, closeDisabled]);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <button
        type="button"
        aria-label="Cerrar modal"
        onClick={() => {
          if (!closeDisabled) onClose();
        }}
        disabled={closeDisabled}
        className="absolute inset-0 cursor-default disabled:cursor-wait"
      />
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-decision-title"
        aria-describedby="modal-decision-description"
        className="relative w-full max-w-md rounded-lg border border-border-secondary bg-white p-6 shadow-xl"
      >
        <button
          ref={closeButtonRef}
          type="button"
          onClick={onClose}
          aria-label="Cerrar"
          disabled={closeDisabled}
          className="absolute right-4 top-4 grid h-8 w-8 place-items-center rounded-md text-text-secondary hover:bg-bg-surface-secondary hover:text-text-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary disabled:cursor-not-allowed disabled:opacity-60"
        >
          <BsXLg className="h-4 w-4" aria-hidden="true" />
        </button>
        <h2 id="modal-decision-title" className="pr-10 text-lg font-semibold text-text-primary">
          {title}
        </h2>
        <p id="modal-decision-description" className="mt-3 text-sm leading-5 text-text-secondary">
          {description}
        </p>
        {children}
      </section>
    </div>
  );
}
