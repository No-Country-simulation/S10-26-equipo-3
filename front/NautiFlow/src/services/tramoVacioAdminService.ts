const API_BASE_URL = (
  import.meta.env.VITE_API_URL ?? "http://localhost:8080"
).replace(/\/+$/, "");

export interface CrearTramoVacioRequest {
  horaRetorno: string;
  turistaNombreCompleto?: string;
  turistaWhatsapp?: string;
}

export interface TramoVacio {
  id: number;
  viajeId: number;
  lanchaNombre: string;
  lancheroNombre: string;
  zonaOrigen: string;
  zonaDestino: string;
  fecha: string;
  horaRetorno: string;
  turistaNombreCompleto: string | null;
  turistaWhatsapp: string | null;
  estado: "ACTIVO" | "INACTIVO";
  mensaje: string;
}

export interface FiltrosTramosVacios {
  fechaDesde?: string;
  fechaHasta?: string;
  estado?: TramoVacio["estado"];
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function isNullableString(value: unknown): value is string | null {
  return typeof value === "string" || value === null;
}

function isTramoVacio(value: unknown): value is TramoVacio {
  return isRecord(value) &&
    typeof value.id === "number" &&
    typeof value.viajeId === "number" &&
    typeof value.lanchaNombre === "string" &&
    typeof value.lancheroNombre === "string" &&
    typeof value.zonaOrigen === "string" &&
    typeof value.zonaDestino === "string" &&
    typeof value.fecha === "string" &&
    typeof value.horaRetorno === "string" &&
    isNullableString(value.turistaNombreCompleto) &&
    isNullableString(value.turistaWhatsapp) &&
    (value.estado === "ACTIVO" || value.estado === "INACTIVO") &&
    typeof value.mensaje === "string";
}

async function errorServidor(response: Response): Promise<Error> {
  let message = `No se pudo completar la operación del retorno (HTTP ${response.status}).`;
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
  options: { method?: "GET" | "POST" | "PATCH"; body?: unknown; signal?: AbortSignal } = {},
): Promise<Response> {
  const { method = "GET", body, signal } = options;
  const headers: Record<string, string> = {
    Accept: "application/json",
    Authorization: `Bearer ${token}`,
  };
  if (body !== undefined) headers["Content-Type"] = "application/json";

  try {
    return await fetch(`${API_BASE_URL}${path}`, {
      method,
      signal,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new Error("No se pudo conectar con el servidor.");
  }
}

async function leerTramo(response: Response): Promise<TramoVacio> {
  if (!response.ok) throw await errorServidor(response);
  let body: unknown;
  try {
    body = await response.json();
  } catch {
    throw new Error("El servidor devolvió una respuesta inválida.");
  }
  if (!isTramoVacio(body)) {
    throw new Error("El servidor devolvió datos de retorno inválidos.");
  }
  return body;
}

export async function listarTramosVacios(
  token: string,
  filtros: FiltrosTramosVacios,
  signal: AbortSignal,
): Promise<TramoVacio[]> {
  const query = new URLSearchParams();
  if (filtros.fechaDesde) query.set("fechaDesde", filtros.fechaDesde);
  if (filtros.fechaHasta) query.set("fechaHasta", filtros.fechaHasta);
  if (filtros.estado) query.set("estado", filtros.estado);
  const suffix = query.size > 0 ? `?${query.toString()}` : "";
  const response = await solicitar(token, `/admin/tramos-vacios${suffix}`, { signal });
  if (!response.ok) throw await errorServidor(response);

  let body: unknown;
  try {
    body = await response.json();
  } catch {
    throw new Error("El servidor devolvió una respuesta inválida.");
  }
  if (!Array.isArray(body) || !body.every(isTramoVacio)) {
    throw new Error("El servidor devolvió una lista de retornos inválida.");
  }
  return body;
}

export async function publicarTramoVacio(
  token: string,
  viajeId: number,
  request: CrearTramoVacioRequest,
): Promise<TramoVacio> {
  const response = await solicitar(token, `/admin/viajes/${viajeId}/retornos-vacios`, {
    method: "POST",
    body: request,
  });
  return leerTramo(response);
}

export async function obtenerMensajeTramoVacio(token: string, id: number): Promise<string> {
  const response = await solicitar(token, `/admin/tramos-vacios/${id}/mensaje`);
  if (!response.ok) throw await errorServidor(response);
  let body: unknown;
  try {
    body = await response.json();
  } catch {
    throw new Error("El servidor devolvió una respuesta inválida.");
  }
  if (!isRecord(body) || typeof body.mensaje !== "string") {
    throw new Error("El servidor devolvió un mensaje de retorno inválido.");
  }
  return body.mensaje;
}

export async function cancelarTramoVacio(token: string, id: number): Promise<TramoVacio> {
  const response = await solicitar(token, `/admin/tramos-vacios/${id}/cancelar`, { method: "PATCH" });
  return leerTramo(response);
}

export async function finalizarTramoVacio(token: string, id: number): Promise<TramoVacio> {
  const response = await solicitar(token, `/admin/tramos-vacios/${id}/finalizar`, { method: "PATCH" });
  return leerTramo(response);
}
