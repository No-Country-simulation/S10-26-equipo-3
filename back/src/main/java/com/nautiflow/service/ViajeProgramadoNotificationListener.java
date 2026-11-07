package com.nautiflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Escucha el evento de viaje programado y envía el correo al lanchero de forma asíncrona.
 */
@Component
public class ViajeProgramadoNotificationListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViajeProgramadoNotificationListener.class);

    private final RegistroNotificacionService notificacionService;

    /**
     * Inyecta el servicio de notificaciones.
     */
    public ViajeProgramadoNotificationListener(RegistroNotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /**
     * Escucha el evento después del commit y envía el correo sin bloquear la transacción.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviarCorreo(ViajeProgramadoEvent viaje) {
        try {
            notificacionService.notificarViajeProgramado(viaje);
        } catch (RuntimeException exception) {
            LOGGER.error("Falló la notificación del viaje {} al lanchero {}.",
                    viaje.idViaje(), viaje.idLanchero(), exception);
        }
    }
}
