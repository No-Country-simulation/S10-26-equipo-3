package com.nautiflow.dto;

import java.time.OffsetDateTime;

/**
 * Reseña asociada a un viaje consultado por token: puntuación, comentario, lanchero y fecha.
 *
 * @param idResena Identificador de la reseña.
 * @param puntuacion Puntuación de la reseña, entre 1 y 5.
 * @param comentario Comentario opcional de la reseña.
 * @param idLanchero Identificador del lanchero.
 * @param nombreLanchero Nombre completo del lanchero.
 * @param fecha Fecha y hora del registro.
 */
public record ResenaTokenDTO(
        Long idResena,
        Integer puntuacion,
        String comentario,
        Long idLanchero,
        String nombreLanchero,
        OffsetDateTime fecha
) {
}
