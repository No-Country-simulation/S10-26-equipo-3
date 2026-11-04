package com.nautiflow.dto;

/**
 * Resultado del rechazo de una solicitud: identificador, estado final y motivo.
 *
 * @param id Identificador del registro.
 * @param estado Estado actual del registro.
 * @param motivo Motivo del rechazo.
 */
public record RechazoSolicitudDTO(Long id, String estado, String motivo) {
}
