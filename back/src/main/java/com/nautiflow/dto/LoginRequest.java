package com.nautiflow.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales de inicio de sesión del administrador.
 *
 * @param email Correo electrónico del administrador.
 * @param password Contraseña del administrador.
 */
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password
) {
}
