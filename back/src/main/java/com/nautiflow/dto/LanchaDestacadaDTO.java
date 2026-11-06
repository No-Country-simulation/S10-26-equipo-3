package com.nautiflow.dto;

public record LanchaDestacadaDTO(
        Long idLancha,
        String nombreEmbarcacion,
        String lancheroNombreCompleto,
        String fotoPrincipalUrl,
        Double promedioCalificacion,
        long totalResenas,
        boolean verificada
) {
}
