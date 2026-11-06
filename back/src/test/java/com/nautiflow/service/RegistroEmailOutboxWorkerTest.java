package com.nautiflow.service;

import com.nautiflow.repository.RegistroEmailOutboxRepository;
import com.nautiflow.repository.RegistroEmailOutboxPayload;
import com.nautiflow.repository.RegistroEmailOutboxTask;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.*;

class RegistroEmailOutboxWorkerTest {
    private final RegistroEmailOutboxRepository repository = mock(RegistroEmailOutboxRepository.class);
    private final RegistroNotificacionService notificacionService = mock(RegistroNotificacionService.class);
    private final RegistroEmailOutboxWorker worker =
            new RegistroEmailOutboxWorker(repository, notificacionService, 5, "render");

    @Test
    void enviaYMarcaUnaTareaReclamadaPorElRepositorio() {
        RegistroEmailOutboxTask tarea = new RegistroEmailOutboxTask(
                7L, 12L, "usuario@example.com", 1
        );
        when(repository.reclamarSiguiente(5, 120)).thenReturn(Optional.of(tarea));
        when(repository.asegurarPayloadYClave(7L, 1, "payload", "registro-confirmacion:render:7"))
                .thenReturn(new RegistroEmailOutboxPayload("payload", "registro-confirmacion:render:7"));
        when(notificacionService.prepararPayloadConfirmacion("usuario@example.com")).thenReturn("payload");

        worker.procesarSiguiente();

        verify(notificacionService).enviarConfirmacionPorCorreo(
                12L, "payload", "registro-confirmacion:render:7"
        );
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
                .when(notificacionService).enviarConfirmacionPorCorreo(
                        12L, "payload", "registro-lanchero-12"
                );
        when(repository.asegurarPayloadYClave(7L, 3, "payload", "registro-lanchero-12"))
                .thenReturn(new RegistroEmailOutboxPayload("payload", "registro-lanchero-12"));
        when(notificacionService.prepararPayloadConfirmacion("usuario@example.com")).thenReturn("payload");

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
                .when(notificacionService).enviarConfirmacionPorCorreo(
                        12L, "payload", "registro-confirmacion:render:7"
                );
        when(repository.asegurarPayloadYClave(7L, 1, "payload", "registro-confirmacion:render:7"))
                .thenReturn(new RegistroEmailOutboxPayload("payload", "registro-confirmacion:render:7"));
        when(notificacionService.prepararPayloadConfirmacion("usuario@example.com")).thenReturn("payload");

        worker.procesarSiguiente();

        verify(repository).marcarFallida(7L, 1, "RESEND_AUTHENTICATION");
        verify(repository, never()).programarReintento(anyLong(), anyInt(), anyInt(), anyLong(), anyString());
        verify(repository, never()).marcarEnviada(anyLong());
    }

    @Test
    void namespacesSeparanNotificacionesConElMismoIdOutbox() {
        when(repository.reclamarSiguiente(5, 120)).thenReturn(Optional.of(
                new RegistroEmailOutboxTask(7L, 12L, "usuario@example.com", 1)
        ));
        when(repository.asegurarPayloadYClave(anyLong(), anyInt(), anyString(), anyString()))
                .thenAnswer(invocation -> new RegistroEmailOutboxPayload(
                        invocation.getArgument(2), invocation.getArgument(3)
                ));
        when(notificacionService.prepararPayloadConfirmacion(anyString())).thenReturn("payload");

        worker.procesarSiguiente();
        RegistroEmailOutboxWorker localWorker = new RegistroEmailOutboxWorker(repository, notificacionService, 5, "local");
        localWorker.procesarSiguiente();

        verify(notificacionService).enviarConfirmacionPorCorreo(
                12L, "payload", "registro-confirmacion:render:7"
        );
        verify(notificacionService).enviarConfirmacionPorCorreo(
                12L, "payload", "registro-confirmacion:local:7"
        );
    }

    @Test
    void reintentoTras409ReutilizaClaveYPayloadPersistidosYNoHaceEnvioConClaveNueva() {
        AtomicReference<RegistroEmailOutboxPayload> guardado = new AtomicReference<>();
        AtomicInteger reclamaciones = new AtomicInteger();
        when(repository.reclamarSiguiente(5, 120)).thenAnswer(invocation -> {
            if (reclamaciones.getAndIncrement() == 0) {
                return Optional.of(new RegistroEmailOutboxTask(7L, 12L, "usuario@example.com", 1));
            }
            RegistroEmailOutboxPayload previo = guardado.get();
            return Optional.of(new RegistroEmailOutboxTask(
                    7L, 12L, "usuario@example.com", 2, previo.json(), previo.idempotencyKey()
            ));
        });
        when(repository.asegurarPayloadYClave(anyLong(), anyInt(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    guardado.compareAndSet(null, new RegistroEmailOutboxPayload(
                            invocation.getArgument(2), invocation.getArgument(3)
                    ));
                    return guardado.get();
                });
        when(notificacionService.prepararPayloadConfirmacion("usuario@example.com"))
                .thenReturn("body-before-config-change", "body-after-config-change");
        doThrow(new ResendEmailException(
                ResendEmailException.Category.PROVIDER_TEMPORARY,
                409,
                true,
                "idempotent request conflict",
                null,
                "invalid_idempotent_request",
                "body mismatch"
        )).doNothing().when(notificacionService).enviarConfirmacionPorCorreo(
                anyLong(), anyString(), anyString()
        );

        worker.procesarSiguiente();
        worker.procesarSiguiente();

        verify(repository).programarReintento(7L, 1, 5, 30, "RESEND_PROVIDER_TEMPORARY");
        verify(notificacionService, times(1)).prepararPayloadConfirmacion("usuario@example.com");
        verify(notificacionService, times(2)).enviarConfirmacionPorCorreo(
                12L, "body-before-config-change", "registro-confirmacion:render:7"
        );
        verify(repository).marcarEnviada(7L);
    }

    @Test
    void recuperarNotificacionFallidaUsaElSnapshotYClaveGuardados() {
        RegistroEmailOutboxTask recuperada = new RegistroEmailOutboxTask(
                7L, 12L, "usuario@example.com", 4,
                "snapshot-original", "registro-confirmacion:render:7"
        );
        when(repository.reclamarSiguiente(5, 120)).thenReturn(Optional.of(recuperada));
        when(repository.asegurarPayloadYClave(7L, 4, "snapshot-original", "registro-confirmacion:render:7"))
                .thenReturn(new RegistroEmailOutboxPayload("snapshot-original", "registro-confirmacion:render:7"));

        worker.procesarSiguiente();

        verify(notificacionService, never()).prepararPayloadConfirmacion(anyString());
        verify(notificacionService).enviarConfirmacionPorCorreo(
                12L, "snapshot-original", "registro-confirmacion:render:7"
        );
        verify(repository).marcarEnviada(7L);
    }
}
