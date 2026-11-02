package com.nautiflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearViajeRequest(
        @NotNull @Positive Long idLancha,
        @NotNull @Positive Long idZonaOrigen,
        @NotNull @Positive Long idZonaDestino,
        @NotNull @Positive Integer cantidadPasajeros,
        @NotBlank @Size(max = 201) String turistaNombreCompleto,
        @NotBlank @Size(max = 20) String turistaWhatsapp
) {
}
