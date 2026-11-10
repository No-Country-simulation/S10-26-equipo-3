package com.nautiflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Retorno vacío con los datos asociados del viaje, embarcación y lanchero.
 */
public record TramoVacioDTO(
        Long id,
        Long viajeId,
        String lanchaNombre,
        String lancheroNombre,
        String zonaOrigen,
        String zonaDestino,
        LocalDate fecha,
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaRetorno,
        String turistaNombreCompleto,
        String turistaWhatsapp,
        String estado,
        String mensaje
) {
}
