package com.nautiflow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearResenaRequest(
        @NotNull(message = "La puntuación es obligatoria.")
        @Min(value = 1, message = "La puntuación debe estar entre 1 y 5.")
        @Max(value = 5, message = "La puntuación debe estar entre 1 y 5.")
        Integer puntuacion,
        @Size(max = 500, message = "El comentario no puede superar 500 caracteres.")
        String comentario
) {
}
