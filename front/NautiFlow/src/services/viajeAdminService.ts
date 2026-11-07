const API_BASE_URL = (
  import.meta.env.VITE_API_URL ?? "http://localhost:8080"
).replace(/\/+$/, "");

export interface CrearViajeRequest {
  idLancha: number;
  idZonaOrigen: number;
  idZonaDestino: number;
  cantidadPasajeros: number;
  turistaNombreCompleto: string;
  turistaWhatsapp: string;
}

export interface ViajeCreado {
  id: number;
  idLancha: number;
  idLanchero: number;
  lancheroNombreCompleto: string;
  zonaOrigen: string;
  zonaDestino: string;
  fechaHoraInicio: string;
  fechaHoraFin: string | null;
  cantidadPasajeros: number;
  turistaNombreCompleto: string;
  turistaWhatsapp: string;
}

export interface ViajeFinalizado {
  id: number;
  fechaHoraFin: string;
  tokenResena: string;
  nombreUsuario: string;
  whatsappUsuario: string;
  mensajeResena: string;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function isViajeCreado(value: unknown): value is ViajeCreado {
  return isRecord(value) &&
    typeof value.id === "number" &&
    typeof value.idLancha === "number" &&
    typeof value.idLanchero === "number" &&
    typeof value.lancheroNombreCompleto === "string" &&
    typeof value.zonaOrigen === "string" &&
    typeof value.zonaDestino === "string" &&
    typeof value.fechaHoraInicio === "string" &&
    (typeof value.fechaHoraFin === "string" || value.fechaHoraFin === null) &&
    typeof value.cantidadPasajeros === "number" &&
    typeof value.turistaNombreCompleto === "string" &&
    typeof value.turistaWhatsapp === "string";
}

function isViajeFinalizado(value: unknown): value is ViajeFinalizado {
  return isRecord(value) && typeof value.id === "number" &&
    typeof value.fechaHoraFin === "string" && typeof value.tokenResena === "string" &&
    typeof value.nombreUsuario === "string" && typeof value.whatsappUsuario === "string" &&
    typeof value.mensajeResena === "string";
}

async function errorServidor(response: Response): Promise<Error> {
  let message = `No se pudo completar la operación del viaje (HTTP ${response.status}).`;
  try {
    const body: unknown = await response.json();
    if (isRecord(body)) {
      if (typeof body.message === "string") message = body.message;
      else if (typeof body.mensaje === "string") message = body.mensaje;
      else if (typeof body.detail === "string") message = body.detail;
    }
  } catch {
    // Conserva el error HTTP cuando el servidor no devuelve JSON.
  }
  return new Error(message);
}

export async function registrarViaje(
  token: string,
  request: CrearViajeRequest,
): Promise<ViajeCreado> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/admin/viajes`, {
      method: "POST",
      headers: {
        Accept: "application/json",
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    });
  } catch {
    throw new Error("No se pudo conectar con el servidor.");
  }
  if (!response.ok) throw await errorServidor(response);

  let body: unknown;
  try {
    body = await response.json();
  } catch {
    throw new Error("El servidor devolvió una respuesta inválida al crear el viaje.");
  }
  if (!isViajeCreado(body)) {
    throw new Error("El servidor devolvió datos de viaje inválidos.");
  }
  return body;
}

async function solicitarAdmin(token: string, path: string, method: "GET" | "PATCH", signal?: AbortSignal): Promise<Response> {
  try {
    return await fetch(`${API_BASE_URL}${path}`, {
      method,
      signal,
      headers: { Accept: "application/json", Authorization: `Bearer ${token}` },
    });
  } catch {
    throw new Error("No se pudo conectar con el servidor.");
  }
}

async function leerRespuesta<T>(response: Response, validar: (value: unknown) => value is T): Promise<T> {
  if (!response.ok) throw await errorServidor(response);
  let body: unknown;
  try { body = await response.json(); } catch { throw new Error("El servidor devolvió una respuesta inválida."); }
  if (!validar(body)) throw new Error("El servidor devolvió datos de viaje inválidos.");
  return body;
}

export async function finalizarViaje(token: string, id: number): Promise<ViajeFinalizado> {
  const response = await solicitarAdmin(token, `/admin/viajes/${id}/finalizar`, "PATCH");
  return leerRespuesta(response, isViajeFinalizado);
}
