const API_BASE_URL = (
  import.meta.env.VITE_API_URL ?? "http://localhost:8080"
).replace(/\/+$/, "");

export interface SolicitudResumen {
  id: number;
  nombreCompleto: string;
  fechaRegistro: string;
  estado: string;
  fechaDecision?: string | null;
}

export interface DocumentoSolicitud {
  tipo: string;
  url: string;
}

export interface EmbarcacionSolicitudDetalle {
  id: number;
  nombre: string;
  capacidadPasajeros: number | null;
  zonas: string[];
}

export interface SolicitudDetalle {
  id: number;
  nombreCompleto: string;
  correo: string | null;
  whatsapp: string | null;
  estado: string;
  fechaRegistro: string;
  fechaDecision: string | null;
  motivoRechazo: string | null;
  embarcacion: EmbarcacionSolicitudDetalle | null;
  documentos: DocumentoSolicitud[];
}

export interface PaginaSolicitudes {
  content: SolicitudResumen[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export type FiltroEstadoSolicitud =
  | "PENDIENTE"
  | "APROBADO"
  | "RECHAZADO"
  | "TODOS";

interface DecisionSolicitud {
  id: number;
  estado: "APROBADO" | "RECHAZADO";
  mensaje?: string;
  motivo?: string | null;
}

export class SolicitudAdminApiError extends Error {
  readonly status: number | null;

  constructor(message: string, status: number | null) {
    super(message);
    this.name = "SolicitudAdminApiError";
    this.status = status;
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function esTextoNullable(value: unknown): value is string | null {
  return value === null || typeof value === "string";
}

async function leerJson(response: Response): Promise<unknown> {
  try {
    return await response.json();
  } catch {
    throw new SolicitudAdminApiError(
      "El servidor devolvió una respuesta inválida.",
      response.status,
    );
  }
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
    return await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers,
    });
  } catch {
    throw new SolicitudAdminApiError("No se pudo conectar con el servidor.", null);
  }
}

async function lanzarErrorRespuesta(response: Response): Promise<never> {
  let message = `No se pudo completar la operación (HTTP ${response.status}).`;
  try {
    const body = await response.json() as unknown;
    if (isRecord(body) && typeof body.message === "string") message = body.message;
    else if (isRecord(body) && typeof body.mensaje === "string") message = body.mensaje;
  } catch {
    // Conserva el error HTTP cuando el backend no responde JSON.
  }
  throw new SolicitudAdminApiError(message, response.status);
}

function esSolicitudResumen(value: unknown): value is SolicitudResumen {
  return isRecord(value) &&
    typeof value.id === "number" &&
    typeof value.nombreCompleto === "string" &&
    typeof value.fechaRegistro === "string" &&
    typeof value.estado === "string";
}

function esDocumentoSolicitud(value: unknown): value is DocumentoSolicitud {
  return isRecord(value) &&
    typeof value.tipo === "string" &&
    typeof value.url === "string";
}

function esEmbarcacionSolicitudDetalle(
  value: unknown,
): value is EmbarcacionSolicitudDetalle | null {
  return value === null || (
    isRecord(value) &&
    typeof value.id === "number" &&
    typeof value.nombre === "string" &&
    (typeof value.capacidadPasajeros === "number" || value.capacidadPasajeros === null) &&
    Array.isArray(value.zonas) &&
    value.zonas.every((zona) => typeof zona === "string")
  );
}

function esSolicitudDetalle(value: unknown): value is SolicitudDetalle {
  return isRecord(value) &&
    typeof value.id === "number" &&
    typeof value.nombreCompleto === "string" &&
    esTextoNullable(value.correo) &&
    esTextoNullable(value.whatsapp) &&
    typeof value.estado === "string" &&
    typeof value.fechaRegistro === "string" &&
    esTextoNullable(value.fechaDecision) &&
    esTextoNullable(value.motivoRechazo) &&
    esEmbarcacionSolicitudDetalle(value.embarcacion) &&
    Array.isArray(value.documentos) &&
    value.documentos.every(esDocumentoSolicitud);
}

export async function listarSolicitudes(
  token: string,
  page: number,
  estado: FiltroEstadoSolicitud,
  signal: AbortSignal,
): Promise<PaginaSolicitudes> {
  const query = new URLSearchParams({
    estado,
    page: String(page),
    size: "10",
  });
  const response = await solicitar(token, `/admin/solicitudes?${query}`, { signal });
  if (!response.ok) return lanzarErrorRespuesta(response);

  const body = await leerJson(response);
  if (
    !isRecord(body) ||
    !Array.isArray(body.content) ||
    !body.content.every(esSolicitudResumen) ||
    typeof body.page !== "number" ||
    typeof body.size !== "number" ||
    typeof body.totalElements !== "number" ||
    typeof body.totalPages !== "number"
  ) {
    throw new SolicitudAdminApiError("El servidor devolvió una lista de solicitudes inválida.", response.status);
  }

  const content = await Promise.all(body.content.map(async (solicitud) => {
    if (solicitud.estado !== "APROBADO" && solicitud.estado !== "RECHAZADO") {
      return { ...solicitud, fechaDecision: null };
    }

    try {
      const detalle = await obtenerSolicitudDetalle(token, solicitud.id, signal);
      return { ...solicitud, fechaDecision: detalle.fechaDecision };
    } catch (error) {
      if (signal.aborted) throw error;
      return { ...solicitud, fechaDecision: null };
    }
  }));

  return {
    content,
    page: body.page,
    size: body.size,
    totalElements: body.totalElements,
    totalPages: body.totalPages,
  };
}

export async function obtenerSolicitudDetalle(
  token: string,
  id: number,
  signal: AbortSignal,
): Promise<SolicitudDetalle> {
  const response = await solicitar(token, `/admin/solicitudes/${id}`, { signal });
  if (!response.ok) return lanzarErrorRespuesta(response);

  const body = await leerJson(response);
  if (!esSolicitudDetalle(body) || body.id !== id) {
    throw new SolicitudAdminApiError(
      "El servidor devolvió un detalle de solicitud inválido.",
      response.status,
    );
  }

  return body;
}

export function listarSolicitudesPendientes(
  token: string,
  page: number,
  signal: AbortSignal,
): Promise<PaginaSolicitudes> {
  return listarSolicitudes(token, page, "PENDIENTE", signal);
}

async function enviarDecision(
  token: string,
  id: number,
  accion: "aprobar" | "rechazar",
  motivo?: string,
): Promise<DecisionSolicitud> {
  const response = await solicitar(token, `/admin/solicitudes/${id}/${accion}`, {
    method: "PATCH",
    ...(accion === "rechazar"
      ? {
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ motivo }),
        }
      : {}),
  });
  if (!response.ok) return lanzarErrorRespuesta(response);
  if (response.status !== 200) {
    throw new SolicitudAdminApiError(
      `El servidor respondió con HTTP ${response.status}; se esperaba 200.`,
      response.status,
    );
  }

  const body = await leerJson(response);
  const estadoEsperado = accion === "aprobar" ? "APROBADO" : "RECHAZADO";
  if (
    !isRecord(body) ||
    typeof body.id !== "number" ||
    body.id !== id ||
    body.estado !== estadoEsperado
  ) {
    throw new SolicitudAdminApiError("El servidor devolvió una confirmación inválida.", response.status);
  }

  return {
    id: body.id,
    estado: estadoEsperado,
    ...(typeof body.mensaje === "string" ? { mensaje: body.mensaje } : {}),
    ...(typeof body.motivo === "string" || body.motivo === null ? { motivo: body.motivo } : {}),
  };
}

export function aprobarSolicitud(token: string, id: number): Promise<DecisionSolicitud> {
  return enviarDecision(token, id, "aprobar");
}

export function rechazarSolicitud(
  token: string,
  id: number,
  motivo: string,
): Promise<DecisionSolicitud> {
  const motivoNormalizado = motivo.trim();
  if (!motivoNormalizado) {
    throw new Error("El motivo del rechazo es obligatorio.");
  }
  if (motivoNormalizado.length > 500) {
    throw new Error("El motivo no puede superar 500 caracteres.");
  }
  return enviarDecision(token, id, "rechazar", motivoNormalizado);
}
