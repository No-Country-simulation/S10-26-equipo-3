package com.nautiflow.controller;

import com.nautiflow.config.JacksonConfig;
import com.nautiflow.config.SecurityConfig;
import com.nautiflow.dto.LanchaDestacadaDTO;
import com.nautiflow.dto.PaginaLanchasDestacadasDTO;
import com.nautiflow.service.JwtTokenService;
import com.nautiflow.service.LanchasDestacadasService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = LanchasDestacadasController.class,
        properties = "nautiflow.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
)
@Import({SecurityConfig.class, JacksonConfig.class, JwtTokenService.class})
class LanchasDestacadasControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LanchasDestacadasService lanchasDestacadasService;

    @Test
    void permiteConsultaPublicaYDevuelveSoloCamposDelDto() throws Exception {
        when(lanchasDestacadasService.listar()).thenReturn(new PaginaLanchasDestacadasDTO(
                List.of(
                        new LanchaDestacadaDTO(
                                1L, "La Porteña III", "Juan Pablo Gómez González",
                                "https://storage.example/portena.webp", 4.0, 12, true
                        ),
                        new LanchaDestacadaDTO(
                                2L, "Brisa Marina", "Carlos Pérez", null, null, 0, true
                        )
                ),
                0,
                10,
                1,
                1
        ));

        mockMvc.perform(get("/lanchas/destacadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idLancha").value(1))
                .andExpect(jsonPath("$.content[0].nombreEmbarcacion").value("La Porteña III"))
                .andExpect(jsonPath("$.content[0].lancheroNombreCompleto").value("Juan Pablo Gómez González"))
                .andExpect(jsonPath("$.content[0].fotoPrincipalUrl").value("https://storage.example/portena.webp"))
                .andExpect(jsonPath("$.content[0].promedioCalificacion").value(4.0))
                .andExpect(jsonPath("$.content[0].totalResenas").value(12))
                .andExpect(jsonPath("$.content[0].verificada").value(true))
                .andExpect(jsonPath("$.content[0].whatsapp").doesNotExist())
                .andExpect(jsonPath("$.content[0].correo").doesNotExist())
                .andExpect(jsonPath("$.content[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.content[1].promedioCalificacion").value(nullValue()))
                .andExpect(jsonPath("$.content[1].totalResenas").value(0))
                .andExpect(jsonPath("$.content[1].fotoPrincipalUrl").value(nullValue()));

        verify(lanchasDestacadasService).listar();
    }
}
