package com.nautiflow.dto;

import java.time.OffsetDateTime;

/**
 * Reseña con los datos del viaje y del lanchero para el panel administrativo.
 *
 * @param idResena Identificador de la reseña.
 * @param idViaje Identificador del viaje.
 * @param puntuacion Puntuación de la reseña, entre 1 y 5.
 * @param comentario Comentario opcional de la reseña.
 * @param idLanchero Identificador del lanchero.
 * @param nombreLanchero Nombre completo del lanchero.
 * @param fecha Fecha y hora del registro.
 * @param turistaNombreCompleto Nombre completo del turista.
 * @param zonaOrigen Nombre de la zona de origen.
 * @param zonaDestino Nombre de la zona de destino.
 * @param fechaHoraInicio Fecha y hora de inicio del viaje.
 * @param fechaHoraFin Fecha y hora de finalización del viaje.
 */
public record ResenaAdminDTO(
        Long idResena,
        Long idViaje,
        Integer puntuacion,
        String comentario,
        Long idLanchero,
        String nombreLanchero,
        OffsetDateTime fecha,
        String turistaNombreCompleto,
        String zonaOrigen,
        String zonaDestino,
        OffsetDateTime fechaHoraInicio,
        OffsetDateTime fechaHoraFin
) {
}
