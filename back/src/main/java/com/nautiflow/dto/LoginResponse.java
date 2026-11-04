package com.nautiflow.dto;

/**
 * Respuesta del inicio de sesión: token JWT, tipo de autenticación y mensaje.
 *
 * @param token Token de autenticación o del enlace público.
 * @param tipo Tipo o categoría del dato.
 * @param mensaje Mensaje descriptivo del resultado.
 */
public record LoginResponse(String token, String tipo, String mensaje) {
}
