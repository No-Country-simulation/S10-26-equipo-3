package com.nautiflow.service;

import java.time.OffsetDateTime;

public record ViajeProgramadoEvent(
        Long idViaje,
        Long idLanchero,
        String correoLanchero,
        String nombreLanchero,
        String zonaOrigen,
        String zonaDestino,
        OffsetDateTime fechaHoraInicio,
        Integer cantidadPasajeros,
        String turistaNombreCompleto,
        String turistaWhatsapp
) {
}
