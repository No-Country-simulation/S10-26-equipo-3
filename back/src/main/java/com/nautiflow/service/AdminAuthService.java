package com.nautiflow.service;

import com.nautiflow.dto.LoginRequest;
import com.nautiflow.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Servicio de autenticación del administrador. Valida las credenciales definidas en la aplicación y genera el token JWT.
 */
@Service
public class AdminAuthService {
    private final JwtTokenService jwtTokenService;
    private final String adminEmail;
    private final String adminPassword;

    /**
     * Inyecta el servicio que genera los tokens JWT.
     */
    public AdminAuthService(
            JwtTokenService jwtTokenService,
            @Value("${nautiflow.admin.email:}") String adminEmail,
            @Value("${nautiflow.admin.password:}") String adminPassword
    ) {
        this.jwtTokenService = jwtTokenService;
        this.adminEmail = adminEmail == null ? "" : adminEmail.trim();
        this.adminPassword = adminPassword == null ? "" : adminPassword;
    }

    /**
     * Valida las credenciales del administrador y devuelve un token JWT si son correctas.
     */
    public LoginResponse login(LoginRequest request) {
        if (adminEmail.isEmpty() || adminPassword.isEmpty()
                || !coincide(request.email(), adminEmail)
                || !coincide(request.password(), adminPassword)) {
            throw new CredencialesInvalidasException();
        }
        return new LoginResponse(
                jwtTokenService.crearTokenAdministrador(adminEmail),
                "Bearer",
                "Inicio de sesión exitoso."
        );
    }

    /**
     * Compara dos cadenas en tiempo constante para evitar filtrar información por tiempos de respuesta.
     */
    private boolean coincide(String recibido, String esperado) {
        if (recibido == null) {
            return false;
        }
        return MessageDigest.isEqual(
                recibido.getBytes(StandardCharsets.UTF_8),
                esperado.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Excepción que indica que el correo o la contraseña del administrador no son correctos.
     */
    public static class CredencialesInvalidasException extends RuntimeException {
    }
}
