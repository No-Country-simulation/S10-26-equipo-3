package com.nautiflow.controller;

import com.nautiflow.dto.PaginaResenasPublicasDTO;
import com.nautiflow.service.ResenasLancheroService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Reseñas públicas")
public class ResenasLancheroController {
    private final ResenasLancheroService resenasLancheroService;

    public ResenasLancheroController(ResenasLancheroService resenasLancheroService) {
        this.resenasLancheroService = resenasLancheroService;
    }

    @GetMapping("/lancheros/{idLanchero}/resenas")
    public PaginaResenasPublicasDTO listar(
            @PathVariable Long idLanchero,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return resenasLancheroService.listar(idLanchero, page, size);
    }
}
