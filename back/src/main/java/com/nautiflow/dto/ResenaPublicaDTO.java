package com.nautiflow.dto;

import java.time.OffsetDateTime;

public record ResenaPublicaDTO(
        Long idResena,
        Integer puntuacion,
        String comentario,
        OffsetDateTime fecha
) {
}
