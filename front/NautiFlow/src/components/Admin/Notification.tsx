import { useEffect, useRef, useState } from "react";
import { BsCheckCircleFill, BsExclamationCircleFill, BsXLg } from "react-icons/bs";
import "./Notification.css";

export type NotificationKind = "success" | "error";

type Props = {
  kind: NotificationKind;
  title: string;
  message: string;
  onClose: () => void;
};

export default function Notification({ kind, title, message, onClose }: Props) {
  const [exiting, setExiting] = useState(false);
  const onCloseRef = useRef(onClose);

  useEffect(() => {
    onCloseRef.current = onClose;
  }, [onClose]);

  useEffect(() => {
    const exitTimeoutId = window.setTimeout(() => setExiting(true), 3800);
    const closeTimeoutId = window.setTimeout(() => onCloseRef.current(), 4000);
    return () => {
      window.clearTimeout(exitTimeoutId);
      window.clearTimeout(closeTimeoutId);
    };
  }, []);

  const isError = kind === "error";
  const Icon = isError ? BsExclamationCircleFill : BsCheckCircleFill;

  return (
    <div
      role={isError ? "alert" : "status"}
      aria-live={isError ? "assertive" : "polite"}
      className={`admin-toast admin-toast--${kind}${exiting ? " admin-toast--exiting" : ""}`}
    >
      <Icon className="admin-toast__icon" aria-hidden="true" />
      <div className="admin-toast__content">
        <p className="admin-toast__title">{title}</p>
        <p className="admin-toast__message">{message}</p>
      </div>
      <button
        type="button"
        onClick={onClose}
        aria-label="Cerrar notificación"
        className="admin-toast__close"
      >
        <BsXLg aria-hidden="true" />
      </button>
    </div>
  );
}
