package com.nautiflow.dto;

import java.util.List;

public record PaginaLanchasDestacadasDTO(
        List<LanchaDestacadaDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
