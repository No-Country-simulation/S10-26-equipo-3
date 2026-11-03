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
        doThrow(new IllegalStateException("smtp unavailable"))
                .when(notificacionService).enviarConfirmacionPorCorreo(12L, "usuario@example.com");

        worker.procesarSiguiente();

        verify(repository).programarReintento(7L, 3, 5, 120, "IllegalStateException");
        verify(repository, never()).marcarEnviada(anyLong());
    }

    @Test
    void quintoFalloEsEntregadoAlRepositorioParaMarcarLaTareaComoFallida() {
        RegistroEmailOutboxTask tarea = new RegistroEmailOutboxTask(
                7L, 12L, "usuario@example.com", 5
        );
        when(repository.reclamarSiguiente(5, 120)).thenReturn(Optional.of(tarea));
        doThrow(new IllegalStateException("smtp unavailable"))
                .when(notificacionService).enviarConfirmacionPorCorreo(12L, "usuario@example.com");

        worker.procesarSiguiente();

        verify(repository).programarReintento(7L, 5, 5, 480, "IllegalStateException");
    }
}
