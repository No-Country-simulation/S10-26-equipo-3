package com.nautiflow.controller;

import com.nautiflow.dto.AdminDashboardDTO;
import com.nautiflow.dto.CrearViajeRequest;
import com.nautiflow.dto.DecisionSolicitudDTO;
import com.nautiflow.dto.EstadoOperativoRequest;
import com.nautiflow.dto.LancheroAdminDTO;
import com.nautiflow.dto.PaginaLancherosDTO;
import com.nautiflow.dto.PaginaSolicitudesDTO;
import com.nautiflow.dto.PaginaResenasDTO;
import com.nautiflow.dto.PaginaViajesDTO;
import com.nautiflow.dto.RechazoSolicitudDTO;
import com.nautiflow.dto.RechazarSolicitudRequest;
import com.nautiflow.dto.SolicitudDetalleDTO;
import com.nautiflow.dto.ViajeAdminDTO;
import com.nautiflow.dto.ViajeFinalizadoDTO;
import com.nautiflow.service.AdminService;
import com.nautiflow.service.AdminWorkflowService;
import com.nautiflow.service.ResenaService;
import com.nautiflow.service.ViajeWorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/admin")
@Validated
public class AdminController {
    private static final Set<String> ESTADOS = Set.of("PENDIENTE", "APROBADO", "RECHAZADO", "TODOS");

    private final AdminService adminService;
    private final AdminWorkflowService adminWorkflowService;
    private final ViajeWorkflowService viajeWorkflowService;
    private final ResenaService resenaService;

    public AdminController(
            AdminService adminService,
            AdminWorkflowService adminWorkflowService,
            ViajeWorkflowService viajeWorkflowService,
            ResenaService resenaService
    ) {
        this.adminService = adminService;
        this.adminWorkflowService = adminWorkflowService;
        this.viajeWorkflowService = viajeWorkflowService;
        this.resenaService = resenaService;
    }

    @GetMapping("/dashboard")
    public AdminDashboardDTO dashboard() {
        return adminService.obtenerDashboard();
    }

    @GetMapping("/solicitudes")
    public PaginaSolicitudesDTO solicitudes(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String estadoNormalizado = estado == null ? null : estado.toUpperCase(Locale.ROOT);
        if (estadoNormalizado != null && !ESTADOS.contains(estadoNormalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado no válido. Valores permitidos: PENDIENTE, APROBADO, RECHAZADO, TODOS."
            );
        }
        return adminService.listarSolicitudes(estadoNormalizado, page, size);
    }

    @GetMapping("/solicitudes/{id}")
    public SolicitudDetalleDTO detalle(@PathVariable Long id) {
        return adminService.obtenerDetalle(id);
    }

    @PatchMapping("/solicitudes/{id}/aprobar")
    public DecisionSolicitudDTO aprobar(@PathVariable Long id) {
        return adminWorkflowService.aprobarSolicitud(id);
    }

    @PatchMapping("/solicitudes/{id}/rechazar")
    public RechazoSolicitudDTO rechazar(
            @PathVariable Long id,
            @Valid @RequestBody RechazarSolicitudRequest solicitud
    ) {
        return adminWorkflowService.rechazarSolicitud(id, solicitud.motivo());
    }

    @GetMapping("/lancheros")
    public PaginaLancherosDTO lancheros(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String estadoNormalizado = estado == null ? null : estado.toUpperCase(Locale.ROOT);
        if (estadoNormalizado != null && !ESTADOS.contains(estadoNormalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado no válido. Valores permitidos: PENDIENTE, APROBADO, RECHAZADO, TODOS."
            );
        }
        return adminWorkflowService.listarLancheros(estadoNormalizado, page, size);
    }

    @PatchMapping("/lancheros/{id}/estado-viaje")
    public LancheroAdminDTO actualizarEstadoOperativoLanchero(
            @PathVariable Long id,
            @Valid @RequestBody EstadoOperativoRequest solicitud
    ) {
        return adminWorkflowService.actualizarEstadoOperativo(id, solicitud);
    }

    @PostMapping("/viajes")
    public ResponseEntity<ViajeAdminDTO> crearViaje(@Valid @RequestBody CrearViajeRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(viajeWorkflowService.crearEIniciar(solicitud));
    }

    @GetMapping("/viajes")
    public PaginaViajesDTO viajesEnCurso(
            @RequestParam(defaultValue = "EN_VIAJE") String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (!"EN_VIAJE".equals(estado.toUpperCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El único estado admitido es EN_VIAJE.");
        }
        return viajeWorkflowService.listarEnCurso(page, size);
    }

    @PatchMapping("/viajes/{id}/finalizar")
    public ViajeFinalizadoDTO finalizarViaje(@PathVariable Long id) {
        return viajeWorkflowService.finalizar(id);
    }

    @GetMapping("/resenas")
    public PaginaResenasDTO resenas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return resenaService.listar(page, size);
    }
}
