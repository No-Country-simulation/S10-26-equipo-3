package com.nautiflow.dto;

import java.time.OffsetDateTime;

/**
 * Resultado de finalizar un viaje: fecha de fin, token y datos para pedir la reseña al turista.
 *
 * @param id Identificador del registro.
 * @param fechaHoraFin Fecha y hora de finalización del viaje.
 * @param tokenResena Token único para registrar la reseña del viaje.
 * @param nombreUsuario Nombre del usuario que realizó el viaje.
 * @param whatsappUsuario Número de WhatsApp del turista.
 * @param mensajeResena Texto sugerido para pedir la reseña al turista.
 */
public record ViajeFinalizadoDTO(
        Long id,
        OffsetDateTime fechaHoraFin,
        String tokenResena,
        String nombreUsuario,
        String whatsappUsuario,
        String mensajeResena
) {
}
