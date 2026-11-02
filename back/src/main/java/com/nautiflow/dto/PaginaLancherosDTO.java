package com.nautiflow.dto;

import java.util.List;

public record PaginaLancherosDTO(
        List<LancheroAdminDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
