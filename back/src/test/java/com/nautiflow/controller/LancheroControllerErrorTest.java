package com.nautiflow.controller;

import com.nautiflow.config.SecurityConfig;
import com.nautiflow.config.JacksonConfig;
import com.nautiflow.dto.LancheroRegistroDTO;
import com.nautiflow.dto.RegistroLancheroResponse;
import com.nautiflow.service.JwtTokenService;
import com.nautiflow.service.LancheroService;
import com.nautiflow.service.LancheroSolicitudParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = LancheroController.class,
        properties = "nautiflow.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
)
@Import({SecurityConfig.class, JacksonConfig.class, JwtTokenService.class, LancheroSolicitudParser.class})
class LancheroControllerErrorTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LancheroService lancheroService;

    @Test
    void jsonMultipartMalFormadoDevuelve400() throws Exception {
        mockMvc.perform(requestWithParts("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/lancheros/registrar"));
    }

    @Test
    void camposObligatoriosVaciosYIdsDeZonaInvalidosDevuelven400() throws Exception {
        mockMvc.perform(requestWithParts("""
                        {"nombreCompleto":"","whatsapp":"","correo":"","contrasena":"",
                         "nombreEmbarcacion":"","capacidadPasajeros":0,"zonasIds":[0]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void archivoDemasiadoGrandeDevuelve413SinDetallesInternos() throws Exception {
        LancheroRegistroDTO solicitud = solicitudValida();
        when(lancheroService.registrarLanchero(
                eq(solicitud), any(MultipartFile.class), any(MultipartFile.class),
                any(MultipartFile.class), anyList()
        )).thenThrow(new com.nautiflow.exception.ArchivoDemasiadoGrandeException("Archivo supera el límite."));

        mockMvc.perform(requestWithParts(jsonSolicitudValida()))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.path").value("/lancheros/registrar"))
                .andExpect(jsonPath("$.exception").doesNotExist());
    }

    @Test
    void registroValidoConservaLaRespuestaActual() throws Exception {
        LancheroRegistroDTO solicitud = solicitudValida();
        when(lancheroService.registrarLanchero(
                eq(solicitud), any(MultipartFile.class), any(MultipartFile.class),
                any(MultipartFile.class), anyList()
        )).thenReturn(new RegistroLancheroResponse(
                12L, 34L, "PENDIENTE", "Solicitud de registro recibida correctamente."
        ));

        mockMvc.perform(requestWithParts(jsonSolicitudValida()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idLanchero").value(12))
                .andExpect(jsonPath("$.idLancha").value(34))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.mensaje").value("Solicitud de registro recibida correctamente."));
        verify(lancheroService).registrarLanchero(
                eq(solicitud), any(MultipartFile.class), any(MultipartFile.class),
                any(MultipartFile.class), anyList()
        );
    }

    @Test
    void registroRechazaContrasenaOmitidaOVacia() throws Exception {
        for (String campoContrasena : List.of("", ",\"contrasena\":\"\"")) {
            String solicitud = """
                    {"nombreCompleto":"Ana Pérez","whatsapp":"+573212684025",
                     "correo":"ana@example.com"%s,"nombreEmbarcacion":"Brisa Marina",
                     "capacidadPasajeros":8,"zonasIds":[1,3]}
                    """.formatted(campoContrasena);
            mockMvc.perform(requestWithParts(solicitud))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("La contraseña es obligatoria")
                    ));
        }

        verifyNoInteractions(lancheroService);
    }

    @Test
    void registroRechazaContrasenaDemasiadoCorta() throws Exception {
        mockMvc.perform(requestWithParts("""
                        {"nombreCompleto":"Ana Pérez","whatsapp":"+573212684025",
                         "correo":"ana@example.com","contrasena":"corta",
                         "nombreEmbarcacion":"Brisa Marina","capacidadPasajeros":8}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("entre 8 y 72 caracteres")
                ));
    }

    private MockMultipartHttpServletRequestBuilder requestWithParts(String solicitud) {
        return multipart("/lancheros/registrar")
                .file(new MockMultipartFile("licencia", "licencia.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[]{1}))
                .file(new MockMultipartFile("seguro", "seguro.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[]{1}))
                .file(new MockMultipartFile(
                        "antecedentes", "antecedentes.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[]{1}
                ))
                .param("solicitud", solicitud);
    }

    private String jsonSolicitudValida() {
        return """
                {"nombreCompleto":"Ana Pérez","whatsapp":"+573212684025",
                 "correo":"ana@example.com","contrasena":"ClaveSegura123",
                 "nombreEmbarcacion":"Brisa Marina","capacidadPasajeros":8,"zonasIds":[1,3]}
                """;
    }

    private LancheroRegistroDTO solicitudValida() {
        return new LancheroRegistroDTO(
                "Ana Pérez", "+573212684025", "ana@example.com", "ClaveSegura123",
                "Brisa Marina", 8, List.of(1L, 3L)
        );
    }
}
