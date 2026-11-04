package com.nautiflow.util;

import com.nautiflow.model.EstadoSolicitud;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Convierte el parámetro de filtro de estado de solicitud en un valor del enum, aceptando TODOS como ausencia de filtro.
 */
public final class EstadoSolicitudFiltro {
    /**
     * Evita instanciar la clase; solo se usa su método estático.
     */
    private EstadoSolicitudFiltro() {
    }

    /**
     * Convierte el texto del filtro en un estado de solicitud; 'TODOS' o null significan sin filtro.
     */
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
