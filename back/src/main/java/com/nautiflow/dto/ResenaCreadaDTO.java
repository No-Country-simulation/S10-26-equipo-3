package com.nautiflow.dto;

import java.time.OffsetDateTime;

/**
 * Reseña recién registrada: identificador, viaje, puntuación, comentario y fecha.
 *
 * @param idResena Identificador de la reseña.
 * @param idViaje Identificador del viaje.
 * @param puntuacion Puntuación de la reseña, entre 1 y 5.
 * @param comentario Comentario opcional de la reseña.
 * @param fecha Fecha y hora del registro.
 */
public record ResenaCreadaDTO(
        Long idResena,
        Long idViaje,
        Integer puntuacion,
        String comentario,
        OffsetDateTime fecha
) {
}
