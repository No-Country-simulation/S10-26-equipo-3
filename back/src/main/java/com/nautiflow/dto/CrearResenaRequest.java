package com.nautiflow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para registrar la reseña de un viaje: puntuación de 1 a 5 y comentario opcional.
 *
 * @param puntuacion Puntuación de la reseña, entre 1 y 5.
 * @param comentario Comentario opcional de la reseña.
 */
public record CrearResenaRequest(
        @NotNull(message = "La puntuación es obligatoria.")
        @Min(value = 1, message = "La puntuación debe estar entre 1 y 5.")
        @Max(value = 5, message = "La puntuación debe estar entre 1 y 5.")
        Integer puntuacion,
        @Size(max = 500, message = "El comentario no puede superar 500 caracteres.")
        String comentario
) {
}
