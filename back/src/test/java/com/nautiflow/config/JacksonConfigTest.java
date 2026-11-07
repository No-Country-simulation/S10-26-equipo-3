package com.nautiflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nautiflow.dto.ViajeAdminDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JacksonConfigTest {
    private final ObjectMapper objectMapper = new JacksonConfig().objectMapper();

    @Test
    void serializaOffsetDateTimeEnZonaArgentinaConPrecisionDeMicrosegundos() throws Exception {
        OffsetDateTime fecha = OffsetDateTime.parse("2026-11-02T13:22:22.682941899-05:00");

        assertEquals("\"2026-11-02T15:22:22.682942-03:00\"", objectMapper.writeValueAsString(fecha));
    }

    @Test
    void conservaPrecisionDeMicrosegundosSinRedondeoAdicional() throws Exception {
        OffsetDateTime fecha = OffsetDateTime.parse("2026-11-02T18:22:22.682941Z");

        assertEquals("\"2026-11-02T15:22:22.682941-03:00\"", objectMapper.writeValueAsString(fecha));
    }

    @Test
    void mantieneFechasCalendarioYCamposTemporalesNulos() throws Exception {
        assertEquals("\"2026-11-02\"", objectMapper.writeValueAsString(LocalDate.of(2026, 11, 2)));

        ViajeAdminDTO viaje = new ViajeAdminDTO(
                1L, 2L, 3L, "Lanchero", "Origen", "Destino",
                OffsetDateTime.parse("2026-11-02T18:22:22.682941Z"),
                null, 2, "Pasajero", "+573001234567"
        );
        String json = objectMapper.writeValueAsString(viaje);

        assertTrue(json.contains("\"fechaHoraInicio\":\"2026-11-02T15:22:22.682941-03:00\""));
        assertTrue(json.contains("\"fechaHoraFin\":null"));
    }
}
