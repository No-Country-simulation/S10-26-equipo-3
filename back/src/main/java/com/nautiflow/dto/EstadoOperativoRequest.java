package com.nautiflow.dto;

import jakarta.validation.constraints.NotNull;

public record EstadoOperativoRequest(@NotNull EstadoOperativo estadoViaje) {
    public enum EstadoOperativo {
        ACTIVO,
        INACTIVO
    }
}
