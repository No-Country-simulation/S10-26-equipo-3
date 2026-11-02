package com.nautiflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ViajeProgramadoNotificationListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViajeProgramadoNotificationListener.class);

    private final RegistroNotificacionService notificacionService;

    public ViajeProgramadoNotificationListener(RegistroNotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

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
