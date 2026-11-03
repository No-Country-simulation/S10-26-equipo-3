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

class ResendRegistroNotificacionServiceTest {
    private final ResendEmailClient resendEmailClient = mock(ResendEmailClient.class);
    private final ResendRegistroNotificacionService service =
            new ResendRegistroNotificacionService(resendEmailClient);

    @Test
    void mantieneElContenidoActualDeLaConfirmacionYUnaClaveEstableDeReintento() {
        service.enviarConfirmacionPorCorreo(12L, "usuario@example.com");

        org.mockito.ArgumentCaptor<CorreoTransaccional> captor =
                org.mockito.ArgumentCaptor.forClass(CorreoTransaccional.class);
        verify(resendEmailClient).enviar(captor.capture(), org.mockito.ArgumentMatchers.eq("registro-lanchero-12"));
        CorreoTransaccional correo = captor.getValue();

        assertEquals("usuario@example.com", correo.destinatario());
        assertEquals("Solicitud de registro recibida - NautiFlow", correo.asunto());
        assertTrue(correo.texto().contains("Recibimos correctamente tus datos y documentos."));
        assertTrue(correo.texto().contains("su estado es PENDIENTE"));
        assertTrue(correo.html().contains("<strong>PENDIENTE</strong>"));
        assertTrue(correo.html().contains("NAUTIFLOW"));
    }

    @Test
    void conservaLaExcepcionResendParaQueElOutboxLaClasifique() {
        ResendEmailException original = new ResendEmailException(
                ResendEmailException.Category.TIMEOUT, 0, true, "request timed out",
                new java.net.http.HttpTimeoutException("timed out"), null, null
        );
        doThrow(original).when(resendEmailClient).enviar(any(CorreoTransaccional.class), any(String.class));

        ResendEmailException recibida = assertThrows(
                ResendEmailException.class,
                () -> service.enviarConfirmacionPorCorreo(12L, "usuario@example.com")
        );

        assertSame(original, recibida);
        assertSame(original.getCause(), recibida.getCause());
    }
}
