package com.nautiflow.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record CrearTramoVacioRequest(
        @NotNull LocalTime horaRetorno
) {
}
