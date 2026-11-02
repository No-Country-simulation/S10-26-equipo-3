package com.nautiflow.dto;

import java.time.OffsetDateTime;

public record ViajeFinalizadoDTO(
        Long id,
        OffsetDateTime fechaHoraFin,
        String tokenResena,
        String nombreUsuario,
        String whatsappUsuario,
        String mensajeResena
) {
}
