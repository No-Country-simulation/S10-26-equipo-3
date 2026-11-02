package com.nautiflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import java.nio.charset.StandardCharsets;

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

    @Override
    public void enviarDecisionAprobada(Long idLanchero, String nombreCompleto, String correo) {
        String nombreHtml = escaparHtml(nombreCompleto);
        enviarNotificacionEstrictaHtml(
                idLanchero,
                correo,
                "Documentos aprobados - NautiFlow",
                """
                        ¡Hola, %s! 👋

                        🎉 ¡Felicidades! Tus documentos han sido aprobados.

                        ¡Nos alegra mucho darte la bienvenida al equipo de NautiFlow! 🚤💙

                        Estamos felices de que formes parte de esta comunidad. ¡Que este sea el comienzo de muchas buenas experiencias juntos!

                        Equipo NautiFlow
                        """.formatted(nombreCompleto),
                """
                        <html>
                        <body>
                        <p>¡Hola, %s! 👋</p>
                        <p>🎉 ¡Felicidades! Tus documentos han sido aprobados.</p>
                        <p>¡Nos alegra mucho darte la bienvenida al equipo de NautiFlow! 🚤💙</p>
                        <p>Estamos felices de que formes parte de esta comunidad. ¡Que este sea el comienzo de muchas buenas experiencias juntos!</p>
                        <p><strong>Equipo NautiFlow</strong></p>
                        </body>
                        </html>
                        """.formatted(nombreHtml)
        );
    }

    @Override
    public void enviarDecisionRechazada(
            Long idLanchero,
            String nombreCompleto,
            String correo,
            String motivoPersistido
    ) {
        String nombreHtml = escaparHtml(nombreCompleto);
        String motivoHtml = escaparHtml(motivoPersistido);
        enviarNotificacionEstrictaHtml(
                idLanchero,
                correo,
                "Documentos rechazados - NautiFlow",
                """
                        ¡Hola, %s! 👋

                        Lamentamos contarte que, después de revisar tus documentos, no pudimos aprobar tu solicitud en esta ocasión.

                        📄 Motivo: %s

                        Te invitamos a revisar la información y realizar los ajustes necesarios para que podamos evaluar nuevamente tu solicitud.

                        ¡Esperamos verte pronto formando parte de NautiFlow! 🚤💙

                        Equipo NautiFlow
                        """.formatted(nombreCompleto, motivoPersistido),
                """
                        <html>
                        <body>
                        <p>¡Hola, %s! 👋</p>
                        <p>Lamentamos contarte que, después de revisar tus documentos, <strong>no pudimos aprobar tu solicitud en esta ocasión.</strong></p>
                        <p>📄 <strong>Motivo:</strong> %s</p>
                        <p>Te invitamos a revisar la información y realizar los ajustes necesarios para que podamos evaluar nuevamente tu solicitud.</p>
                        <p>¡Esperamos verte pronto formando parte de NautiFlow! 🚤💙</p>
                        <p><strong>Equipo NautiFlow</strong></p>
                        </body>
                        </html>
                        """.formatted(nombreHtml, motivoHtml)
        );
    }

    @Override
    public void notificarResena(Long idLanchero, String correo, Long idResena, Long idViaje, int estrellas) {
        enviarNotificacionEstrica(
                idLanchero,
                correo,
                "Recibiste una reseña - NautiFlow",
                "Recibiste una calificación de " + estrellas + " estrellas para el viaje " + idViaje
                        + " (reseña " + idResena + ").\n\nEl equipo de NautiFlow"
        );
    }

    private void enviarNotificacionEstrica(Long idLanchero, String correo, String asunto, String texto) {
        if (!configuracionLista) {
            logger.error("No se pudo enviar la notificación para lanchero {}: SMTP no configurado.", idLanchero);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo enviar la notificación. La operación no fue confirmada."
            );
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(correo);
        mensaje.setSubject(asunto);
        mensaje.setText(texto);
        try {
            mailSender.send(mensaje);
            logger.info("Notificación enviada para lanchero {}.", idLanchero);
        } catch (MailException exception) {
            logger.error(
                    "No se pudo enviar la notificación para lanchero {}. Error SMTP: {}.",
                    idLanchero,
                    exception.getClass().getSimpleName()
            );
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo enviar la notificación. La operación no fue confirmada."
            );
        }
    }

    private void enviarNotificacionEstrictaHtml(
            Long idLanchero,
            String correo,
            String asunto,
            String textoPlano,
            String textoHtml
    ) {
        if (!configuracionLista) {
            logger.error("No se pudo enviar la notificación para lanchero {}: SMTP no configurado.", idLanchero);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo enviar la notificación. La operación no fue confirmada."
            );
        }

        MimeMessage mensaje = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mensaje,
                    false,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(remitente);
            helper.setTo(correo);
            helper.setSubject(asunto);
            MimeBodyPart parteTexto = new MimeBodyPart();
            parteTexto.setText(textoPlano, StandardCharsets.UTF_8.name());
            MimeBodyPart parteHtml = new MimeBodyPart();
            parteHtml.setContent(textoHtml, "text/html; charset=UTF-8");
            MimeMultipart alternativas = new MimeMultipart("alternative");
            alternativas.addBodyPart(parteTexto);
            alternativas.addBodyPart(parteHtml);
            mensaje.setContent(alternativas);
            mensaje.saveChanges();
            mailSender.send(mensaje);
            logger.info("Notificación enviada para lanchero {}.", idLanchero);
        } catch (MailException | MessagingException exception) {
            logger.error(
                    "No se pudo enviar la notificación para lanchero {}. Error SMTP: {}.",
                    idLanchero,
                    exception.getClass().getSimpleName()
            );
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo enviar la notificación. La operación no fue confirmada."
            );
        }
    }

    private String escaparHtml(String valor) {
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
