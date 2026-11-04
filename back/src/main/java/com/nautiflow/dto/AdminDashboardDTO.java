package com.nautiflow.dto;

/**
 * Resumen de métricas del panel administrativo: solicitudes pendientes, lanchas verificadas y viajes activos.
 *
 * @param solicitudesPendientes Cantidad de solicitudes pendientes de revisión.
 * @param lanchasVerificadas Cantidad de lanchas verificadas.
 * @param viajesActivos Cantidad de viajes activos.
 */
public record AdminDashboardDTO(
        long solicitudesPendientes,
        long lanchasVerificadas,
        long viajesActivos
) {
}
