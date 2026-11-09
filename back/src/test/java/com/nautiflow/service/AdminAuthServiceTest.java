package com.nautiflow.service;

import com.nautiflow.dto.LoginRequest;
import com.nautiflow.service.AdminAuthService.CredencialesInvalidasException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminAuthServiceTest {
    private AdminAuthService adminAuthService;
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
        adminAuthService = new AdminAuthService(jwtTokenService);
    }

    @Test
    void loginAceptaCredencialesAdministrativasYEmiteToken() {
        var response = adminAuthService.login(new LoginRequest("admin@nautiflow.com", "Admin123*"));

        assertEquals("Bearer", response.tipo());
        assertEquals("Inicio de sesión exitoso.", response.mensaje());
        assertEquals("admin@nautiflow.com",
                jwtTokenService.validarToken(response.token()).getSubject());
    }

    @Test
    void loginRechazaCredencialesIncorrectas() {
        assertThrows(CredencialesInvalidasException.class, () ->
                adminAuthService.login(new LoginRequest("admin@nautiflow.com", "incorrecta")));
        assertThrows(CredencialesInvalidasException.class, () ->
                adminAuthService.login(new LoginRequest("otro@example.com", "Admin123*")));
    }
}
