package com.nautiflow.dto;

import java.time.OffsetDateTime;

public record ResenaViajeDTO(
        Long idViaje,
        String nombreUsuario,
        String zonaOrigen,
        String zonaDestino,
        OffsetDateTime fechaHoraInicio,
        OffsetDateTime fechaHoraFin,
        boolean resenaRegistrada
) {
}
