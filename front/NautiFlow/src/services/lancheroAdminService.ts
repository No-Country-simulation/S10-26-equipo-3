const API_BASE_URL = (
  import.meta.env.VITE_API_URL ?? "http://localhost:8080"
).replace(/\/+$/, "");

export type EstadoSolicitudLanchero =
  | "PENDIENTE"
  | "APROBADO"
  | "RECHAZADO"
  | "TODOS";

export type EstadoViajeLanchero = "ACTIVO" | "INACTIVO" | "EN_VIAJE" | "EN_RETORNO";
export type EstadoViajeEditable = Extract<EstadoViajeLanchero, "ACTIVO" | "INACTIVO">;

export interface LancheroAdmin {
  id: number;
  nombreCompleto: string;
  whatsapp: string;
  nombreLancha: string | null;
  fechaRegistro: string;
  estadoSolicitud: Exclude<EstadoSolicitudLanchero, "TODOS">;
  estadoViaje: EstadoViajeLanchero;
}

export interface PaginaLancheros {
  content: LancheroAdmin[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function esEstadoSolicitud(
  value: unknown,
): value is LancheroAdmin["estadoSolicitud"] {
  return value === "PENDIENTE" || value === "APROBADO" || value === "RECHAZADO";
}

function esEstadoViaje(value: unknown): value is EstadoViajeLanchero {
  return value === "ACTIVO" || value === "INACTIVO" || value === "EN_VIAJE" || value === "EN_RETORNO";
}

function esTextoNullable(value: unknown): value is string | null {
  return typeof value === "string" || value === null;
}

function esLanchero(value: unknown): value is LancheroAdmin {
  return isRecord(value) &&
    typeof value.id === "number" &&
    typeof value.nombreCompleto === "string" &&
    typeof value.whatsapp === "string" &&
    esTextoNullable(value.nombreLancha) &&
    typeof value.fechaRegistro === "string" &&
    esEstadoSolicitud(value.estadoSolicitud) &&
    esEstadoViaje(value.estadoViaje);
}

async function errorRespuesta(response: Response): Promise<Error> {
  let message = `No se pudo completar la operación (HTTP ${response.status}).`;
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

async function solicitar(
  token: string,
  path: string,
  init: RequestInit = {},
): Promise<Response> {
  try {
    const headers = new Headers(init.headers);
    headers.set("Accept", "application/json");
    headers.set("Authorization", `Bearer ${token}`);
    return await fetch(`${API_BASE_URL}${path}`, { ...init, headers });
  } catch {
    throw new Error("No se pudo conectar con el servidor.");
  }
}

async function leerLancheroActualizado(
  response: Response,
  id: number,
  estadoViaje: EstadoViajeEditable,
): Promise<EstadoViajeEditable> {
  if (!response.ok) throw await errorRespuesta(response);
  if (response.status !== 200) {
    throw new Error(`El servidor respondió con HTTP ${response.status}; se esperaba 200.`);
  }

  let body: unknown;
  try {
    body = await response.json();
  } catch {
    throw new Error("El servidor devolvió una respuesta inválida al actualizar el estado.");
  }
  if (!isRecord(body) || body.id !== id || body.estadoViaje !== estadoViaje) {
    throw new Error("El servidor no confirmó el estado solicitado para el lanchero.");
  }
  return estadoViaje;
}

export async function listarLancheros(
  token: string,
  estado: EstadoSolicitudLanchero,
  page: number,
  size: number,
  signal: AbortSignal,
): Promise<PaginaLancheros> {
  const query = new URLSearchParams({
    estado,
    page: String(page),
    size: String(size),
  });
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/admin/lancheros?${query}`, {
      signal,
      headers: {
        Accept: "application/json",
        Authorization: `Bearer ${token}`,
      },
    });
  } catch {
    throw new Error("No se pudo conectar con el servidor.");
  }
  if (!response.ok) throw await errorRespuesta(response);

  let body: unknown;
  try {
    body = await response.json();
  } catch {
    throw new Error("El servidor devolvió una respuesta inválida.");
  }
  if (
    !isRecord(body) ||
    !Array.isArray(body.content) ||
    !body.content.every(esLanchero) ||
    typeof body.page !== "number" ||
    typeof body.size !== "number" ||
    typeof body.totalElements !== "number" ||
    typeof body.totalPages !== "number"
  ) {
    throw new Error("El servidor devolvió una lista de lancheros inválida.");
  }

  return {
    content: body.content,
    page: body.page,
    size: body.size,
    totalElements: body.totalElements,
    totalPages: body.totalPages,
  };
}

export async function actualizarEstadoLanchero(
  token: string,
  id: number,
  estadoViaje: EstadoViajeEditable,
): Promise<EstadoViajeEditable> {
  const response = await solicitar(token, `/admin/lancheros/${id}/estado-viaje`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ estadoViaje }),
  });
  return leerLancheroActualizado(response, id, estadoViaje);
}

export interface LanchaDisponible {
  id: number;
  nombre: string;
  capacidad: number;
  zonasDisponibles: string[];
  verificada: boolean;
  estadoOperativo: string;
  whatsappLanchero: string;
}

export interface FiltrosBuscarLanchas {
  capacidad?: number;
  zonaOrigen?: number | string;
  zonaDestino?: number | string;
  hora: string;
}

export async function buscarLanchasDisponibles(
  token: string,
  filtros: FiltrosBuscarLanchas,
  signal?: AbortSignal,
): Promise<LanchaDisponible[]> {
  const query = new URLSearchParams();
  if (filtros.capacidad !== undefined && filtros.capacidad !== null) {
    query.set("capacidad", String(filtros.capacidad));
  }
  if (filtros.zonaOrigen !== undefined && filtros.zonaOrigen !== null) {
    query.set("zonaOrigen", String(filtros.zonaOrigen));
  }
  if (filtros.zonaDestino !== undefined && filtros.zonaDestino !== null) {
    query.set("zonaDestino", String(filtros.zonaDestino));
  }
  query.set("hora", filtros.hora);

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/admin/lancheros/disponibles?${query.toString()}`, {
      signal,
      headers: {
        Accept: "application/json",
        Authorization: `Bearer ${token}`,
      },
    });
  } catch (err: unknown) {
    if (signal?.aborted) throw err;
    throw new Error("No se pudo conectar con el servidor.");
  }

  if (!response.ok) {
    throw await errorRespuesta(response);
  }

  let body: unknown;
  try {
    body = await response.json();
  } catch {
    throw new Error("El servidor devolvió una respuesta inválida.");
  }

  if (!Array.isArray(body)) {
    throw new Error("El servidor devolvió un formato inesperado de embarcaciones disponibles.");
  }

  return body as LanchaDisponible[];
}
