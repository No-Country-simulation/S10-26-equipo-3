package com.nautiflow.controller;

import com.nautiflow.dto.EstadoOperativoRequest;
import com.nautiflow.dto.LanchaDisponibleDTO;
import com.nautiflow.dto.LancheroAdminDTO;
import com.nautiflow.dto.PaginaLancherosDTO;
import com.nautiflow.service.AdminWorkflowService;
import com.nautiflow.service.ViajeWorkflowService;
import com.nautiflow.util.EstadoSolicitudFiltro;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class LancheroAdminController {
    private final AdminWorkflowService adminWorkflowService;
    private final ViajeWorkflowService viajeWorkflowService;

    public LancheroAdminController(
            AdminWorkflowService adminWorkflowService,
            ViajeWorkflowService viajeWorkflowService
    ) {
        this.adminWorkflowService = adminWorkflowService;
        this.viajeWorkflowService = viajeWorkflowService;
    }

    @GetMapping("/lancheros")
    @Operation(tags = "Lancheros")
    public PaginaLancherosDTO lancheros(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminWorkflowService.listarLancheros(EstadoSolicitudFiltro.parse(estado), page, size);
    }

    @PatchMapping("/lancheros/{id}/estado-viaje")
    @Operation(tags = "Lancheros")
    public LancheroAdminDTO actualizarEstadoOperativoLanchero(
            @PathVariable Long id,
            @Valid @RequestBody EstadoOperativoRequest solicitud
    ) {
        return adminWorkflowService.actualizarEstadoOperativo(id, solicitud);
    }

    @GetMapping("/lancheros/disponibles")
    @Operation(
            tags = "Lancheros",
            summary = "Buscar embarcaciones operativas para una ruta y hora",
            description = "Solo incluye lancheros aprobados y activos sin un viaje incompatible en curso. "
                    + "La fecha es la actual según el Clock configurado; no se registran reservas futuras ni duraciones."
    )
    public List<LanchaDisponibleDTO> lanchasDisponibles(
            @RequestParam(required = false) Integer capacidad,
            @RequestParam(name = "zonaOrigen", required = false) String zonaOrigen,
            @RequestParam(name = "zonaDestino", required = false) String zonaDestino,
            @RequestParam(name = "zona_origen", required = false) String zonaOrigenId,
            @RequestParam(name = "zona_destino", required = false) String zonaDestinoId,
            @RequestParam String hora
    ) {
        LocalTime horaSeleccionada;
        try {
            horaSeleccionada = LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora debe tener formato HH:mm.");
        }
        return viajeWorkflowService.buscarDisponibles(
                capacidad,
                combinarFiltroZona(zonaOrigen, zonaOrigenId, "origen"),
                combinarFiltroZona(zonaDestino, zonaDestinoId, "destino"),
                horaSeleccionada
        );
    }

    private String combinarFiltroZona(String nombre, String id, String tipo) {
        if (nombre != null && id != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Indique la zona de " + tipo + " usando solo uno de sus parámetros."
            );
        }
        return id == null ? nombre : id;
    }
}
