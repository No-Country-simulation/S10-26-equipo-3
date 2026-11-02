package com.nautiflow.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class PaginacionValidator {
    private static final int MAXIMO_ELEMENTOS_POR_PAGINA = 100;

    private PaginacionValidator() {
    }

    public static void validar(int page, int size) {
        if (page < 0 || size < 1 || size > MAXIMO_ELEMENTOS_POR_PAGINA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La página debe ser positiva y size entre 1 y " + MAXIMO_ELEMENTOS_POR_PAGINA + "."
            );
        }
    }
}
