package com.nautiflow.controller;

import com.nautiflow.dto.CrearViajeRequest;
import com.nautiflow.dto.PaginaSeguimientoViajesDTO;
import com.nautiflow.dto.PaginaViajesDTO;
import com.nautiflow.dto.ViajeAdminDTO;
import com.nautiflow.dto.ViajeFinalizadoDTO;
import com.nautiflow.model.EstadoOperativoLanchero;
import com.nautiflow.service.ViajeWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

/**
 * Expone la creación, consulta, seguimiento y finalización de viajes para el administrador.
 */
@RestController
@RequestMapping("/admin")
public class ViajeAdminController {
    private final ViajeWorkflowService viajeWorkflowService;

    /**
     * Inyecta el servicio de flujo de viajes.
     */
    public ViajeAdminController(ViajeWorkflowService viajeWorkflowService) {
        this.viajeWorkflowService = viajeWorkflowService;
    }

    /**
     * Crea e inicia un nuevo viaje.
     */
    @PostMapping("/viajes")
    @Operation(tags = "Viajes")
    public ResponseEntity<ViajeAdminDTO> crearViaje(@Valid @RequestBody CrearViajeRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(viajeWorkflowService.crearEIniciar(solicitud));
    }

    /**
     * Lista los viajes en curso; solo admite el estado EN_VIAJE.
     */
    @GetMapping("/viajes")
    @Operation(tags = "Viajes")
    public PaginaViajesDTO viajesEnCurso(
            @RequestParam(defaultValue = "EN_VIAJE") String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (!EstadoOperativoLanchero.EN_VIAJE.name().equals(estado.toUpperCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El único estado admitido es EN_VIAJE.");
        }
        return viajeWorkflowService.listarEnCurso(page, size);
    }

    /**
     * Lista el historial de viajes para seguimiento.
     */
    @GetMapping("/viajes/seguimiento")
    @Operation(
            tags = "Viajes",
            summary = "Consultar el historial y estado operativo de los viajes"
    )
    public PaginaSeguimientoViajesDTO seguimientoViajes(
            @RequestParam(defaultValue = "TODOS") String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return viajeWorkflowService.listarSeguimiento(page, size, estado);
    }

    /**
     * Finaliza un viaje y genera su token de reseña.
     */
    @PatchMapping("/viajes/{id}/finalizar")
    @Operation(tags = "Viajes")
    public ViajeFinalizadoDTO finalizarViaje(@PathVariable Long id) {
        return viajeWorkflowService.finalizar(id);
    }
}
