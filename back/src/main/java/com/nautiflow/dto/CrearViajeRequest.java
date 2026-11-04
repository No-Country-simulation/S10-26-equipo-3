package com.nautiflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear un viaje: embarcación, zonas de origen y destino, cantidad de pasajeros y datos del turista.
 *
 * @param idLancha Identificador de la lancha.
 * @param idZonaOrigen Identificador de la zona de origen.
 * @param idZonaDestino Identificador de la zona de destino.
 * @param cantidadPasajeros Cantidad de pasajeros del viaje.
 * @param turistaNombreCompleto Nombre completo del turista.
 * @param turistaWhatsapp Número de WhatsApp del turista.
 */
public record CrearViajeRequest(
        @NotNull @Positive Long idLancha,
        @NotNull @Positive Long idZonaOrigen,
        @NotNull @Positive Long idZonaDestino,
        @NotNull @Positive Integer cantidadPasajeros,
        @NotBlank @Size(max = 201) String turistaNombreCompleto,
        @NotBlank @Size(max = 20) String turistaWhatsapp
) {
}
