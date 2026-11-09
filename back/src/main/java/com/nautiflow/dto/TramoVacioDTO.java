package com.nautiflow.dto;

import java.time.OffsetDateTime;

public record TramoVacioDTO(
        Long id,
        Long idViaje,
        Long idLancha,
        String nombreEmbarcacion,
        Integer capacidad,
        String zonaOrigen,
        String zonaDestino,
        OffsetDateTime fechaHoraRetorno,
        String mensaje
) {
}
