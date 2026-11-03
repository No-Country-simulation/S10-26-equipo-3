package com.nautiflow.service;

import com.nautiflow.repository.RegistroEmailOutboxRepository;
import com.nautiflow.repository.RegistroEmailOutboxTask;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

class RegistroEmailOutboxWorkerTest {
    private final RegistroEmailOutboxRepository repository = mock(RegistroEmailOutboxRepository.class);
    private final RegistroNotificacionService notificacionService = mock(RegistroNotificacionService.class);
    private final RegistroEmailOutboxWorker worker =
            new RegistroEmailOutboxWorker(repository, notificacionService, 5);

    @Test
    void enviaYMarcaUnaTareaReclamadaPorElRepositorio() {
        RegistroEmailOutboxTask tarea = new RegistroEmailOutboxTask(
                7L, 12L, "usuario@example.com", 2
        );
        when(repository.reclamarSiguiente(5, 120)).thenReturn(Optional.of(tarea));

        worker.procesarSiguiente();

        verify(notificacionService).enviarConfirmacionPorCorreo(12L, "usuario@example.com");
        verify(repository).marcarEnviada(7L);
    }

    @Test
    void reprogramaElCorreoFallidoConDemoraYMaximoDeIntentos() {
        RegistroEmailOutboxTask tarea = new RegistroEmailOutboxTask(
                7L, 12L, "usuario@example.com", 3
        );
        when(repository.reclamarSiguiente(5, 120)).thenReturn(Optional.of(tarea));
        doThrow(new ResendEmailException(
                ResendEmailException.Category.PROVIDER_TEMPORARY,
                503,
                true,
                "provider unavailable",
                new RuntimeException("cause"),
                null,
                null
        ))
                .when(notificacionService).enviarConfirmacionPorCorreo(12L, "usuario@example.com");

        worker.procesarSiguiente();

        verify(repository).programarReintento(7L, 3, 5, 120, "RESEND_PROVIDER_TEMPORARY");
        verify(repository, never()).marcarEnviada(anyLong());
    }

    @Test
    void errorPermanenteMarcaFallidaSinProgramarReintentoNiMarcarEnviada() {
        RegistroEmailOutboxTask tarea = new RegistroEmailOutboxTask(7L, 12L, "usuario@example.com", 1);
        when(repository.reclamarSiguiente(5, 120)).thenReturn(Optional.of(tarea));
        doThrow(new ResendEmailException(
                ResendEmailException.Category.AUTHENTICATION,
                401,
                false,
                "authentication rejected",
                null,
                null,
                null
        ))
                .when(notificacionService).enviarConfirmacionPorCorreo(12L, "usuario@example.com");

        worker.procesarSiguiente();

        verify(repository).marcarFallida(7L, 1, "RESEND_AUTHENTICATION");
        verify(repository, never()).programarReintento(anyLong(), anyInt(), anyInt(), anyLong(), anyString());
        verify(repository, never()).marcarEnviada(anyLong());
    }
}
