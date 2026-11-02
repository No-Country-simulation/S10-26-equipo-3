package com.nautiflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RechazarSolicitudRequest(
        @NotBlank(message = "El motivo de rechazo es obligatorio.")
        @Size(max = 500, message = "El motivo no puede superar 500 caracteres.")
        String motivo
) {
}
