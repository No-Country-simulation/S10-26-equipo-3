package com.nautiflow.controller;

import com.nautiflow.dto.PaginaLanchasDestacadasDTO;
import com.nautiflow.service.LanchasDestacadasService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Lanchas")
public class LanchasDestacadasController {
    private final LanchasDestacadasService lanchasDestacadasService;

    public LanchasDestacadasController(LanchasDestacadasService lanchasDestacadasService) {
        this.lanchasDestacadasService = lanchasDestacadasService;
    }

    @GetMapping("/lanchas/destacadas")
    public PaginaLanchasDestacadasDTO listar() {
        return lanchasDestacadasService.listar();
    }
}
