package com.nautiflow.controller;

import com.nautiflow.config.SecurityConfig;
import com.nautiflow.config.JacksonConfig;
import com.nautiflow.dto.AdminDashboardDTO;
import com.nautiflow.dto.DecisionSolicitudDTO;
import com.nautiflow.dto.ViajeAdminDTO;
import com.nautiflow.dto.LancheroAdminDTO;
import com.nautiflow.dto.LanchaDisponibleDTO;
import com.nautiflow.dto.LoginRequest;
import com.nautiflow.dto.LoginResponse;
import com.nautiflow.dto.PaginaLancherosDTO;
import com.nautiflow.dto.PaginaSolicitudesDTO;
import com.nautiflow.dto.PaginaViajesDTO;
import com.nautiflow.dto.PaginaSeguimientoViajesDTO;
import com.nautiflow.dto.RechazoSolicitudDTO;
import com.nautiflow.dto.PaginaResenasDTO;
import com.nautiflow.dto.ResenaAdminDTO;
import com.nautiflow.dto.ResenaCreadaDTO;
import com.nautiflow.dto.ResenaTokenDTO;
import com.nautiflow.dto.ResenaViajeDTO;
import com.nautiflow.dto.SolicitudDetalleDTO;
import com.nautiflow.dto.SolicitudResumenDTO;
import com.nautiflow.dto.ViajeEnCursoDTO;
import com.nautiflow.dto.ViajeSeguimientoDTO;
import com.nautiflow.dto.ZonaDTO;
import com.nautiflow.model.EstadoSolicitud;
import com.nautiflow.service.AdminAuthService;
import com.nautiflow.service.AdminAuthService.CredencialesInvalidasException;
import com.nautiflow.service.AdminService;
import com.nautiflow.service.AdminWorkflowService;
import com.nautiflow.service.LoginAttemptService;
import com.nautiflow.service.ViajeWorkflowService;
import com.nautiflow.service.JwtTokenService;
import com.nautiflow.service.ResenaService;
import com.nautiflow.service.ZonaService;
import com.nautiflow.exception.DemasiadosIntentosLoginException;
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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AdminDashboardController.class,
        SolicitudAdminController.class,
        LancheroAdminController.class,
        ViajeAdminController.class,
        ResenaAdminController.class,
        LoginController.class,
        ResenaController.class,
        ZonaController.class
},
        properties = "nautiflow.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
