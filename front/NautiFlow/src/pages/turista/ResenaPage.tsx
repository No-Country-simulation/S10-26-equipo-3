import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { BsPencil, BsStarFill } from "react-icons/bs";
import Logo from "../../components/Logo";
import {
  consultarResenaPorToken,
  enviarCalificacionResena,
} from "../../services/resenaPublicService";

type EstadoVista = "cargando" | "formulario" | "enviada" | "no-disponible";

export default function ResenaPage() {
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();

  const [estado, setEstado] = useState<EstadoVista>("cargando");
  const [puntuacion, setPuntuacion] = useState<number>(0);
  const [hoverPuntuacion, setHoverPuntuacion] = useState<number>(0);
  const [comentario, setComentario] = useState<string>("");
  const [enviando, setEnviando] = useState<boolean>(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  useEffect(() => {
    if (!token) {
      setEstado("no-disponible");
      return;
    }

    const controller = new AbortController();
    consultarResenaPorToken(token, controller.signal)
      .then((data) => {
        if (data.resenaRegistrada) {
          setEstado("no-disponible");
        } else {
          setEstado("formulario");
        }
      })
      .catch(() => {
        if (!controller.signal.aborted) {
          setEstado("no-disponible");
        }
      });

    return () => controller.abort();
  }, [token]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;

    if (puntuacion < 1 || puntuacion > 5) {
      setErrorEnvio("Por favor selecciona una puntuación antes de enviar.");
      return;
    }

    setEnviando(true);
    setErrorEnvio(null);

    try {
      await enviarCalificacionResena(token, {
        puntuacion,
        comentario: comentario.trim() || undefined,
      });
      setEstado("enviada");
    } catch (err: unknown) {
      const msg =
        err instanceof Error ? err.message : "No se pudo enviar la calificación. Intenta nuevamente.";
      setErrorEnvio(msg);
    } finally {
      setEnviando(false);
    }
  };

  const handleCerrar = () => {
    try {
      if (window.opener && !window.opener.closed) {
        window.close();
      } else {
        navigate("/");
      }
    } catch {
      navigate("/");
    }
  };

  return (
    <main className="min-h-dvh w-full bg-white flex flex-col items-center justify-center px-6 py-8 font-inter text-text-primary selection:bg-bg-fill-primary/20">
      <div className="w-full max-w-[360px] sm:max-w-[400px] flex flex-col items-center gap-6">
        {/* Logotipo */}
        <div className="flex items-center justify-center shrink-0">
          <Logo className="h-16 w-auto" />
        </div>

        {estado === "cargando" && (
          <div className="flex flex-col items-center justify-center gap-3 py-16">
            <div className="size-8 rounded-full border-2 border-bg-fill-primary border-t-transparent animate-spin" />
            <span className="text-sm font-medium text-text-secondary animate-pulse">
              Cargando información del viaje…
            </span>
          </div>
        )}

        {/* Estado 1: Formulario de Calificación (Figma 2199:2397) */}
        {estado === "formulario" && (
          <>
            {/* Ilustración lancha */}
            <div className="h-[160px] w-[260px] relative shrink-0 flex items-center justify-center">
              <img
                src="/resena-experiencia.png"
                alt="Experiencia en lancha"
                className="h-full w-full object-contain pointer-events-none select-none"
              />
            </div>

            {/* Contenido - Introducción */}
            <div className="flex flex-col gap-2 items-center text-center w-full">
              <h1 className="text-2xl font-semibold leading-7 text-text-primary">
                ¿Cómo estuvo tu experiencia?
              </h1>
              <p className="text-base font-semibold leading-6 text-text-secondary">
                Califica tu viaje en lancha
              </p>
            </div>

            {/* Formulario */}
            <form onSubmit={handleSubmit} className="w-full flex flex-col gap-6">
              {/* Calificación - 5 Estrellas */}
              <div
                role="group"
                aria-label="Calificación con estrellas"
                className="flex items-center justify-center gap-3 w-full"
                onMouseLeave={() => setHoverPuntuacion(0)}
              >
                {[1, 2, 3, 4, 5].map((star) => {
                  const valorActivo = hoverPuntuacion || puntuacion;
                  const estaRellena = star <= valorActivo;

                  return (
                    <button
                      key={star}
                      type="button"
                      onClick={() => {
                        setPuntuacion(star);
                        setErrorEnvio(null);
                      }}
                      onMouseEnter={() => setHoverPuntuacion(star)}
                      aria-label={`${star} estrella${star > 1 ? "s" : ""}`}
                      className="size-9 flex items-center justify-center transition-transform hover:scale-110 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-bg-fill-primary/30 rounded-md cursor-pointer"
                    >
                      <BsStarFill
                        className={`size-[30px] transition-colors ${
                          estaRellena ? "text-bg-fill-accent" : "text-[#D5D7DB]"
                        }`}
                        aria-hidden="true"
                      />
                    </button>
                  );
                })}
              </div>

              {/* Campo - Comentario */}
              <div className="w-full h-[120px] rounded-lg border border-border-secondary bg-white p-4 flex gap-2 items-start focus-within:border-bg-fill-primary focus-within:ring-2 focus-within:ring-bg-fill-primary/20 transition-all">
                <BsPencil
                  className="size-5 text-text-secondary mt-0.5 shrink-0"
                  aria-hidden="true"
                />
                <textarea
                  value={comentario}
                  onChange={(e) => setComentario(e.target.value)}
                  placeholder="Cuéntanos tu experiencia (opcional)"
                  maxLength={500}
                  disabled={enviando}
                  className="w-full h-full resize-none text-sm font-semibold text-text-primary placeholder:text-text-secondary placeholder:font-semibold focus:outline-none bg-transparent"
                />
              </div>

              {errorEnvio && (
                <p role="alert" className="text-sm font-medium text-text-error text-center">
                  {errorEnvio}
                </p>
              )}

              {/* Botón Enviar calificación */}
              <div className="w-full flex justify-center">
                <button
                  type="submit"
                  disabled={enviando}
                  className="w-full h-12 rounded-full bg-bg-fill-primary text-text-invert text-lg font-semibold flex items-center justify-center hover:bg-bg-fill-primary-hover transition-colors shadow-sm cursor-pointer disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {enviando ? "Enviando…" : "Enviar calificación"}
                </button>
              </div>
            </form>
          </>
        )}

        {/* Estado 2: Calificación Enviada (Figma 2201:2498) */}
        {estado === "enviada" && (
          <>
            {/* Ilustración de confirmación */}
            <div className="h-[160px] w-[260px] relative shrink-0 flex items-center justify-center">
              <img
                src="/resena-enviada.png"
                alt="Calificación enviada"
                className="h-full w-full object-contain pointer-events-none select-none"
              />
            </div>

            {/* Contenido - Éxito */}
            <div className="flex flex-col gap-2 items-center text-center w-full">
              <h1 className="text-2xl font-semibold leading-7 text-text-primary">
                ¡Gracias por compartir tu experiencia!
              </h1>
              <p className="text-base font-semibold leading-6 text-text-secondary">
                Tu calificación se ha enviado correctamente.
              </p>
            </div>

            {/* Botón Cerrar */}
            <div className="w-full flex justify-center pt-2">
              <button
                type="button"
                onClick={handleCerrar}
                className="w-full h-12 rounded-full bg-bg-fill-primary text-text-invert text-lg font-semibold flex items-center justify-center hover:bg-bg-fill-primary-hover transition-colors shadow-sm cursor-pointer"
              >
                Cerrar
              </button>
            </div>
          </>
        )}

        {/* Estado 3: Enlace No Disponible (Figma 2201:2574) */}
        {estado === "no-disponible" && (
          <>
            {/* Ilustración enlace no disponible */}
            <div className="h-[160px] w-[260px] relative shrink-0 flex items-center justify-center">
              <img
                src="/resena-no-disponible.png"
                alt="Enlace no disponible"
                className="h-full w-full object-contain pointer-events-none select-none"
              />
            </div>

            {/* Contenido - No disponible */}
            <div className="flex flex-col gap-2 items-center text-center w-full">
              <h1 className="text-2xl font-semibold leading-7 text-text-primary">
                Este enlace ya no está disponible
              </h1>
              <p className="text-base font-semibold leading-6 text-text-secondary">
                El enlace puede haber caducado o ya fue utilizado.
              </p>
            </div>

            {/* Botón Volver al inicio */}
            <div className="w-full flex justify-center pt-2">
              <button
                type="button"
                onClick={() => navigate("/")}
                className="w-full h-12 rounded-full bg-bg-fill-primary text-text-invert text-lg font-semibold flex items-center justify-center hover:bg-bg-fill-primary-hover transition-colors shadow-sm cursor-pointer"
              >
                Volver al inicio
              </button>
            </div>
          </>
        )}
      </div>
    </main>
  );
}

