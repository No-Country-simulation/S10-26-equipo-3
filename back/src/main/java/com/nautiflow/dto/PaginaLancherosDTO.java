package com.nautiflow.dto;

import java.util.List;

/**
 * Página de lancheros para el panel administrativo.
 *
 * @param content Elementos incluidos en la página.
 * @param page Número de página solicitada (empieza en 0).
 * @param size Cantidad de elementos por página.
 * @param totalElements Cantidad total de elementos.
 * @param totalPages Cantidad total de páginas.
 */
public record PaginaLancherosDTO(
        List<LancheroListadoAdminDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
