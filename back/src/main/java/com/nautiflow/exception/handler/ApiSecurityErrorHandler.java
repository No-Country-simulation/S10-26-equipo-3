package com.nautiflow.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nautiflow.exception.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Responde en formato JSON los errores de autenticación (401) y de permisos (403) generados por Spring Security.
 */
public class ApiSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    /**
     * Crea el manejador con el ObjectMapper usado para escribir respuestas JSON.
     */
    public ApiSecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Responde 401 cuando falta autenticación o el token es inválido.
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.AuthenticationException exception
    ) throws IOException, ServletException {
        escribirError(
                request,
                response,
                HttpStatus.UNAUTHORIZED,
                "Se requiere autenticación válida para acceder a este recurso."
        );
    }

    /**
     * Responde 403 cuando el usuario autenticado no tiene permisos.
     */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException, ServletException {
        escribirError(
                request,
                response,
                HttpStatus.FORBIDDEN,
                "No tienes permisos para realizar esta operación."
        );
    }

    /**
     * Escribe una respuesta de error JSON con el estado y el mensaje indicados.
     */
    private void escribirError(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(
                response.getOutputStream(),
                ApiErrorResponse.of(status, message, request.getRequestURI())
        );
    }
}
