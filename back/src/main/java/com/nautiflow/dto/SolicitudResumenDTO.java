package com.nautiflow.dto;

import java.time.LocalDate;

public record SolicitudResumenDTO(Long id, String nombreCompleto, LocalDate fechaRegistro, String estado) {
}
