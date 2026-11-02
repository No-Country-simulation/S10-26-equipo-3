package com.nautiflow.dto;

import java.util.List;

public record PaginaResenasDTO(
        List<ResenaAdminDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
