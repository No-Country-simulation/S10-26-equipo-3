package com.nautiflow.controller;

import com.nautiflow.dto.LancheroRegistroDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lancheros")
public class LancheroController {
    @PostMapping("/registrar")
    public ResponseEntity<String> register(@Valid @RequestBody LancheroRegistroDTO dto) {
        // Lógica de servicio pendiente (guardar en BD y enviar notificaciones)
        return ResponseEntity.status(HttpStatus.CREATED).body("Lanchero registrado correctamente.");
    }
}
