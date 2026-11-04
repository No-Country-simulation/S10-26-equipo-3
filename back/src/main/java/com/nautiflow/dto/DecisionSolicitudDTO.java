package com.nautiflow.dto;

/**
 * Resultado de aprobar o rechazar una solicitud: identificador, estado final y mensaje.
 *
 * @param id Identificador del registro.
 * @param estado Estado actual del registro.
 * @param mensaje Mensaje descriptivo del resultado.
 */
public record DecisionSolicitudDTO(Long id, String estado, String mensaje) {
}
