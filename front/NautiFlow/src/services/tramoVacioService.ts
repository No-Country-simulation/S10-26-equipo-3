import { API_BASE_URL } from "../config/api";

export interface TramoVacio {
  id: number;
  idViaje: number;
  idLancha: number;
  nombreEmbarcacion: string;
  capacidad: number;
  zonaOrigen: string;
  zonaDestino: string;
  fechaHoraRetorno: string;
  mensaje: string;
}

function esRegistro(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function esTramoVacio(value: unknown): value is TramoVacio {
  return esRegistro(value) &&
    typeof value.id === "number" &&
    typeof value.idViaje === "number" &&
    typeof value.idLancha === "number" &&
    typeof value.nombreEmbarcacion === "string" &&
    typeof value.capacidad === "number" &&
    typeof value.zonaOrigen === "string" &&
    typeof value.zonaDestino === "string" &&
    typeof value.fechaHoraRetorno === "string" &&
    typeof value.mensaje === "string";
}

async function errorServidor(response: Response): Promise<Error> {
  let message = `No se pudo completar la operación del tramo vacío (HTTP ${response.status}).`;
  try {
    const body: unknown = await response.json();
    if (esRegistro(body) && typeof body.message === "string") message = body.message;
  } catch {
    return new Error(message);
  }
  return new Error(message);
}

async function solicitar(token: string, path: string, init: RequestInit = {}): Promise<Response> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers: {
        Accept: "application/json",
        Authorization: `Bearer ${token}`,
        ...(init.body ? { "Content-Type": "application/json" } : {}),
      },
    });
  } catch (cause) {
    if (init.signal?.aborted) throw cause;
    throw new Error("No se pudo conectar con el servidor.", { cause });
  }
  if (!response.ok) throw await errorServidor(response);
  return response;
}

async function leerJson(response: Response): Promise<unknown> {
  try {
    return await response.json() as unknown;
  } catch {
    throw new Error("El servidor devolvió una respuesta inválida.");
  }
}

export async function publicarRetornoVacio(token: string, idViaje: number, horaRetorno: string): Promise<TramoVacio> {
  const response = await solicitar(token, `/admin/viajes/${idViaje}/retorno-vacio`, {
    method: "POST",
    body: JSON.stringify({ horaRetorno }),
  });
  const body = await leerJson(response);
  if (!esTramoVacio(body)) throw new Error("El servidor devolvió un tramo vacío inválido.");
  return body;
}

export async function listarTramosVacios(token: string, signal?: AbortSignal): Promise<TramoVacio[]> {
  const response = await solicitar(token, "/admin/tramos-vacios", { signal });
  const body = await leerJson(response);
  if (!Array.isArray(body) || !body.every(esTramoVacio)) {
    throw new Error("El servidor devolvió tramos vacíos inválidos.");
  }
  return body;
}

export async function quitarTramoVacio(token: string, id: number): Promise<void> {
  await solicitar(token, `/admin/tramos-vacios/${id}`, { method: "DELETE" });
}
