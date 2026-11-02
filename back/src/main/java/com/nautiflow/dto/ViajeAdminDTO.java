package com.nautiflow.dto;

import java.time.OffsetDateTime;

public record ViajeAdminDTO(
        Long id,
        Long idLancha,
        Long idLanchero,
        String lancheroNombreCompleto,
        String zonaOrigen,
        String zonaDestino,
        OffsetDateTime fechaHoraInicio,
        OffsetDateTime fechaHoraFin,
        Integer cantidadPasajeros,
        String turistaNombreCompleto,
        String turistaWhatsapp
) {
}
