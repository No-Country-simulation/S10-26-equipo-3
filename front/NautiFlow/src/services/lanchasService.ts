import { API_BASE_URL } from "../config/api";

/** Una reseña individual devuelta por el backend */
export interface ResenaLancha {
  idResena: number;
  puntuacion: number;
  comentario: string;
  fecha: string;
}

export interface ResenasResponse {
  content: ResenaLancha[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface LanchaDestacada {
  idLancha: number;
  /** ID del lanchero dueño de la embarcación (para consultar sus reseñas) */
  idLanchero?: number;
  nombreEmbarcacion: string;
  lancheroNombreCompleto: string;
  fotoPrincipalUrl: string;
  promedioCalificacion: number;
  totalResenas: number;
  verificada: boolean;
}

export interface LanchasDestacadasResponse {
  content: LanchaDestacada[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}


async function getLanchasDestacadas(
  page = 0,
  size = 10
): Promise<LanchasDestacadasResponse> {
  let res: Response;
  try {
    res = await fetch(
      `${API_BASE_URL}/lanchas/destacadas?page=${page}&size=${size}`
    );
  } catch {
    throw new Error("No se pudo conectar con el servidor.");
  }

  if (!res.ok) {
    if (res.status === 404) {
      throw new Error("No se encontraron lanchas destacadas.");
    }

    let message = `Error al obtener lanchas destacadas (código ${res.status}).`;
    try {
      const data = (await res.json()) as { message?: string; error?: string; detail?: string };
      message = data.message || data.error || data.detail || message;
    } catch {
      /* La respuesta no contenía un JSON válido */
    }
    throw new Error(message);
  }

  return (await res.json()) as LanchasDestacadasResponse;
}

/** Obtiene las reseñas de un lanchero por su ID.
 *  GET /lancheros/:idLanchero/resenas?page=&size=
 */
async function getResenasByLanchero(
  idLanchero: number,
  page = 0,
  size = 10
): Promise<ResenasResponse> {
  let res: Response;
  try {
    res = await fetch(
      `${API_BASE_URL}/lancheros/${idLanchero}/resenas?page=${page}&size=${size}`
    );
  } catch {
    throw new Error("No se pudo conectar con el servidor.");
  }

  if (!res.ok) {
    if (res.status === 404) {
      throw new Error("No se encontraron reseñas para este lanchero.");
    }

    let message = `Error al obtener reseñas (código ${res.status}).`;
    try {
      const data = (await res.json()) as { message?: string; error?: string; detail?: string };
      message = data.message || data.error || data.detail || message;
    } catch {
      /* La respuesta no contenía un JSON válido */
    }
    throw new Error(message);
  }

  return (await res.json()) as ResenasResponse;
}

export const lanchasService = { getLanchasDestacadas, getResenasByLanchero };
