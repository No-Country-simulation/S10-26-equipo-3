package com.nautiflow.controller;

import com.nautiflow.dto.LancheroRegistroDTO;
import com.nautiflow.service.LancheroSolicitudParser;
import com.nautiflow.service.LancheroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/lancheros")
public class LancheroController {
    private final LancheroService lancheroService;
    private final LancheroSolicitudParser solicitudParser;

    public LancheroController(LancheroService lancheroService, LancheroSolicitudParser solicitudParser) {
        this.lancheroService = lancheroService;
        this.solicitudParser = solicitudParser;
    }

    @PostMapping("/registrar")
    public ResponseEntity<LancheroService.RegistroRespuesta> register(
            @RequestParam("solicitud") String solicitudJson,
            @RequestPart("licencia") MultipartFile licencia,
            @RequestPart("seguro") MultipartFile seguro,
            @RequestPart("antecedentes") MultipartFile antecedentes,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lancheroService.registrarLanchero(
                        solicitudParser.parsear(solicitudJson), licencia, seguro, antecedentes, fotos
                ));
    }
}
