package com.nautiflow.service;

import jakarta.mail.Session;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.server.ResponseStatusException;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GmailRegistroNotificacionServiceTest {
    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final GmailRegistroNotificacionService service = crearServicio();

    private GmailRegistroNotificacionService crearServicio() {
        when(mailSender.createMimeMessage()).thenReturn(
                new MimeMessage(Session.getInstance(new Properties()))
        );
        return new GmailRegistroNotificacionService(mailSender, "noreply@nautiflow.com", "app-password");
    }

    @Test
    void rechazoIncluyeNombreMotivoFormatoYCaracteresEspeciales() throws Exception {
        service.enviarDecisionRechazada(12L, "Ana Pérez", "ana@example.com", "Documento <ilegible> & borroso");

        var mensaje = capturarMimeMensaje();
        assertEquals("ana@example.com", mensaje.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("Documentos rechazados - NautiFlow", mensaje.getSubject());
        assertEquals(
                """
                        ¡Hola, Ana Pérez! 👋

                        Lamentamos contarte que, después de revisar tus documentos, no pudimos aprobar tu solicitud en esta ocasión.

                        📄 Motivo: Documento <ilegible> & borroso

                        Te invitamos a revisar la información y realizar los ajustes necesarios para que podamos evaluar nuevamente tu solicitud.

                        ¡Esperamos verte pronto formando parte de NautiFlow! 🚤💙

                        Equipo NautiFlow
                        """,
                contenidoPlano(mensaje)
        );
        assertTrue(contenidoPlano(mensaje).contains("¡Hola, Ana Pérez! 👋"));
        assertTrue(contenidoPlano(mensaje).contains("📄 Motivo: Documento <ilegible> & borroso"));
        assertTrue(contenidoHtml(mensaje).contains("<strong>no pudimos aprobar tu solicitud en esta ocasión.</strong>"));
        assertTrue(contenidoHtml(mensaje).contains(
                "<strong>Motivo:</strong> Documento &lt;ilegible&gt; &amp; borroso"
        ));
        assertTrue(contenidoHtml(mensaje).contains("🚤💙"));
        assertTrue(contenidoPlano(mensaje).contains("\n\n"));
        assertTrue(contenidoHtml(mensaje).contains("<p>"));
        assertFalse(contenidoPlano(mensaje).contains("estado operativo"));
    }

    @Test
    void aprobacionIncluyeMensajeAmigableConEmojisYSaltosDeLinea() throws Exception {
        service.enviarDecisionAprobada(12L, "Ana Pérez", "ana@example.com");

        var mensaje = capturarMimeMensaje();
        assertEquals("ana@example.com", mensaje.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("Documentos aprobados - NautiFlow", mensaje.getSubject());
        assertEquals(
                """
                        ¡Hola, Ana Pérez! 👋

                        🎉 ¡Felicidades! Tus documentos han sido aprobados.

                        ¡Nos alegra mucho darte la bienvenida al equipo de NautiFlow! 🚤💙

                        Estamos felices de que formes parte de esta comunidad. ¡Que este sea el comienzo de muchas buenas experiencias juntos!

                        Equipo NautiFlow
                        """,
                contenidoPlano(mensaje)
        );
        assertTrue(contenidoPlano(mensaje).contains("¡Hola, Ana Pérez! 👋"));
        assertTrue(contenidoPlano(mensaje).contains("🎉 ¡Felicidades! Tus documentos han sido aprobados."));
        assertTrue(contenidoPlano(mensaje).contains(
                "¡Nos alegra mucho darte la bienvenida al equipo de NautiFlow! 🚤💙"
        ));
        assertTrue(contenidoPlano(mensaje).contains("\n\n"));
        assertTrue(contenidoHtml(mensaje).contains("<strong>Equipo NautiFlow</strong>"));
        assertFalse(contenidoPlano(mensaje).contains("estado operativo"));
    }

    @Test
    void notificacionDeResenaIncluyePuntuacionEIdentificadores() {
        service.notificarResena(12L, "ana@example.com", 45L, 25L, 5);

        var mensaje = capturarMensajeSimple();
        assertArrayEquals(new String[]{"ana@example.com"}, mensaje.getTo());
        assertNotNull(mensaje.getText());
        assertTrue(mensaje.getText().contains("5 estrellas"));
        assertTrue(mensaje.getText().contains("viaje 25"));
        assertTrue(mensaje.getText().contains("reseña 45"));
    }

    @Test
    void falloSmtpSePropagaComoServicioNoDisponible() {
        doThrow(new org.springframework.mail.MailSendException("smtp failure"))
                .when(mailSender).send(any(MimeMessage.class));

        assertEquals(503, assertThrows(
                ResponseStatusException.class,
                () -> service.enviarDecisionAprobada(12L, "Ana Pérez", "ana@example.com")
        ).getStatusCode().value());
    }

    private SimpleMailMessage capturarMensajeSimple() {
        var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }

    private MimeMessage capturarMimeMensaje() {
        var captor = org.mockito.ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }

    private String contenidoPlano(MimeMessage mensaje) throws Exception {
        return contenidoMime(mensaje, "text/plain");
    }

    private String contenidoHtml(MimeMessage mensaje) throws Exception {
        return contenidoMime(mensaje, "text/html");
    }

    private String contenidoMime(Part parte, String tipoContenido) throws Exception {
        Object contenido = parte.getContent();
        if (contenido instanceof Multipart multipart) {
            for (int indice = 0; indice < multipart.getCount(); indice++) {
                String contenidoAlternativo = buscarContenidoMime(multipart.getBodyPart(indice), tipoContenido);
                if (contenidoAlternativo != null) {
                    return contenidoAlternativo;
                }
            }
        } else if (parte.isMimeType(tipoContenido)) {
            return contenido.toString();
        }
        throw new AssertionError("No se encontró una parte " + tipoContenido + " en el correo.");
    }

    private String buscarContenidoMime(Part parte, String tipoContenido) throws Exception {
        Object contenido = parte.getContent();
        if (contenido instanceof Multipart multipart) {
            for (int indice = 0; indice < multipart.getCount(); indice++) {
                String contenidoAlternativo = buscarContenidoMime(multipart.getBodyPart(indice), tipoContenido);
                if (contenidoAlternativo != null) {
                    return contenidoAlternativo;
                }
            }
            return null;
        }
        return parte.isMimeType(tipoContenido) ? contenido.toString() : null;
    }
}
