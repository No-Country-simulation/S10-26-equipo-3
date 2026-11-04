package com.nautiflow.exception;

import org.springframework.http.HttpStatus;
import com.nautiflow.util.FechaHoraUtc;

import java.time.OffsetDateTime;

/**
 * Estructura estándar de error que devuelve la API: código HTTP, descripción, mensaje, ruta y marca de tiempo.
 *
 * @param status Código HTTP del error.
 * @param error Descripción corta del error.
 * @param message Mensaje descriptivo del error.
 * @param path Ruta de la petición que produjo el error.
 * @param timestamp Fecha y hora en que se generó el error.
 */
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        OffsetDateTime timestamp
) {
    /**
     * Construye una respuesta de error estándar con el estado HTTP, el mensaje y la ruta.
     */
    public static ApiErrorResponse of(HttpStatus status, String message, String path) {
        return new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                FechaHoraUtc.ahora()
        );
    }
}
