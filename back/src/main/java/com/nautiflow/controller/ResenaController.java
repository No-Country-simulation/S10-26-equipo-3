package com.nautiflow.controller;

import com.nautiflow.dto.CrearResenaRequest;
import com.nautiflow.dto.ResenaCreadaDTO;
import com.nautiflow.dto.ResenaViajeDTO;
import com.nautiflow.service.ResenaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResenaController {
    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    @GetMapping("/resena/{token}")
    public ResenaViajeDTO consultar(@PathVariable String token) {
        return resenaService.obtenerViaje(token);
    }

    @PostMapping("/resena/{token}")
    public ResponseEntity<ResenaCreadaDTO> crear(
            @PathVariable String token,
            @Valid @RequestBody CrearResenaRequest solicitud
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resenaService.crear(solicitud, token));
    }
}