@Import({SecurityConfig.class, JacksonConfig.class, JwtTokenService.class})
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
    private LoginAttemptService loginAttemptService;

    @MockitoBean
    private AdminWorkflowService adminWorkflowService;

    @MockitoBean
    private ViajeWorkflowService viajeWorkflowService;

    @MockitoBean
    private ResenaService resenaService;

    @MockitoBean
    private ZonaService zonaService;

    @Test
    void preflightCorsPermiteHeadersYMetodosEnRutasAdministrativasSinValidarJwt() throws Exception {
        mockMvc.perform(options("/admin/viajes")
                        .header("Origin", "https://frontend.example")
                        .header("Access-Control-Request-Method", "PATCH")
                        .header("Access-Control-Request-Headers", "authorization,content-type")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://frontend.example"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("PATCH")))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("authorization")))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

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
    void camposDeLoginVaciosDevuelven400ConFormatoApi() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"Admin123*\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/login"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@nautiflow.com\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/login"));
    }

    @Test
    void jsonMalFormadoEnLoginDevuelve400() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/login"));
    }

    @Test
    void tipoDeContenidoNoSoportadoDevuelve415() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("email=admin&password=secret"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.path").value("/login"));
    }

    @Test
    void loginIncorrectoDevuelve401() throws Exception {
        when(adminAuthService.login(any(LoginRequest.class))).thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@nautiflow.com","password":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/login"));

        verify(loginAttemptService).registrarFallo(anyString());
    }

    @Test
    void loginBloqueadoPorDemasiadosIntentosDevuelve429ConRetryAfter() throws Exception {
        doThrow(new DemasiadosIntentosLoginException(600))
                .when(loginAttemptService).validarPermitido(anyString());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@nautiflow.com","password":"Admin123*"}
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "600"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.path").value("/login"));
    }

    @Test
    void dashboardSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/admin/dashboard"));
    }

    @Test
    void endpointsNuevosDeAdministracionRequierenAutenticacion() throws Exception {
        mockMvc.perform(get("/admin/lancheros/disponibles").param("hora", "14:00"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/admin/viajes/seguimiento"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buscarDisponiblesValidaFormatoDeHoraYExponeDatosDeContacto() throws Exception {
        when(viajeWorkflowService.buscarDisponibles(null, "DELTA", "PRIMERA_SECCION", java.time.LocalTime.of(14, 0)))
                .thenReturn(List.of(new LanchaDisponibleDTO(
                        34L, "Brisa Marina", 8, List.of("DELTA", "PRIMERA_SECCION"), true,
                        "ACTIVO", "+573001234567"
                )));

        mockMvc.perform(get("/admin/lancheros/disponibles")
                        .header("Authorization", bearerToken())
                        .param("zonaOrigen", "DELTA")
                        .param("zonaDestino", "PRIMERA_SECCION")
                        .param("hora", "14:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(34))
                .andExpect(jsonPath("$[0].nombre").value("Brisa Marina"))
                .andExpect(jsonPath("$[0].capacidad").value(8))
                .andExpect(jsonPath("$[0].verificada").value(true))
                .andExpect(jsonPath("$[0].estadoOperativo").value("ACTIVO"))
                .andExpect(jsonPath("$[0].whatsappLanchero").value("+573001234567"));
        verify(viajeWorkflowService).buscarDisponibles(
                null, "DELTA", "PRIMERA_SECCION", java.time.LocalTime.of(14, 0)
        );

        mockMvc.perform(get("/admin/lancheros/disponibles")
                        .header("Authorization", bearerToken())
                        .param("hora", "25:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarDisponiblesAceptaLosParametrosSnakeCaseConIdsDeZona() throws Exception {
        when(viajeWorkflowService.buscarDisponibles(null, "1", "2", java.time.LocalTime.of(23, 0)))
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/lancheros/disponibles")
                        .header("Authorization", bearerToken())
                        .param("capacidad", "4")
                        .param("zona_origen", "1")
                        .param("zona_destino", "2")
                        .param("hora", "23:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(viajeWorkflowService).buscarDisponibles(
                4, "1", "2", java.time.LocalTime.of(23, 0)
        );
    }

    @Test
    void buscarDisponiblesAceptaElContratoCamelCaseDePostman() throws Exception {
        when(viajeWorkflowService.buscarDisponibles(4, "1", "3", java.time.LocalTime.of(23, 0)))
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/lancheros/disponibles")
                        .header("Authorization", bearerToken())
                        .param("capacidad", "4")
                        .param("zonaOrigen", "1")
                        .param("zonaDestino", "3")
                        .param("hora", "23:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(viajeWorkflowService).buscarDisponibles(
                4, "1", "3", java.time.LocalTime.of(23, 0)
        );
    }

    @Test
    void openApiPublicadoDocumentaSoloLosCuatroParametrosPublicos() throws Exception {
        String documento = mockMvc.perform(get("/openapi.yaml"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        int inicio = documento.indexOf("  /admin/lancheros/disponibles:");
        int fin = documento.indexOf("  /admin/lancheros/{id}/estado-viaje:", inicio);
        assertTrue(inicio >= 0 && fin > inicio);

        String operacion = documento.substring(inicio, fin);
        List<String> parametros = operacion.lines()
                .map(String::strip)
                .filter(line -> line.startsWith("- name:"))
                .map(line -> line.substring("- name:".length()).strip())
                .toList();
        assertEquals(List.of("capacidad", "zonaOrigen", "zonaDestino", "hora"), parametros);
        assertFalse(operacion.contains("zona_origen"));
        assertFalse(operacion.contains("zona_destino"));
        assertTrue(documento.contains("description: No autenticado."));
        assertTrue(documento.contains("description: Sin permiso."));
        String definicionesParametros = operacion.substring(
                operacion.indexOf("      parameters:"), operacion.indexOf("      responses:")
        );
        assertFalse(definicionesParametros.contains("description:"));
        assertEquals(3, definicionesParametros.split("type: integer", -1).length - 1);
        assertTrue(definicionesParametros.contains("type: string"));
        assertTrue(definicionesParametros.contains("pattern: '^([01][0-9]|2[0-3]):[0-5][0-9]$'"));
        assertTrue(definicionesParametros.contains("example: \"23:00\""));
    }

    @Test
    void seguimientoDevuelveHistorialConEstadoYUltimaUbicacion() throws Exception {
        when(viajeWorkflowService.listarSeguimiento(0, 10)).thenReturn(new PaginaSeguimientoViajesDTO(
                List.of(new ViajeSeguimientoDTO(
                        25L, "Brisa Marina", "Delta", "Primera Seccion",
                        OffsetDateTime.parse("2026-11-02T14:00:00Z"), null,
                        "EN_VIAJE", "Segunda Seccion"
                )),
                0, 10, 1, 1
        ));

        mockMvc.perform(get("/admin/viajes/seguimiento").header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(25))
                .andExpect(jsonPath("$.content[0].nombreEmbarcacion").value("Brisa Marina"))
                .andExpect(jsonPath("$.content[0].estado").value("EN_VIAJE"))
                .andExpect(jsonPath("$.content[0].ultimaUbicacionConfirmada").value("Segunda Seccion"));
    }

    @Test
    void tokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/admin/dashboard").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenExpiradoYFirmaInvalidaDevuelven401() throws Exception {
        String expired = Jwts.builder()
                .subject("admin@nautiflow.com")
                .claim("role", "ADMIN")
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                        "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
                )))
                .compact();
        String invalidSignature = Jwts.builder()
                .subject("admin@nautiflow.com")
                .claim("role", "ADMIN")
                .signWith(Keys.hmacShaKeyFor("abcdef0123456789abcdef0123456789".getBytes()))
                .compact();

        for (String token : List.of(expired, invalidSignature)) {
            mockMvc.perform(get("/admin/dashboard").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"));
        }
    }

    @Test
    void tokenMalFormadoEnEndpointPublicoTambienDevuelve401() throws Exception {
        mockMvc.perform(get("/zonas").header("Authorization", "Bearer no-es-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/zonas"));
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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.path").value("/admin/dashboard"));
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
    void errorInesperadoDevuelve500SinExponerDetallesInternos() throws Exception {
        when(adminService.obtenerDashboard()).thenThrow(new IllegalStateException("database password leaked"));

        mockMvc.perform(get("/admin/dashboard").header("Authorization", bearerToken()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ocurrió un error interno al procesar la solicitud."))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("database password")
                )))
                .andExpect(jsonPath("$.path").value("/admin/dashboard"));
    }

    @Test
    void listadoFiltraPorEstadoYRespetaPaginacion() throws Exception {
        when(adminService.listarSolicitudes(EstadoSolicitud.PENDIENTE, 1, 2)).thenReturn(new PaginaSolicitudesDTO(
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
        verify(adminService).listarSolicitudes(EstadoSolicitud.PENDIENTE, 1, 2);
    }

    @Test
    void estadosPermitidosSonAceptados() throws Exception {
        for (String estado : List.of(
                "PENDIENTE", "APROBADO", "RECHAZADO", "TODOS", "pendiente", "aprobado", "rechazado", "todos"
        )) {
            EstadoSolicitud filtro = "TODOS".equalsIgnoreCase(estado)
                    ? null
                    : EstadoSolicitud.valueOf(estado.toUpperCase(Locale.ROOT));
            when(adminService.listarSolicitudes(eq(filtro), eq(0), eq(10)))
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
        mockMvc.perform(get("/admin/lancheros")
                        .param("estado", "OTRO")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void estadoDeViajesInvalidoDevuelve400() throws Exception {
        mockMvc.perform(get("/admin/viajes")
                        .param("estado", "FINALIZADO")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/admin/viajes")
                        .param("estado", "OTRO")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void parametrosDeConsultaEIdsDeRutaMalFormadosDevuelven400() throws Exception {
        mockMvc.perform(get("/admin/solicitudes")
                        .param("page", "no-es-numero")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/admin/solicitudes"));

        mockMvc.perform(get("/admin/solicitudes/no-es-numero")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
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
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/admin/solicitudes/999"));
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

        when(adminWorkflowService.rechazarSolicitud(12L, null))
                .thenReturn(new RechazoSolicitudDTO(12L, "RECHAZADO", null));
        mockMvc.perform(patch("/admin/solicitudes/12/rechazar")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivo").doesNotExist());

        mockMvc.perform(patch("/admin/solicitudes/12/rechazar")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"" + "a".repeat(501) + "\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/admin/solicitudes/12/rechazar")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivo").doesNotExist());
    }

    @Test
    void listadoLancherosExponeAmbosEstadosPorSeparado() throws Exception {
        when(adminWorkflowService.listarLancheros(EstadoSolicitud.APROBADO, 0, 10)).thenReturn(new PaginaLancherosDTO(
                List.of(new LancheroAdminDTO(
                        12L, "Ana Pérez", OffsetDateTime.parse("2026-10-25T10:30:00Z"), "APROBADO", "ACTIVO"
                )),
                0, 10, 1, 1
        ));

        mockMvc.perform(get("/admin/lancheros")
                        .param("estado", "aPrObAdO")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].estadoSolicitud").value("APROBADO"))
                .andExpect(jsonPath("$.content[0].estadoViaje").value("ACTIVO"));
        verify(adminWorkflowService).listarLancheros(EstadoSolicitud.APROBADO, 0, 10);
    }

    @Test
    void filtroTodosEnLancherosConsultaSinEstado() throws Exception {
        when(adminWorkflowService.listarLancheros(null, 0, 10))
                .thenReturn(new PaginaLancherosDTO(List.of(), 0, 10, 0, 0));

        mockMvc.perform(get("/admin/lancheros")
                        .param("estado", "TODOS")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk());

        verify(adminWorkflowService).listarLancheros(null, 0, 10);
    }

    @Test
    void estadoOperativoRecibeElEstadoDeseado() throws Exception {
        when(adminWorkflowService.actualizarEstadoOperativo(eq(12L), any()))
                .thenReturn(new LancheroAdminDTO(
                        12L, "Ana Pérez", OffsetDateTime.parse("2026-10-25T10:30:00Z"), "APROBADO", "INACTIVO"
                ));

        mockMvc.perform(patch("/admin/lancheros/12/estado-viaje")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estadoViaje\":\"INACTIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoViaje").value("INACTIVO"));

        mockMvc.perform(patch("/admin/lancheros/12/estado-viaje")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estadoViaje\":\"EN_VIAJE\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/admin/lancheros/12/activar")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estadoViaje\":\"INACTIVO\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void consultarViajesUsaElFiltroOperativoYEstaProtegido() throws Exception {
        when(viajeWorkflowService.listarEnCurso(0, 10)).thenReturn(new PaginaViajesDTO(
                List.of(new ViajeEnCursoDTO(
                        25L,
                        34L,
                        12L,
                        "María Pérez",
                        "Zona Norte",
                        "Zona Sur",
                        OffsetDateTime.parse("2026-11-02T03:37:41.082632Z"),
                        null,
                        2,
                        "Ana Pérez",
                        "+573001234567"
                )),
                0,
                10,
                1,
                1
        ));

        mockMvc.perform(get("/admin/viajes")
                        .param("estado", "EN_VIAJE")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].turistaWhatsapp").value("+573001234567"))
                .andExpect(jsonPath("$.content[0].turistaWhatsapp").isString())
                .andExpect(jsonPath("$.content[0].turistaNombreCompleto").value("Ana Pérez"))
                .andExpect(jsonPath("$.content[0].idLancha").value(34))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
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
    void listadoDeResenasIncluyeElContextoDelViajeSinPerderLaFechaDeResena() throws Exception {
        when(resenaService.listar(0, 10)).thenReturn(new PaginaResenasDTO(
                List.of(new ResenaAdminDTO(
                        45L,
                        25L,
                        5,
                        "Excelente servicio",
                        12L,
                        "María Pérez",
                        OffsetDateTime.parse("2026-11-02T11:05:00Z"),
                        "Ana Pérez",
                        "Zona Norte",
                        "Zona Sur",
                        OffsetDateTime.parse("2026-11-02T10:00:00Z"),
                        OffsetDateTime.parse("2026-11-02T11:00:00Z")
                )),
                0,
                10,
                1,
                1
        ));

        mockMvc.perform(get("/admin/resenas")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idResena").value(45))
                .andExpect(jsonPath("$.content[0].idViaje").value(25))
                .andExpect(jsonPath("$.content[0].turistaNombreCompleto").value("Ana Pérez"))
                .andExpect(jsonPath("$.content[0].zonaOrigen").value("Zona Norte"))
                .andExpect(jsonPath("$.content[0].zonaDestino").value("Zona Sur"))
                .andExpect(jsonPath("$.content[0].fecha").value("2026-11-02T11:05:00.000000Z"))
                .andExpect(jsonPath("$.content[0].fechaHoraInicio").value("2026-11-02T10:00:00.000000Z"))
                .andExpect(jsonPath("$.content[0].fechaHoraFin").value("2026-11-02T11:00:00.000000Z"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
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

        mockMvc.perform(post("/resena/token-de-prueba")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"puntuacion\":5,\"comentario\":\"" + "a".repeat(501) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/resena/token-de-prueba"));
    }

    @Test
    void camposObligatoriosAusentesEnViajeDevuelven400() throws Exception {
        mockMvc.perform(post("/admin/viajes")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/admin/viajes"));
    }

    @Test
    void aprobarYRechazarSolicitudProcesadaDevuelven409Estructurado() throws Exception {
        when(adminWorkflowService.aprobarSolicitud(12L))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT,
                        "La solicitud ya fue decidida."
                ));
        when(adminWorkflowService.rechazarSolicitud(12L, "Motivo"))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT,
                        "La solicitud ya fue decidida."
                ));

        mockMvc.perform(patch("/admin/solicitudes/12/aprobar")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("La solicitud ya fue decidida."));

        mockMvc.perform(patch("/admin/solicitudes/12/rechazar")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Motivo\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("La solicitud ya fue decidida."));
    }

    @Test
    void aprobarYRechazarSolicitudInexistenteDevuelven404Estructurado() throws Exception {
        when(adminWorkflowService.aprobarSolicitud(999L))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Lanchero no encontrado."
                ));
        when(adminWorkflowService.rechazarSolicitud(999L, "Motivo"))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Lanchero no encontrado."
                ));

        mockMvc.perform(patch("/admin/solicitudes/999/aprobar")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        mockMvc.perform(patch("/admin/solicitudes/999/rechazar")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Motivo\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void consultaReservaPorTokenEsPublicaYUsaDatosPersistidos() throws Exception {
        when(resenaService.obtenerViaje("token-de-prueba")).thenReturn(new ResenaViajeDTO(
                25L, "Pasajero reservado", "Origen", "Destino",
                OffsetDateTime.now(), OffsetDateTime.now(), false, null
        ));

        mockMvc.perform(get("/resena/token-de-prueba"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("Pasajero reservado"))
                .andExpect(jsonPath("$.zonaOrigen").value("Origen"))
                .andExpect(jsonPath("$.resenaRegistrada").value(false))
                .andExpect(jsonPath("$.resena").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.turistaWhatsapp").doesNotExist());
    }

    @Test
    void consultaReservaPorTokenIncluyeResenaExistente() throws Exception {
        OffsetDateTime inicio = OffsetDateTime.parse("2026-11-02T18:16:19.230457Z");
        OffsetDateTime fin = OffsetDateTime.parse("2026-11-02T18:22:18.068711Z");
        OffsetDateTime fechaResena = OffsetDateTime.parse("2026-11-02T18:22:22.682942Z");
        ResenaTokenDTO resena = new ResenaTokenDTO(
                2L, 5, "Excelente servicio", 3L, "María Pérez", fechaResena
        );
        when(resenaService.obtenerViaje("token-con-resena")).thenReturn(new ResenaViajeDTO(
                3L, "Pasajero de prueba", "Zona Norte", "Zona Sur", inicio, fin, true, resena
        ));

        mockMvc.perform(get("/resena/token-con-resena"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resenaRegistrada").value(true))
                .andExpect(jsonPath("$.resena.idResena").value(2))
                .andExpect(jsonPath("$.resena.puntuacion").value(5))
                .andExpect(jsonPath("$.resena.comentario").value("Excelente servicio"))
                .andExpect(jsonPath("$.resena.idLanchero").value(3))
                .andExpect(jsonPath("$.resena.nombreLanchero").value("María Pérez"))
                .andExpect(jsonPath("$.resena.fecha").value("2026-11-02T18:22:22.682942Z"))
                .andExpect(jsonPath("$.resena.idViaje").doesNotExist())
                .andExpect(jsonPath("$.resena.turistaNombreCompleto").doesNotExist())
                .andExpect(jsonPath("$.resena.zonaOrigen").doesNotExist())
                .andExpect(jsonPath("$.resena.zonaDestino").doesNotExist())
                .andExpect(jsonPath("$.resena.fechaHoraInicio").doesNotExist())
                .andExpect(jsonPath("$.resena.fechaHoraFin").doesNotExist())
                .andExpect(jsonPath("$.idViaje").value(3))
                .andExpect(jsonPath("$.zonaOrigen").value("Zona Norte"))
                .andExpect(jsonPath("$.zonaDestino").value("Zona Sur"))
                .andExpect(jsonPath("$.fechaHoraInicio").value("2026-11-02T18:16:19.230457Z"))
                .andExpect(jsonPath("$.fechaHoraFin").value("2026-11-02T18:22:18.068711Z"));
    }

    @Test
    void crearViajeDevuelveWhatsappComoTextoYConservaFechaHora() throws Exception {
        OffsetDateTime inicio = OffsetDateTime.parse("2026-11-02T12:53:31.8975973-05:00");
        when(viajeWorkflowService.crearEIniciar(any())).thenReturn(new ViajeAdminDTO(
                2L,
                3L,
                3L,
                "María Pérez",
                "Zona Norte",
                "Zona Sur",
                inicio,
                null,
                2,
                "Pasajero de prueba",
                "+5491123456789"
        ));

        mockMvc.perform(post("/admin/viajes")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idLancha": 3,
                                  "idZonaOrigen": 1,
                                  "idZonaDestino": 2,
                                  "cantidadPasajeros": 2,
                                  "turistaNombreCompleto": "Pasajero de prueba",
                                  "turistaWhatsapp": "+5491123456789"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.turistaWhatsapp").value("+5491123456789"))
                .andExpect(jsonPath("$.turistaWhatsapp").isString())
                .andExpect(jsonPath("$.fechaHoraInicio").value("2026-11-02T17:53:31.897597Z"))
                .andExpect(jsonPath("$.turistaNombreCompleto").value("Pasajero de prueba"))
                .andExpect(jsonPath("$.fechaHoraFin").value(org.hamcrest.Matchers.nullValue()));
    }

    private String bearerToken() {
        return "Bearer " + jwtTokenService.crearTokenAdministrador("admin@nautiflow.com");
    }
}
