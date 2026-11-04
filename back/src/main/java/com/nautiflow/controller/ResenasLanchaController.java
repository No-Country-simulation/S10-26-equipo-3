package com.nautiflow.controller;

import com.nautiflow.dto.PaginaResenasPublicasDTO;
import com.nautiflow.service.ResenasLanchaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Reseñas públicas")
public class ResenasLanchaController {
    private final ResenasLanchaService resenasLanchaService;

    public ResenasLanchaController(ResenasLanchaService resenasLanchaService) {
        this.resenasLanchaService = resenasLanchaService;
    }

    @GetMapping("/lanchas/{idLancha}/resenas")
    public PaginaResenasPublicasDTO listar(
            @PathVariable Long idLancha,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return resenasLanchaService.listar(idLancha, page, size);
    }
}
