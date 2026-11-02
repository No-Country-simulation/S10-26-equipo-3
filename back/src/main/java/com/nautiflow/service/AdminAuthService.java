package com.nautiflow.service;

import com.nautiflow.dto.LoginRequest;
import com.nautiflow.dto.LoginResponse;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class AdminAuthService {
    private static final String ADMIN_EMAIL = "admin@nautiflow.com";
    private static final String ADMIN_PASSWORD = "Admin123*";

    private final JwtTokenService jwtTokenService;

    public AdminAuthService(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public LoginResponse login(LoginRequest request) {
        if (!coincide(request.email(), ADMIN_EMAIL) || !coincide(request.password(), ADMIN_PASSWORD)) {
            throw new CredencialesInvalidasException();
        }
        return new LoginResponse(
                jwtTokenService.crearTokenAdministrador(ADMIN_EMAIL),
                "Bearer",
                "Inicio de sesión exitoso."
        );
    }

    private boolean coincide(String recibido, String esperado) {
        if (recibido == null) {
            return false;
        }
        return MessageDigest.isEqual(
                recibido.getBytes(StandardCharsets.UTF_8),
                esperado.getBytes(StandardCharsets.UTF_8)
        );
    }

    public static class CredencialesInvalidasException extends RuntimeException {
    }
}
