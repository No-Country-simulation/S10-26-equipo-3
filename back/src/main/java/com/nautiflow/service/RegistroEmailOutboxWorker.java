package com.nautiflow.service;

import com.nautiflow.repository.RegistroEmailOutboxRepository;
import com.nautiflow.repository.RegistroEmailOutboxPayload;
import com.nautiflow.repository.RegistroEmailOutboxTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RegistroEmailOutboxWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistroEmailOutboxWorker.class);
    private static final long LEASE_SEGUNDOS = 120;

    private final RegistroEmailOutboxRepository outboxRepository;
    private final RegistroNotificacionService notificacionService;
    private final int maximoIntentos;
    private final String namespaceEntorno;

    public RegistroEmailOutboxWorker(
            RegistroEmailOutboxRepository outboxRepository,
            RegistroNotificacionService notificacionService,
            @Value("${nautiflow.registration.email.max-attempts:5}") int maximoIntentos,
            @Value("${nautiflow.mail.idempotency-namespace:local}") String namespaceEntorno
    ) {
        if (maximoIntentos < 1) {
            throw new IllegalArgumentException("El máximo de intentos de correo debe ser positivo.");
        }
        if (namespaceEntorno == null || !namespaceEntorno.matches("[A-Za-z0-9._-]{1,200}")) {
            throw new IllegalArgumentException("El namespace de idempotencia debe contener 1-200 caracteres seguros.");
        }
        this.outboxRepository = outboxRepository;
        this.notificacionService = notificacionService;
        this.maximoIntentos = maximoIntentos;
        this.namespaceEntorno = namespaceEntorno;
    }

    @Scheduled(fixedDelayString = "${nautiflow.registration.email.poll-interval-ms:1000}")
    public void procesarSiguiente() {
        Optional<RegistroEmailOutboxTask> tareaPendiente;
        try {
            tareaPendiente = outboxRepository.reclamarSiguiente(maximoIntentos, LEASE_SEGUNDOS);
        } catch (RuntimeException exception) {
            LOGGER.error("No se pudo reclamar una notificación pendiente del outbox.", exception);
            return;
        }
        if (tareaPendiente.isEmpty()) {
            return;
        }

        RegistroEmailOutboxTask tarea = tareaPendiente.get();
        try {
            String payloadJson = tarea.payloadJson() == null
                    ? notificacionService.prepararPayloadConfirmacion(tarea.correo())
                    : tarea.payloadJson();
            String claveIdempotencia = tarea.idempotencyKey() == null
                    ? crearClaveIdempotencia(tarea)
                    : tarea.idempotencyKey();
            RegistroEmailOutboxPayload envio = outboxRepository.asegurarPayloadYClave(
                    tarea.id(), tarea.intento(), payloadJson, claveIdempotencia
            );
            notificacionService.enviarConfirmacionPorCorreo(
                    tarea.idLanchero(), envio.json(), envio.idempotencyKey()
            );
        } catch (RuntimeException exception) {
            if (exception instanceof ResendEmailException resendException && !resendException.isRetryable()) {
                registrarFalloPermanente(tarea, resendException);
                return;
            }
            String categoriaError = exception instanceof ResendEmailException resendException
                    ? resendException.getErrorCode()
                    : "RESEND_UNEXPECTED";
            registrarReintento(tarea, exception, categoriaError);
            return;
        }

        outboxRepository.marcarEnviada(tarea.id());
        LOGGER.info("Notificación de registro {} enviada correctamente.", tarea.id());
    }

    private String crearClaveIdempotencia(RegistroEmailOutboxTask tarea) {
        if (tarea.intento() > 1) {
            // Older outbox rows may already have reached Resend with this key before snapshots were persisted.
            return "registro-lanchero-" + tarea.idLanchero();
        }
        return "registro-confirmacion:" + namespaceEntorno + ":" + tarea.id();
    }

    private void registrarReintento(
            RegistroEmailOutboxTask tarea,
            RuntimeException exception,
            String categoriaError
    ) {
        long demoraSegundos = Math.min(30L << Math.min(tarea.intento() - 1, 7), 3600);
        try {
            outboxRepository.programarReintento(
                    tarea.id(),
                    tarea.intento(),
                    maximoIntentos,
                    demoraSegundos,
                    categoriaError
            );
            LOGGER.warn(
                    "Falló el envío de la notificación de registro {}; intento {}/{}; categoría={}; causas={}",
                    tarea.id(),
                    tarea.intento(),
                    maximoIntentos,
                    categoriaError,
                    exception instanceof ResendEmailException resendException
                            ? resendException.getCauseTypes()
                            : exception.getClass().getSimpleName()
            );
        } catch (RuntimeException persistenciaException) {
            exception.addSuppressed(persistenciaException);
            LOGGER.error(
                    "No se pudo registrar el reintento de la notificación {}; la tarea quedará recuperable al expirar el lease.",
                    tarea.id(),
                    exception
            );
        }
    }

    private void registrarFalloPermanente(RegistroEmailOutboxTask tarea, ResendEmailException exception) {
        try {
            outboxRepository.marcarFallida(tarea.id(), tarea.intento(), exception.getErrorCode());
            LOGGER.error(
                    "Error permanente al enviar la notificación de registro {}; intento {}/{}; categoría={}, HTTP {}.",
                    tarea.id(),
                    tarea.intento(),
                    maximoIntentos,
                    exception.getCategory(),
                    exception.getHttpStatus()
            );
        } catch (RuntimeException persistenciaException) {
            exception.addSuppressed(persistenciaException);
            LOGGER.error(
                    "No se pudo marcar como fallida la notificación {}; la tarea quedará recuperable al expirar el lease.",
                    tarea.id(),
                    exception
            );
        }
    }
}
