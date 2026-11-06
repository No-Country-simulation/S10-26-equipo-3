package com.nautiflow.exception;

import com.nautiflow.exception.handler.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiExceptionHandlerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UploadExceptionController())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void limiteDeArchivoSuperadoDevuelve413ConFormatoApi() throws Exception {
        mockMvc.perform(multipart("/test/upload"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.error").value("Payload Too Large"))
                .andExpect(jsonPath("$.path").value("/test/upload"));
    }

    @Test
    void limiteEnvueltoEnErrorMultipartTambienDevuelve413() throws Exception {
        mockMvc.perform(multipart("/test/upload").param("wrapped", "true"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.path").value("/test/upload"));
    }

    @RestController
    static class UploadExceptionController {
        @PostMapping("/test/upload")
        void upload(@RequestParam(defaultValue = "false") boolean wrapped) {
            MaxUploadSizeExceededException tooLarge = new MaxUploadSizeExceededException(1);
            if (wrapped) {
                throw new MultipartException("Request failed", tooLarge);
            }
            throw tooLarge;
        }
    }
}
