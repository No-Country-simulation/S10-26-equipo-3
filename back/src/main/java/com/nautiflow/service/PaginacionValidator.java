package com.nautiflow.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Valida los parámetros de paginación usados por los listados de la API.
 */
public final class PaginacionValidator {
    private static final int MAXIMO_ELEMENTOS_POR_PAGINA = 100;

    /**
     * Evita instanciar la clase; solo se usa su método estático.
     */
    private PaginacionValidator() {
    }

    /**
     * Valida que la página no sea negativa y que el tamaño esté entre 1 y 100.
     */
    public static void validar(int page, int size) {
        if (page < 0 || size < 1 || size > MAXIMO_ELEMENTOS_POR_PAGINA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La página debe ser positiva y size entre 1 y " + MAXIMO_ELEMENTOS_POR_PAGINA + "."
            );
        }
    }
}
