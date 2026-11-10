package com.nautiflow.dto;

/**
 * Cantidad de viajes finalizados durante un mes calendario.
 *
 * @param anio Año del mes.
 * @param mes Mes del año, del 1 al 12.
 * @param cantidad Cantidad de viajes finalizados.
 */
public record ViajesFinalizadosPorMesDTO(int anio, int mes, long cantidad) {
}
