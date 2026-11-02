package com.nautiflow.dto;

import java.time.OffsetDateTime;

public record ViajeSeguimientoDTO(
        Long id,
        String nombreEmbarcacion,
        String zonaOrigen,
        String zonaDestino,
        OffsetDateTime fechaHoraInicio,
        OffsetDateTime fechaHoraFin,
        String estado,
        String ultimaUbicacionConfirmada
) {
}
