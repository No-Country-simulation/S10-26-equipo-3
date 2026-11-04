package com.nautiflow.dto;

/**
 * Respuesta del registro de un lanchero: identificadores creados, estado y mensaje.
 *
 * @param idLanchero Identificador del lanchero.
 * @param idLancha Identificador de la lancha.
 * @param estado Estado actual del registro.
 * @param mensaje Mensaje descriptivo del resultado.
 */
public record RegistroLancheroResponse(
        Long idLanchero,
        Long idLancha,
        String estado,
        String mensaje
) {
}
