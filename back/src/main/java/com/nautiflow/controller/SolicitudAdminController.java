package com.nautiflow.controller;

import com.nautiflow.dto.DecisionSolicitudDTO;
import com.nautiflow.dto.PaginaSolicitudesDTO;
import com.nautiflow.dto.RechazoSolicitudDTO;
import com.nautiflow.dto.RechazarSolicitudRequest;
import com.nautiflow.dto.SolicitudDetalleDTO;
import com.nautiflow.service.AdminService;
import com.nautiflow.service.AdminWorkflowService;
import com.nautiflow.util.EstadoSolicitudFiltro;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class SolicitudAdminController {
    private final AdminService adminService;
    private final AdminWorkflowService adminWorkflowService;

    public SolicitudAdminController(AdminService adminService, AdminWorkflowService adminWorkflowService) {
        this.adminService = adminService;
        this.adminWorkflowService = adminWorkflowService;
    }

    @GetMapping("/solicitudes")
    @Operation(tags = "Solicitudes")
    public PaginaSolicitudesDTO solicitudes(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminService.listarSolicitudes(EstadoSolicitudFiltro.parse(estado), page, size);
    }

    @GetMapping("/solicitudes/{id}")
    @Operation(tags = "Solicitudes")
    public SolicitudDetalleDTO detalle(@PathVariable Long id) {
        return adminService.obtenerDetalle(id);
    }

    @PatchMapping("/solicitudes/{id}/aprobar")
    @Operation(tags = "Solicitudes")
    public DecisionSolicitudDTO aprobar(@PathVariable Long id) {
        return adminWorkflowService.aprobarSolicitud(id);
    }

    @PatchMapping("/solicitudes/{id}/rechazar")
    @Operation(tags = "Solicitudes")
    public RechazoSolicitudDTO rechazar(
            @PathVariable Long id,
            @Valid @RequestBody RechazarSolicitudRequest solicitud
    ) {
        return adminWorkflowService.rechazarSolicitud(id, solicitud.motivo());
    }
}
