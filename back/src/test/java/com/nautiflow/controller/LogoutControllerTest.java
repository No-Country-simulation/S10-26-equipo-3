package com.nautiflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nautiflow.config.JacksonConfig;
import com.nautiflow.config.SecurityConfig;
import com.nautiflow.dto.AdminDashboardDTO;
import com.nautiflow.service.AdminAuthService;
import com.nautiflow.service.AdminService;
import com.nautiflow.service.JwtTokenService;
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
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {LogoutController.class, AdminDashboardController.class, LoginController.class},
        properties = "nautiflow.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
)
@Import({SecurityConfig.class, JacksonConfig.class, JwtTokenService.class, AdminAuthService.class})
class LogoutControllerTest {
    private static final String JWT_SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    @Test
    void logoutRevocaElTokenYUnaNuevaSesionSigueFuncionando() throws Exception {
        when(adminService.obtenerDashboard()).thenReturn(new AdminDashboardDTO(1, 2, 3));
        String tokenCerrado = iniciarSesion();

        mockMvc.perform(post("/logout").header("Authorization", "Bearer " + tokenCerrado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Sesión cerrada correctamente"));

        mockMvc.perform(get("/admin/dashboard").header("Authorization", "Bearer " + tokenCerrado))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(adminService);

        esperarSiguienteSegundoUtc();
        String tokenNuevo = iniciarSesion();
        assertNotEquals(tokenCerrado, tokenNuevo);
        mockMvc.perform(get("/admin/dashboard").header("Authorization", "Bearer " + tokenNuevo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solicitudesPendientes").value(1));
        verify(adminService).obtenerDashboard();
    }

    @Test
    void cerrarUnaSesionNoRevocaOtroToken() throws Exception {
        String tokenCerrado = jwtTokenService.crearTokenAdministrador("sesion-cerrada@example.test");
        String tokenIndependiente = jwtTokenService.crearTokenAdministrador("otra-sesion@example.test");
        when(adminService.obtenerDashboard()).thenReturn(new AdminDashboardDTO(0, 0, 0));

        mockMvc.perform(post("/logout").header("Authorization", "Bearer " + tokenCerrado))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/dashboard").header("Authorization", "Bearer " + tokenIndependiente))
                .andExpect(status().isOk());
        verify(adminService).obtenerDashboard();
    }

    @Test
    void logoutSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutConTokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(post("/logout").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutConTokenExpiradoDevuelve401() throws Exception {
        Instant ahora = Instant.now();
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
        String tokenExpirado = Jwts.builder()
                .subject("admin-expirado@example.test")
                .claim("role", "ADMIN")
                .issuedAt(Date.from(ahora.minusSeconds(120)))
                .expiration(Date.from(ahora.minusSeconds(60)))
                .signWith(key)
                .compact();

        mockMvc.perform(post("/logout").header("Authorization", "Bearer " + tokenExpirado))
                .andExpect(status().isUnauthorized());
    }

    private String iniciarSesion() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@nautiflow.com","password":"Admin123*"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("token")
                .asText();
    }

    private void esperarSiguienteSegundoUtc() throws InterruptedException {
        long segundoActual = Instant.now().getEpochSecond();
        while (Instant.now().getEpochSecond() == segundoActual) {
            Thread.sleep(10);
        }
    }
}
