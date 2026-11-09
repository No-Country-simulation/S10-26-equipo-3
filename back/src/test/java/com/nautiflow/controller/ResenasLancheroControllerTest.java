package com.nautiflow.controller;

import com.nautiflow.config.JacksonConfig;
import com.nautiflow.config.SecurityConfig;
import com.nautiflow.dto.PaginaResenasPublicasDTO;
import com.nautiflow.dto.ResenaPublicaDTO;
import com.nautiflow.service.JwtTokenService;
import com.nautiflow.service.ResenasLancheroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ResenasLancheroController.class,
        properties = "nautiflow.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
)
@Import({SecurityConfig.class, JacksonConfig.class, JwtTokenService.class})
class ResenasLancheroControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResenasLancheroService resenasLancheroService;

    @Test
    void consultaPublicaConPaginacionYNoExponeDatosDelTurista() throws Exception {
        when(resenasLancheroService.listar(12L, 1, 2)).thenReturn(new PaginaResenasPublicasDTO(
                List.of(new ResenaPublicaDTO(
                        25L,
                        5,
                        "Excelente experiencia.",
                        OffsetDateTime.parse("2026-11-03T18:20:00Z")
                )),
                1,
                2,
                3,
                2
        ));

        mockMvc.perform(get("/lancheros/12/resenas")
                        .queryParam("page", "1")
                        .queryParam("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idResena").value(25))
                .andExpect(jsonPath("$.content[0].puntuacion").value(5))
                .andExpect(jsonPath("$.content[0].comentario").value("Excelente experiencia."))
                .andExpect(jsonPath("$.content[0].fecha").value("2026-11-03T15:20:00.000000-03:00"))
                .andExpect(jsonPath("$.content[0].turistaNombreCompleto").doesNotExist())
                .andExpect(jsonPath("$.content[0].turistaWhatsapp").doesNotExist())
                .andExpect(jsonPath("$.content[0].correo").doesNotExist())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));

        verify(resenasLancheroService).listar(12L, 1, 2);
    }

    @Test
    void lancheroInexistenteDevuelve404() throws Exception {
        when(resenasLancheroService.listar(99L, 0, 10))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lanchero no encontrado."));

        mockMvc.perform(get("/lancheros/99/resenas"))
                .andExpect(status().isNotFound());
    }

    @Test
    void lancheroSinResenasDevuelveListaVacia() throws Exception {
        when(resenasLancheroService.listar(12L, 0, 10))
                .thenReturn(new PaginaResenasPublicasDTO(List.of(), 0, 10, 0, 0));

        mockMvc.perform(get("/lancheros/12/resenas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }
}
