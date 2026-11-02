package com.nautiflow.dto;

import java.util.List;

public record PaginaSeguimientoViajesDTO(
        List<ViajeSeguimientoDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
