import { API_BASE_URL } from "../config/api";

export interface Zona {
  id: number;
  nombre: string;
}

export async function obtenerZonas(): Promise<Zona[]> {
  let res: Response;
  try {
    res = await fetch(`${API_BASE_URL}/zonas`, {
      headers: {
        Accept: "*/*",
      },
    });
  } catch {
    throw new Error("No se pudo conectar con el servidor para obtener las zonas.");
  }

  if (!res.ok) {
    throw new Error(`Error al obtener las zonas (código ${res.status}).`);
  }

  return (await res.json()) as Zona[];
}

export const zonasService = { obtenerZonas };

