package com.nautiflow.exception;

import org.springframework.http.HttpStatus;
import com.nautiflow.time.FechaHoraUtc;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        OffsetDateTime timestamp
) {
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
