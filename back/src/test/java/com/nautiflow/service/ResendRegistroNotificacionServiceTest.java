package com.nautiflow.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResendRegistroNotificacionServiceTest {
    private final ResendEmailClient resendEmailClient = mock(ResendEmailClient.class);
    private final ResendRegistroNotificacionService service =
            new ResendRegistroNotificacionService(resendEmailClient);

    @Test
    void mantieneElContenidoActualYEnviaElSnapshotConClaveDelOutbox() {
        when(resendEmailClient.crearCuerpoJson(any(CorreoTransaccional.class))).thenReturn("payload");
        String payload = service.prepararPayloadConfirmacion("usuario@example.com");

        org.mockito.ArgumentCaptor<CorreoTransaccional> captor =
                org.mockito.ArgumentCaptor.forClass(CorreoTransaccional.class);
        verify(resendEmailClient).crearCuerpoJson(captor.capture());
        CorreoTransaccional correo = captor.getValue();

        assertEquals("usuario@example.com", correo.destinatario());
        assertEquals("Solicitud de registro recibida - NautiFlow", correo.asunto());
        assertTrue(correo.texto().contains("Recibimos correctamente tus datos y documentos."));
        assertTrue(correo.texto().contains("su estado es PENDIENTE"));
        assertTrue(correo.html().contains("<strong>PENDIENTE</strong>"));
        assertTrue(correo.html().contains("NAUTIFLOW"));

        service.enviarConfirmacionPorCorreo(12L, payload, "registro-confirmacion:render:7");
        verify(resendEmailClient).enviar("payload", "registro-confirmacion:render:7");
    }

    @Test
    void conservaLaExcepcionResendParaQueElOutboxLaClasifique() {
        ResendEmailException original = new ResendEmailException(
                ResendEmailException.Category.TIMEOUT, 0, true, "request timed out",
                new java.net.http.HttpTimeoutException("timed out"), null, null
        );
        doThrow(original).when(resendEmailClient).enviar(any(String.class), any(String.class));

        ResendEmailException recibida = assertThrows(
                ResendEmailException.class,
                () -> service.enviarConfirmacionPorCorreo(12L, "payload", "registro-confirmacion:render:7")
        );

        assertSame(original, recibida);
        assertSame(original.getCause(), recibida.getCause());
    }

    @Test
    void notificaElPromedioActualizadoEnTextoYHtml() {
        service.notificarResena(
                12L,
                "ana@example.com",
                "Ana Pérez",
                5,
                4.3,
                "Zona origen",
                "Zona destino",
                "Excelente"
        );

        org.mockito.ArgumentCaptor<CorreoTransaccional> captor =
                org.mockito.ArgumentCaptor.forClass(CorreoTransaccional.class);
        verify(resendEmailClient).enviar(captor.capture());
        CorreoTransaccional correo = captor.getValue();

        assertTrue(correo.texto().contains("Tu nuevo promedio de calificación es 4.3 estrellas sobre 5."));
        assertTrue(correo.html().contains("Tu nuevo promedio de calificación es"));
        assertTrue(correo.html().contains("<strong>4.3 estrellas sobre 5</strong>"));
    }
}
