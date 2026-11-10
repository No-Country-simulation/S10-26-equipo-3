package com.nautiflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/**
 * Datos para publicar el retorno de una embarcación.
 */
public record CrearTramoVacioRequest(
        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaRetorno,
        @Size(max = 201)
        String turistaNombreCompleto,
        @Size(max = 20)
        String turistaWhatsapp
) {
}
