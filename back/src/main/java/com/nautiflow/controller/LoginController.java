package com.nautiflow.controller;

import com.nautiflow.dto.LoginRequest;
import com.nautiflow.dto.LoginResponse;
import com.nautiflow.service.AdminAuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone el inicio de sesión del administrador y entrega el token JWT.
 */
@RestController
@Tag(name = "Autenticación")
public class LoginController {
    private final AdminAuthService adminAuthService;

    /**
     * Inyecta el servicio de autenticación del administrador.
     */
    public LoginController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * Valida las credenciales y devuelve el token JWT del administrador.
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return adminAuthService.login(request);
    }

}
