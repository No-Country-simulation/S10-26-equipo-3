package com.nautiflow.dto;

import java.time.OffsetDateTime;

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
