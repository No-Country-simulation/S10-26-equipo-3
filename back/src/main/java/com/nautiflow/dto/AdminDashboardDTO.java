package com.nautiflow.dto;

public record AdminDashboardDTO(
        long solicitudesPendientes,
        long lanchasVerificadas,
        long viajesActivos
) {
}
