const BASE_URL: string =
  import.meta.env.VITE_API_URL ?? "http://localhost:3000/api";

export interface LanchaDestacada {
  idLancha: number;
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
      `${BASE_URL}/lanchas/destacadas?page=${page}&size=${size}`
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

export const lanchasService = { getLanchasDestacadas };
