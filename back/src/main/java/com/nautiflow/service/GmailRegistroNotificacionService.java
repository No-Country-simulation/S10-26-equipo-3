package com.nautiflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GmailRegistroNotificacionService implements RegistroNotificacionService {
    private static final Logger logger = LoggerFactory.getLogger(GmailRegistroNotificacionService.class);
    private static final String ASUNTO = "Solicitud de registro recibida - NautiFlow";
    private static final String MENSAJE = """
            ¡Hola! 👋

            ¡Gracias por sumarte a NautiFlow! Recibimos correctamente tus datos y documentos.
            Nuestro equipo revisará tu solicitud; por ahora, su estado es PENDIENTE.

            Te avisaremos por este medio cuando tengamos novedades.

            ¡Nos alegra que quieras navegar con nosotros! 🚤

            El equipo de NautiFlow
            """;

    private final JavaMailSender mailSender;
    private final String remitente;

    public GmailRegistroNotificacionService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String remitente,
            @Value("${spring.mail.password:}") String claveAplicacion
    ) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.configuracionLista = StringUtils.hasText(remitente) && StringUtils.hasText(claveAplicacion);
    }

    private final boolean configuracionLista;

    @Override
    public void enviarConfirmacionPorCorreo(Long idLanchero, String correo) {
        if (!configuracionLista) {
            logger.warn("No se envió el correo de confirmación para lanchero {}: configura GMAIL_USERNAME y GMAIL_APP_PASSWORD.", idLanchero);
            return;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(correo);
        mensaje.setSubject(ASUNTO);
        mensaje.setText(MENSAJE);
        try {
            mailSender.send(mensaje);
            logger.info("Correo de confirmación enviado para lanchero {}.", idLanchero);
        } catch (MailException exception) {
            logger.error(
                    "No se pudo enviar el correo de confirmación para lanchero {}. Error SMTP: {}.",
                    idLanchero,
                    exception.getClass().getSimpleName()
            );
        }
    }
}
