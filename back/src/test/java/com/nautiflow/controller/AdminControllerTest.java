package com.nautiflow.controller;

import com.nautiflow.config.SecurityConfig;
import com.nautiflow.dto.AdminDashboardDTO;
import com.nautiflow.dto.DecisionSolicitudDTO;
import com.nautiflow.dto.LancheroAdminDTO;
import com.nautiflow.dto.LoginRequest;
import com.nautiflow.dto.LoginResponse;
import com.nautiflow.dto.PaginaLancherosDTO;
import com.nautiflow.dto.PaginaSolicitudesDTO;
import com.nautiflow.dto.PaginaViajesDTO;
import com.nautiflow.dto.RechazoSolicitudDTO;
import com.nautiflow.dto.ResenaCreadaDTO;
import com.nautiflow.dto.ResenaViajeDTO;
import com.nautiflow.dto.SolicitudDetalleDTO;
import com.nautiflow.dto.SolicitudResumenDTO;
import com.nautiflow.dto.ZonaDTO;
import com.nautiflow.service.AdminAuthService;
import com.nautiflow.service.AdminAuthService.CredencialesInvalidasException;
import com.nautiflow.service.AdminService;
import com.nautiflow.service.AdminWorkflowService;
import com.nautiflow.service.ViajeWorkflowService;
import com.nautiflow.service.JwtTokenService;
import com.nautiflow.service.ResenaService;
import com.nautiflow.service.ZonaService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AdminController.class, LoginController.class, ResenaController.class, ZonaController.class
},
        properties = "nautiflow.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
