package com.nautiflow.dto;

import java.time.LocalDate;

/**
 * Resumen de una solicitud de registro para el listado administrativo.
 *
 * @param id Identificador del registro.
 * @param nombreCompleto Nombre completo de la persona.
 * @param fechaRegistro Fecha y hora en que se registró la solicitud.
 * @param estado Estado actual del registro.
 */
public record SolicitudResumenDTO(Long id, String nombreCompleto, LocalDate fechaRegistro, String estado) {
}
