import { useRef, useState, type FormEvent } from "react";
import { BsXLg } from "react-icons/bs";
import { registrarViaje, type ViajeCreado } from "../../services/viajeAdminService";

type Props = {
  token: string;
  lancha: {
    id: number;
    nombre: string;
    capacidad: number;
  };
  busqueda: {
    idZonaOrigen: number;
    zonaOrigen: string;
    idZonaDestino: number;
    zonaDestino: string;
    hora: string;
    cantidadPasajeros: number;
  };
  onAssigned?: (trip: ViajeCreado) => void;
};

function esBusquedaValida(busqueda: Props["busqueda"], capacidad: number): boolean {
  return Number.isInteger(busqueda.idZonaOrigen) &&
    busqueda.idZonaOrigen > 0 &&
    Number.isInteger(busqueda.idZonaDestino) &&
    busqueda.idZonaDestino > 0 &&
    busqueda.idZonaOrigen !== busqueda.idZonaDestino &&
    Number.isInteger(busqueda.cantidadPasajeros) &&
    busqueda.cantidadPasajeros > 0 &&
    busqueda.cantidadPasajeros <= capacidad &&
    /^([01]\d|2[0-3]):[0-5]\d$/.test(busqueda.hora);
}

export default function AsignarViajeButton({
  token,
  lancha,
  busqueda,
  onAssigned,
}: Props) {
  const [abierto, setAbierto] = useState(false);
  const pasajeros = busqueda.cantidadPasajeros;
  const [nombre, setNombre] = useState("");
  const [telefono, setTelefono] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const envioEnCurso = useRef(false);

  const cerrar = () => {
    if (enviando) return;
    setAbierto(false);
    setError(null);
  };

  const enviar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (envioEnCurso.current) return;

    const nombreNormalizado = nombre.trim();
    const telefonoNormalizado = telefono.replace(/\D/g, "");
    if (!nombreNormalizado || !telefonoNormalizado) {
      setError("Ingresa el nombre del turista y su número de WhatsApp.");
      return;
    }
    if (!Number.isInteger(lancha.id) || lancha.id < 1 ||
        !Number.isInteger(lancha.capacidad) || lancha.capacidad < 1) {
      setError("Los datos de la lancha seleccionada no son válidos.");
      return;
    }
    if (!esBusquedaValida(busqueda, lancha.capacidad)) {
      setError("Los datos de la búsqueda seleccionada no son válidos para esta lancha.");
      return;
    }
    if (!Number.isInteger(pasajeros) || pasajeros < 1 || pasajeros > lancha.capacidad) {
      setError(`La cantidad de pasajeros debe estar entre 1 y ${lancha.capacidad}.`);
      return;
    }
    if (`+54${telefonoNormalizado}`.length > 20) {
      setError("El número completo de WhatsApp no puede superar 20 caracteres.");
      return;
    }

    envioEnCurso.current = true;
    setEnviando(true);
    setError(null);
    let trip: ViajeCreado;
    try {
      trip = await registrarViaje(token, {
        idLancha: lancha.id,
        idZonaOrigen: busqueda.idZonaOrigen,
        idZonaDestino: busqueda.idZonaDestino,
        cantidadPasajeros: pasajeros,
        turistaNombreCompleto: nombreNormalizado,
        turistaWhatsapp: `+54${telefonoNormalizado}`,
      });
    } catch (reason: unknown) {
      setError(reason instanceof Error ? reason.message : "No se pudo asignar el viaje.");
      return;
    } finally {
      setEnviando(false);
      envioEnCurso.current = false;
    }
    setAbierto(false);
    setNombre("");
    setTelefono("");
    onAssigned?.(trip);
  };

  return (
    <>
      <button
        type="button"
        onClick={() => {
          setError(null);
          setAbierto(true);
        }}
        className="rounded-full bg-bg-fill-primary px-5 py-2.5 text-sm font-semibold text-text-invert transition-colors hover:bg-bg-fill-primary-hover focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary"
      >
        Asignar viaje
      </button>

      {abierto && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <button
            type="button"
            aria-label="Cerrar modal"
            onClick={cerrar}
            disabled={enviando}
            className="absolute inset-0 cursor-default disabled:cursor-wait"
          />
          <section
            role="dialog"
            aria-modal="true"
            aria-labelledby="asignar-viaje-title"
            className="relative w-full max-w-lg rounded-xl border border-border-secondary bg-white p-6 shadow-xl"
          >
            <button
              type="button"
              onClick={cerrar}
              aria-label="Cerrar"
              disabled={enviando}
              className="absolute right-4 top-4 grid h-8 w-8 place-items-center rounded-md text-text-secondary hover:bg-bg-surface-secondary hover:text-text-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary disabled:cursor-not-allowed disabled:opacity-60"
            >
              <BsXLg className="h-4 w-4" aria-hidden="true" />
            </button>
            <h2 id="asignar-viaje-title" className="pr-10 text-xl font-bold text-text-primary">
              Nuevo viaje
            </h2>
            <p className="mt-1 text-sm text-text-secondary">{lancha.nombre}</p>

            <form onSubmit={(event) => void enviar(event)} className="mt-6 space-y-4">
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label htmlFor="viaje-pasajeros" className="mb-1.5 block text-sm font-medium text-text-primary">
                    Número de pasajeros
                  </label>
                  <input
                    id="viaje-pasajeros"
                    value={pasajeros}
                    readOnly
                    className="h-11 w-full rounded-lg border border-border-secondary bg-bg-surface-primary px-3 text-sm text-text-primary"
                  />
                  <p className="mt-1 text-xs text-text-secondary">Capacidad máxima: {lancha.capacidad}</p>
                </div>
                <div>
                  <label htmlFor="viaje-hora" className="mb-1.5 block text-sm font-medium text-text-primary">
                    Hora
                  </label>
                  <input
                    id="viaje-hora"
                    value={busqueda.hora}
                    readOnly
                    className="h-11 w-full rounded-lg border border-border-secondary bg-bg-surface-primary px-3 text-sm text-text-primary"
                  />
                </div>
                <div>
                  <label htmlFor="viaje-origen" className="mb-1.5 block text-sm font-medium text-text-primary">
                    Zona origen
                  </label>
                  <input
                    id="viaje-origen"
                    value={busqueda.zonaOrigen}
                    readOnly
                    className="h-11 w-full rounded-lg border border-border-secondary bg-bg-surface-primary px-3 text-sm text-text-primary"
                  />
                </div>
                <div>
                  <label htmlFor="viaje-destino" className="mb-1.5 block text-sm font-medium text-text-primary">
                    Zona destino
                  </label>
                  <input
                    id="viaje-destino"
                    value={busqueda.zonaDestino}
                    readOnly
                    className="h-11 w-full rounded-lg border border-border-secondary bg-bg-surface-primary px-3 text-sm text-text-primary"
                  />
                </div>
              </div>

              <div>
                <label htmlFor="viaje-turista" className="mb-1.5 block text-sm font-medium text-text-primary">
                  Nombre turista
                </label>
                <input
                  id="viaje-turista"
                  value={nombre}
                  onChange={(event) => setNombre(event.target.value)}
                  required
                  maxLength={201}
                  autoComplete="name"
                  disabled={enviando}
                  className="h-11 w-full rounded-lg border border-border-secondary px-3 text-sm text-text-primary outline-none focus:border-bg-fill-primary focus:ring-2 focus:ring-bg-fill-primary/20 disabled:bg-bg-surface-primary"
                />
              </div>

              <div>
                <label htmlFor="viaje-whatsapp" className="mb-1.5 block text-sm font-medium text-text-primary">
                  WhatsApp
                </label>
                <div className="flex gap-2">
                  <span className="flex h-11 w-24 shrink-0 items-center justify-center gap-2 rounded-lg border border-border-secondary bg-bg-surface-primary text-sm font-medium text-text-primary">
                    <img src="/argentina.svg" alt="Bandera de Argentina" className="h-5 w-5 object-contain" />
                    +54
                  </span>
                  <input
                    id="viaje-whatsapp"
                    type="tel"
                    value={telefono}
                    onChange={(event) => setTelefono(event.target.value)}
                    required
                    maxLength={20}
                    pattern="[0-9\\s()+-]{6,20}"
                    inputMode="tel"
                    autoComplete="tel-national"
                    disabled={enviando}
                    className="h-11 min-w-0 flex-1 rounded-lg border border-border-secondary px-3 text-sm text-text-primary outline-none focus:border-bg-fill-primary focus:ring-2 focus:ring-bg-fill-primary/20 disabled:bg-bg-surface-primary"
                  />
                </div>
              </div>

              {error && <p role="alert" className="text-sm text-text-error">{error}</p>}

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={cerrar}
                  disabled={enviando}
                  className="rounded-full border border-border-secondary bg-white px-5 py-2.5 text-sm font-semibold text-text-primary hover:bg-bg-surface-secondary disabled:cursor-not-allowed disabled:opacity-60"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={enviando}
                  className="rounded-full bg-bg-fill-primary px-5 py-2.5 text-sm font-semibold text-text-invert hover:bg-bg-fill-primary-hover disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {enviando ? "Asignando…" : "Asignar viaje"}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </>
  );
}
