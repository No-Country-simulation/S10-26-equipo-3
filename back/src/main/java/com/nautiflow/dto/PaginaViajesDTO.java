package com.nautiflow.dto;

import java.util.List;

public record PaginaViajesDTO(
        List<ViajeEnCursoDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
