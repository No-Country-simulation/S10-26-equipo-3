package com.nautiflow.util;

import com.nautiflow.model.EstadoSolicitud;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EstadoSolicitudFiltro {
    private EstadoSolicitudFiltro() {
    }

    public static EstadoSolicitud parse(String valor) {
        if (valor == null) {
            return null;
        }

        String normalizado = valor.toUpperCase(Locale.ROOT);
        if ("TODOS".equals(normalizado)) {
            return null;
        }

        try {
            return EstadoSolicitud.valueOf(normalizado);
        } catch (IllegalArgumentException exception) {
            String estadosPermitidos = Stream.concat(
                    Stream.of(EstadoSolicitud.values()).map(Enum::name),
                    Stream.of("TODOS")
            ).collect(Collectors.joining(", "));
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado no válido. Valores permitidos: " + estadosPermitidos + "."
            );
        }
    }
}
