package com.nautiflow.dto;

import java.time.OffsetDateTime;

/**
 * Viaje consultado mediante su token de reseña, con los datos de la reseña si ya existe.
 *
 * @param idViaje Identificador del viaje.
 * @param nombreUsuario Nombre del usuario que realizó el viaje.
 * @param zonaOrigen Nombre de la zona de origen.
 * @param zonaDestino Nombre de la zona de destino.
 * @param fechaHoraInicio Fecha y hora de inicio del viaje.
 * @param fechaHoraFin Fecha y hora de finalización del viaje.
 * @param resenaRegistrada Indica si el viaje ya tiene una reseña registrada.
 * @param resena Datos de la reseña registrada, o null si aún no existe.
 */
public record ResenaViajeDTO(
        Long idViaje,
        String nombreUsuario,
        String zonaOrigen,
        String zonaDestino,
        OffsetDateTime fechaHoraInicio,
        OffsetDateTime fechaHoraFin,
        boolean resenaRegistrada,
        ResenaTokenDTO resena
) {
}