@Import({SecurityConfig.class, JwtTokenService.class})
class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private AdminAuthService adminAuthService;

    @MockitoBean
    private AdminWorkflowService adminWorkflowService;

    @MockitoBean
    private ViajeWorkflowService viajeWorkflowService;

    @MockitoBean
    private ResenaService resenaService;

    @MockitoBean
    private ZonaService zonaService;

    @Test
    void listarZonasEsPublicoYDevuelveSoloIdYNombre() throws Exception {
        when(zonaService.listar()).thenReturn(List.of(
                new ZonaDTO(1L, "Zona Norte"),
                new ZonaDTO(3L, "Zona Centro")
        ));

        mockMvc.perform(get("/zonas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Zona Norte"))
                .andExpect(jsonPath("$[0].lanchas").doesNotExist())
                .andExpect(jsonPath("$[1].id").value(3));
    }

    @Test
    void loginDevuelveTokenBearer() throws Exception {
        when(adminAuthService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("jwt", "Bearer", "Inicio de sesión exitoso."));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@nautiflow.com","password":"Admin123*"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.mensaje").value("Inicio de sesión exitoso."));
    }

    @Test
    void loginIncorrectoDevuelve401() throws Exception {
        when(adminAuthService.login(any(LoginRequest.class))).thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@nautiflow.com","password":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void dashboardSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/admin/dashboard").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioAutenticadoSinRolAdminRecibe403() throws Exception {
        String token = Jwts.builder()
                .subject("usuario@example.com")
                .claim("role", "USER")
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                        "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
                )))
                .compact();

        mockMvc.perform(get("/admin/dashboard").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void dashboardConTokenDevuelveIndicadores() throws Exception {
        when(adminService.obtenerDashboard()).thenReturn(new AdminDashboardDTO(12, 28, 4));

        mockMvc.perform(get("/admin/dashboard").header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solicitudesPendientes").value(12))
                .andExpect(jsonPath("$.lanchasVerificadas").value(28))
                .andExpect(jsonPath("$.viajesActivos").value(4));
    }

    @Test
    void listadoFiltraPorEstadoYRespetaPaginacion() throws Exception {
        when(adminService.listarSolicitudes("PENDIENTE", 1, 2)).thenReturn(new PaginaSolicitudesDTO(
                List.of(new SolicitudResumenDTO(2L, "Ana Pérez", LocalDate.parse("2026-10-25"), "PENDIENTE")),
                1, 2, 3, 2
        ));

        mockMvc.perform(get("/admin/solicitudes")
                        .param("estado", "PENDIENTE")
                        .param("page", "1")
                        .param("size", "2")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
        verify(adminService).listarSolicitudes("PENDIENTE", 1, 2);
    }

    @Test
    void estadosPermitidosSonAceptados() throws Exception {
        for (String estado : List.of("PENDIENTE", "APROBADO", "RECHAZADO", "TODOS")) {
            when(adminService.listarSolicitudes(eq(estado), eq(0), eq(10)))
                    .thenReturn(new PaginaSolicitudesDTO(List.of(), 0, 10, 0, 0));
            mockMvc.perform(get("/admin/solicitudes")
                            .param("estado", estado)
                            .header("Authorization", bearerToken()))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void estadoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(get("/admin/solicitudes")
                        .param("estado", "OTRO")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void detalleExistenteDevuelveDatosSinClaveR2() throws Exception {
        when(adminService.obtenerDetalle(12L)).thenReturn(new SolicitudDetalleDTO(
                12L, "Ana Pérez", "ana@example.com", "5491123456789",
                "PENDIENTE", OffsetDateTime.parse("2026-10-25T10:30:00Z"), null, null,
                new SolicitudDetalleDTO.EmbarcacionDTO(34L, "La Aurora", 8, List.of("Primera Sección")),
                List.of(new SolicitudDetalleDTO.DocumentoDTO("LICENCIA", "https://r2.example/signed"))
        ));

        mockMvc.perform(get("/admin/solicitudes/12").header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCompleto").value("Ana Pérez"))
                .andExpect(jsonPath("$.documentoIdentidad").doesNotExist())
                .andExpect(jsonPath("$.embarcacion.zonas[0]").value("Primera Sección"))
                .andExpect(jsonPath("$.documentos[0].url").value("https://r2.example/signed"))
                .andExpect(jsonPath("$.documentos[0].claveArchivo").doesNotExist());
    }

    @Test
    void detalleInexistenteDevuelve404() throws Exception {
        when(adminService.obtenerDetalle(999L))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/admin/solicitudes/999").header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void aprobarRequiereAdminYDevuelveDecision() throws Exception {
        when(adminWorkflowService.aprobarSolicitud(12L))
                .thenReturn(new DecisionSolicitudDTO(12L, "APROBADO", "Solicitud aprobada."));

        mockMvc.perform(patch("/admin/solicitudes/12/aprobar").header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADO"))
                .andExpect(jsonPath("$.mensaje").value("Solicitud aprobada."));
        mockMvc.perform(patch("/admin/solicitudes/12/aprobar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rechazarValidaMotivoYDevuelveElMotivoGuardado() throws Exception {
        when(adminWorkflowService.rechazarSolicitud(12L, "Documento ilegible"))
                .thenReturn(new RechazoSolicitudDTO(12L, "RECHAZADO", "Documento ilegible"));

        mockMvc.perform(patch("/admin/solicitudes/12/rechazar")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Documento ilegible\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivo").value("Documento ilegible"));

        mockMvc.perform(patch("/admin/solicitudes/12/rechazar")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listadoLancherosExponeAmbosEstadosPorSeparado() throws Exception {
        when(adminWorkflowService.listarLancheros("APROBADO", 0, 10)).thenReturn(new PaginaLancherosDTO(
                List.of(new LancheroAdminDTO(
                        12L, "Ana Pérez", OffsetDateTime.parse("2026-10-25T10:30:00Z"), "APROBADO", "ACTIVO"
                )),
                0, 10, 1, 1
        ));

        mockMvc.perform(get("/admin/lancheros")
                        .param("estado", "APROBADO")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].estadoSolicitud").value("APROBADO"))
                .andExpect(jsonPath("$.content[0].estadoViaje").value("ACTIVO"));
        verify(adminWorkflowService).listarLancheros("APROBADO", 0, 10);
    }

    @Test
    void estadoOperativoRecibeElEstadoDeseado() throws Exception {
        when(adminWorkflowService.actualizarEstadoOperativo(eq(12L), any()))
                .thenReturn(new LancheroAdminDTO(
                        12L, "Ana Pérez", OffsetDateTime.parse("2026-10-25T10:30:00Z"), "APROBADO", "INACTIVO"
                ));

        mockMvc.perform(patch("/admin/lancheros/12/activar")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estadoViaje\":\"INACTIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoViaje").value("INACTIVO"));

        mockMvc.perform(patch("/admin/lancheros/12/activar")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estadoViaje\":\"EN_VIAJE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarViajesUsaElFiltroOperativoYEstaProtegido() throws Exception {
        when(viajeWorkflowService.listarEnCurso(0, 10))
                .thenReturn(new PaginaViajesDTO(List.of(), 0, 10, 0, 0));

        mockMvc.perform(get("/admin/viajes")
                        .param("estado", "EN_VIAJE")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/viajes").param("estado", "PROGRAMADO")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/admin/viajes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void finalizarViajeDevuelveTokenYDatosParaMensajePersonalizado() throws Exception {
        when(viajeWorkflowService.finalizar(25L)).thenReturn(new com.nautiflow.dto.ViajeFinalizadoDTO(
                25L,
                OffsetDateTime.parse("2026-10-25T10:30:00Z"),
                "secure-token",
                "Pasajero reservado",
                "5491123456789",
                "Hola Pasajero reservado, esperamos que hayas disfrutado tu viaje. "
                        + "Nos gustaría conocer tu experiencia. Nos encantaría que dejaras una reseña."
        ));

        mockMvc.perform(patch("/admin/viajes/25/finalizar").header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenResena").value("secure-token"))
                .andExpect(jsonPath("$.nombreUsuario").value("Pasajero reservado"))
                .andExpect(jsonPath("$.whatsappUsuario").value("5491123456789"))
                .andExpect(jsonPath("$.mensajeResena").value(
                        "Hola Pasajero reservado, esperamos que hayas disfrutado tu viaje. "
                                + "Nos gustaría conocer tu experiencia. Nos encantaría que dejaras una reseña."
                ))
                .andExpect(jsonPath("$.enlaceResena").doesNotExist())
                .andExpect(jsonPath("$.enlaceWhatsApp").doesNotExist());
    }

    @Test
    void crearResenaUtilizaTokenDeViajeYValidaPuntuacion() throws Exception {
        when(resenaService.crear(any(), eq("token-de-prueba")))
                .thenReturn(new ResenaCreadaDTO(45L, 25L, 5, "Excelente", OffsetDateTime.now()));

        mockMvc.perform(post("/resena/token-de-prueba")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"puntuacion\":5,\"comentario\":\"Excelente\",\"nombreCompleto\":\"Otro\",\"whatsapp\":\"000\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idViaje").value(25))
                .andExpect(jsonPath("$.puntuacion").value(5));

        for (int puntuacionInvalida : List.of(0, 6)) {
            mockMvc.perform(post("/resena/token-de-prueba")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"puntuacion\":" + puntuacionInvalida + "}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void consultaReservaPorTokenEsPublicaYUsaDatosPersistidos() throws Exception {
        when(resenaService.obtenerViaje("token-de-prueba")).thenReturn(new ResenaViajeDTO(
                25L, "Pasajero reservado", "Origen", "Destino",
                OffsetDateTime.now(), OffsetDateTime.now(), false
        ));

        mockMvc.perform(get("/resena/token-de-prueba"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("Pasajero reservado"))
                .andExpect(jsonPath("$.zonaOrigen").value("Origen"))
                .andExpect(jsonPath("$.turistaWhatsapp").doesNotExist());
    }

    private String bearerToken() {
        return "Bearer " + jwtTokenService.crearTokenAdministrador("admin@nautiflow.com");
    }
}
