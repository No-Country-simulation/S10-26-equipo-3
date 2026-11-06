package com.nautiflow.dto;

import java.util.List;

public record PaginaResenasPublicasDTO(
        List<ResenaPublicaDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
