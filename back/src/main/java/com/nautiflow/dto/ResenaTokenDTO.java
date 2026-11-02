package com.nautiflow.dto;

import java.time.OffsetDateTime;

public record ResenaTokenDTO(
        Long idResena,
        Integer puntuacion,
        String comentario,
        Long idLanchero,
        String nombreLanchero,
        OffsetDateTime fecha
) {
}
