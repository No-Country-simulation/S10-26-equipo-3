import { API_BASE_URL } from "../config/api";

export interface ActividadMensual {
  mes: string;
  viajes: number;
}
export interface AdminDashboardMetrics {
  solicitudesPendientes: number;
  lanchasVerificadas: number;
  viajesActivos: number;
  tramosVacios: number;
  actividadMensual: ActividadMensual[];
}
export interface ViajeSeguimiento {
  id: number;
  nombreEmbarcacion: string;
  turistaNombreCompleto: string;
  turistaWhatsapp: string;
  zonaOrigen: string;
  zonaDestino: string;
  fechaHoraInicio: string;
  fechaHoraFin: string | null;
  estado: string;
  ultimaUbicacionConfirmada: string | null;
}
export interface PaginaSeguimientoViajes {
  content: ViajeSeguimiento[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
function esRegistro(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}
function esViaje(value: unknown): value is ViajeSeguimiento {
  return esRegistro(value) && typeof value.id === "number" &&
    typeof value.nombreEmbarcacion === "string" && typeof value.zonaOrigen === "string" &&
    typeof value.turistaNombreCompleto === "string" && typeof value.turistaWhatsapp === "string" &&
    typeof value.zonaDestino === "string" && typeof value.fechaHoraInicio === "string" &&
    (typeof value.fechaHoraFin === "string" || value.fechaHoraFin === null) &&
    typeof value.estado === "string" &&
    (typeof value.ultimaUbicacionConfirmada === "string" || value.ultimaUbicacionConfirmada === null);
}
async function solicitar(token: string, path: string, signal: AbortSignal): Promise<unknown> {
  let response: Response;
  try {
    response = await fetch(API_BASE_URL + path, {
      signal,
      headers: { Accept: "application/json", Authorization: "Bearer " + token },
    });
  } catch {
    throw new Error("No se pudo conectar con el servidor.");
  }
  if (!response.ok) throw new Error("No se pudieron cargar los datos (HTTP " + response.status + ").");
  try {
    return await response.json() as unknown;
  } catch {
    throw new Error("El servidor devolvió una respuesta inválida.");
  }
}
export async function obtenerAdminDashboard(token: string, signal: AbortSignal): Promise<AdminDashboardMetrics> {
  const body = await solicitar(token, "/admin/dashboard", signal);
  if (!esRegistro(body) || typeof body.solicitudesPendientes !== "number" ||
      typeof body.lanchasVerificadas !== "number" || typeof body.viajesActivos !== "number") {
    throw new Error("El servidor devolvió métricas de Dashboard inválidas.");
  }
  const actividad = Array.isArray(body.actividadMensual)
    ? body.actividadMensual.filter((item): item is ActividadMensual =>
      esRegistro(item) && typeof item.mes === "string" && typeof item.viajes === "number")
    : [];
  return {
    solicitudesPendientes: body.solicitudesPendientes,
    lanchasVerificadas: body.lanchasVerificadas,
    viajesActivos: body.viajesActivos,
    tramosVacios: typeof body.tramosVacios === "number" ? body.tramosVacios : 0,
    actividadMensual: actividad,
  };
}
export async function listarSeguimientoViajes(
  token: string,
  page: number,
  signal: AbortSignal,
  estado = "TODOS",
): Promise<PaginaSeguimientoViajes> {
  const query = new URLSearchParams({ page: String(page), size: "10", estado });
  const body = await solicitar(token, "/admin/viajes/seguimiento?" + query, signal);
  if (!esRegistro(body) || !Array.isArray(body.content) || !body.content.every(esViaje) ||
      typeof body.page !== "number" || typeof body.size !== "number" ||
      typeof body.totalElements !== "number" || typeof body.totalPages !== "number") {
    throw new Error("El servidor devolvió un seguimiento de viajes inválido.");
  }
  return {
    content: body.content, page: body.page, size: body.size,
    totalElements: body.totalElements, totalPages: body.totalPages,
  };
}
