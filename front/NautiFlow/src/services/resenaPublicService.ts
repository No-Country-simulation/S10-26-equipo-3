const API_BASE_URL = (
  import.meta.env.VITE_API_URL ?? "http://localhost:8080"
).replace(/\/+$/, "");

export interface ResenaViajePublico {
  idViaje: number;
  nombreUsuario: string;
  zonaOrigen: string;
  zonaDestino: string;
  fechaHoraInicio: string;
  fechaHoraFin: string;
  resenaRegistrada: boolean;
  resena?: {
    puntuacion: number;
    comentario?: string;
  } | null;
}

export interface CrearResenaDTO {
  puntuacion: number;
  comentario?: string;
}

export interface ResenaCreadaRespuesta {
  idResena: number;
  idViaje: number;
  puntuacion: number;
  comentario: string | null;
  fecha: string;
}

export class HttpError extends Error {
  readonly status: number;
  constructor(status: number, message: string) {
    super(message);
    this.name = "HttpError";
    this.status = status;
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

async function extraerError(response: Response): Promise<HttpError> {
  let message = `Error al procesar la solicitud (HTTP ${response.status}).`;
  try {
    const body: unknown = await response.json();
    if (isRecord(body)) {
      if (typeof body.message === "string") message = body.message;
      else if (typeof body.mensaje === "string") message = body.mensaje;
      else if (typeof body.detail === "string") message = body.detail;
    }
  } catch {
    // conserva mensaje por defecto
  }
  return new HttpError(response.status, message);
}

export async function consultarResenaPorToken(
  token: string,
  signal?: AbortSignal,
): Promise<ResenaViajePublico> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/resena/${encodeURIComponent(token)}`, {
      method: "GET",
      signal,
      headers: {
        Accept: "application/json",
      },
    });
  } catch (err: unknown) {
    if (signal?.aborted) throw err;
    throw new Error("No se pudo conectar con el servidor.");
  }

  if (!response.ok) {
    throw await extraerError(response);
  }

  let body: unknown;
  try {
    body = await response.json();
  } catch {
    throw new Error("El servidor devolvió una respuesta inválida.");
  }

  if (!isRecord(body) || typeof body.resenaRegistrada !== "boolean") {
    throw new Error("Formato de respuesta de reseña inválido.");
  }

  return body as unknown as ResenaViajePublico;
}

export async function enviarCalificacionResena(
  token: string,
  solicitud: CrearResenaDTO,
): Promise<ResenaCreadaRespuesta> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/resena/${encodeURIComponent(token)}`, {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      body: JSON.stringify(solicitud),
    });
  } catch {
    throw new Error("No se pudo conectar con el servidor.");
  }

  if (!response.ok) {
    throw await extraerError(response);
  }

  let body: unknown;
  try {
    body = await response.json();
  } catch {
    throw new Error("El servidor devolvió una respuesta inválida.");
  }

  if (!isRecord(body) || typeof body.idResena !== "number" || typeof body.fecha !== "string") {
    throw new Error("El servidor devolvió un formato inesperado de reseña creada.");
  }

  return {
    idResena: Number(body.idResena),
    idViaje: Number(body.idViaje),
    puntuacion: Number(body.puntuacion),
    comentario: typeof body.comentario === "string" ? body.comentario : null,
    fecha: String(body.fecha),
  };
}

