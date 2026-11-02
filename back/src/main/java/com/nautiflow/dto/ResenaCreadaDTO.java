package com.nautiflow.dto;

import java.time.OffsetDateTime;

public record ResenaCreadaDTO(
        Long idResena,
        Long idViaje,
        Integer puntuacion,
        String comentario,
        OffsetDateTime fecha
) {
}
