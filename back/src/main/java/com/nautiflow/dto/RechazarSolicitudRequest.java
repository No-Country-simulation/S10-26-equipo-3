package com.nautiflow.dto;

import jakarta.validation.constraints.Size;

public record RechazarSolicitudRequest(
        @Size(max = 500, message = "El motivo no puede superar 500 caracteres.")
        String motivo
) {
}
