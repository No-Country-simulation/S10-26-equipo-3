package com.nautiflow.dto;

import jakarta.validation.constraints.Size;

/**
 * Motivo opcional con el que se rechaza una solicitud de registro.
 *
 * @param motivo Motivo del rechazo.
 */
public record RechazarSolicitudRequest(
        @Size(max = 500, message = "El motivo no puede superar 500 caracteres.")
        String motivo
) {
}
