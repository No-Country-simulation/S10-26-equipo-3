package com.nautiflow.controller;

import com.nautiflow.dto.LoginRequest;
import com.nautiflow.dto.LoginResponse;
import com.nautiflow.service.AdminAuthService;
import com.nautiflow.service.AdminAuthService.CredencialesInvalidasException;
import com.nautiflow.service.LoginAttemptService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
    private final LoginAttemptService loginAttemptService;

    /**
     * Inyecta el servicio de autenticación y el control de intentos fallidos.
     *
     * @param adminAuthService servicio que valida las credenciales y emite el token.
     * @param loginAttemptService servicio que limita los intentos fallidos por IP.
     */
    public LoginController(AdminAuthService adminAuthService, LoginAttemptService loginAttemptService) {
        this.adminAuthService = adminAuthService;
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Valida las credenciales, controla los intentos fallidos por IP y devuelve el token JWT del administrador.
     *
     * @param request credenciales enviadas por el administrador.
     * @param httpRequest petición HTTP de la que se obtiene la dirección del cliente.
     * @return token JWT, tipo de autenticación y mensaje de éxito.
     * @throws CredencialesInvalidasException si el correo o la contraseña no son correctos.
     * @throws com.nautiflow.exception.DemasiadosIntentosLoginException si la IP está bloqueada por demasiados fallos.
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String direccionIp = obtenerDireccionIp(httpRequest);
        loginAttemptService.validarPermitido(direccionIp);
        try {
            LoginResponse respuesta = adminAuthService.login(request);
            loginAttemptService.registrarExito(direccionIp);
            return respuesta;
        } catch (CredencialesInvalidasException exception) {
            loginAttemptService.registrarFallo(direccionIp);
            throw exception;
        }
    }

    /**
     * Obtiene la dirección IP del cliente; si no está disponible, usa un valor fijo para no perder el control.
     *
     * @param request petición HTTP actual.
     * @return dirección IP del cliente o "desconocida".
     */
    private String obtenerDireccionIp(HttpServletRequest request) {
        String direccionIp = request.getRemoteAddr();
        return direccionIp == null || direccionIp.isBlank() ? "desconocida" : direccionIp;
    }
}
