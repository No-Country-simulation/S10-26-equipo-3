package com.nautiflow.service;

import com.nautiflow.dto.LoginRequest;
import com.nautiflow.service.AdminAuthService.CredencialesInvalidasException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminAuthServiceTest {
    private static final String EMAIL = "admin@nautiflow.test";
    private static final String PASSWORD = "clave-de-prueba";

    private AdminAuthService adminAuthService;
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
        adminAuthService = new AdminAuthService(jwtTokenService, EMAIL, PASSWORD);
    }

    @Test
    void loginAceptaCredencialesAdministrativasYEmiteToken() {
        var response = adminAuthService.login(new LoginRequest(EMAIL, PASSWORD));

        assertEquals("Bearer", response.tipo());
        assertEquals("Inicio de sesión exitoso.", response.mensaje());
        assertEquals(EMAIL, jwtTokenService.validarToken(response.token()).getSubject());
    }

    @Test
    void loginRechazaCredencialesIncorrectas() {
        assertThrows(CredencialesInvalidasException.class, () ->
                adminAuthService.login(new LoginRequest(EMAIL, "incorrecta")));
        assertThrows(CredencialesInvalidasException.class, () ->
                adminAuthService.login(new LoginRequest("otro@example.com", PASSWORD)));
    }

    @Test
    void loginRechazaTodoSiLasCredencialesNoEstanConfiguradas() {
        var sinPassword = new AdminAuthService(jwtTokenService, EMAIL, "");
        var sinEmail = new AdminAuthService(jwtTokenService, " ", PASSWORD);
        var sinNada = new AdminAuthService(jwtTokenService, null, null);

        assertThrows(CredencialesInvalidasException.class, () ->
                sinPassword.login(new LoginRequest(EMAIL, "")));
        assertThrows(CredencialesInvalidasException.class, () ->
                sinEmail.login(new LoginRequest("", PASSWORD)));
        assertThrows(CredencialesInvalidasException.class, () ->
                sinNada.login(new LoginRequest("", "")));
    }
}
