package com.nautiflow.dto;

import java.time.OffsetDateTime;

public record LancheroAdminDTO(
        Long id,
        String nombreCompleto,
        OffsetDateTime fechaRegistro,
        String estadoSolicitud,
        String estadoViaje
) {
}
