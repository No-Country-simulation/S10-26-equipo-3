package com.nautiflow.controller;

import com.nautiflow.dto.LogoutResponse;
import com.nautiflow.service.JwtTokenService;
import com.nautiflow.service.JwtTokenRevocationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
public class LogoutController {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final JwtTokenRevocationService revocationService;

    public LogoutController(
            JwtTokenService jwtTokenService,
            JwtTokenRevocationService revocationService
    ) {
        this.jwtTokenService = jwtTokenService;
        this.revocationService = revocationService;
    }

    @PostMapping("/logout")
    public LogoutResponse logout(HttpServletRequest request, Authentication authentication) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Se requiere autenticación válida.");
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        try {
            Claims claims = jwtTokenService.validarToken(token);
            Date expiration = claims.getExpiration();
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || !authentication.getName().equals(claims.getSubject())
                    || expiration == null
                    || !revocationService.revocar(token, expiration.toInstant())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El token JWT no es válido.");
            }
        } catch (JwtException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El token JWT no es válido.");
        }

        return new LogoutResponse("Sesión cerrada correctamente");
    }
}
