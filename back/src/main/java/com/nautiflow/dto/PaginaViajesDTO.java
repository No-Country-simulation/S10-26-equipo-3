package com.nautiflow.dto;

import java.util.List;

public record PaginaViajesDTO(
        List<ViajeAdminDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
