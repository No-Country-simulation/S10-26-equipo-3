package com.nautiflow.dto;

import java.time.OffsetDateTime;

/**
 * Estado de seguimiento de un viaje: embarcación, ruta, fechas, estado y última ubicación confirmada.
 *
 * @param id Identificador del registro.
 * @param nombreEmbarcacion Nombre de la embarcación.
 * @param turistaNombreCompleto Nombre completo del turista registrado.
 * @param turistaWhatsapp WhatsApp del turista registrado.
 * @param zonaOrigen Nombre de la zona de origen.
 * @param zonaDestino Nombre de la zona de destino.
 * @param fechaHoraInicio Fecha y hora de inicio del viaje.
 * @param fechaHoraFin Fecha y hora de finalización del viaje.
 * @param estado Estado actual del registro.
 * @param ultimaUbicacionConfirmada Última zona de destino confirmada para la embarcación.
 */
public record ViajeSeguimientoDTO(
        Long id,
        String nombreEmbarcacion,
        String turistaNombreCompleto,
        String turistaWhatsapp,
        String zonaOrigen,
        String zonaDestino,
        OffsetDateTime fechaHoraInicio,
        OffsetDateTime fechaHoraFin,
        String estado,
        String ultimaUbicacionConfirmada
) {
}
