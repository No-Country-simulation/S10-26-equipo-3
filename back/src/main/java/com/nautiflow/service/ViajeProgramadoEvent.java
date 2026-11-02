package com.nautiflow.service;

public record ViajeProgramadoEvent(
        Long idViaje,
        Long idLanchero,
        String correoLanchero,
        String nombreLanchero,
        String zonaOrigen,
        String zonaDestino,
        String fechaHoraInicio,
        Integer cantidadPasajeros,
        String turistaNombreCompleto,
        String turistaWhatsapp
) {
}
