package com.nautiflow.service;

import jakarta.mail.Session;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.server.ResponseStatusException;

import java.util.Properties;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

                        El equipo de NautiFlow
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
        assertIdentidadVisualCompartida(mensaje, "Actualización sobre tus documentos");
        assertFalse(contenidoPlano(mensaje).contains("estado operativo"));
    }

    @Test
    void rechazoSinMotivoUsaUnTextoAlternativoEnLaNotificacion() throws Exception {
        service.enviarDecisionRechazada(12L, "Ana Pérez", "ana@example.com", null);

        var mensaje = capturarMimeMensaje();

        assertTrue(contenidoPlano(mensaje).contains("📄 Motivo: No especificado"));
        assertTrue(contenidoHtml(mensaje).contains("<strong>Motivo:</strong> No especificado"));
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

                        El equipo de NautiFlow
                        """,
                contenidoPlano(mensaje)
        );
        assertTrue(contenidoPlano(mensaje).contains("¡Hola, Ana Pérez! 👋"));
        assertTrue(contenidoPlano(mensaje).contains("🎉 ¡Felicidades! Tus documentos han sido aprobados."));
        assertTrue(contenidoPlano(mensaje).contains(
                "¡Nos alegra mucho darte la bienvenida al equipo de NautiFlow! 🚤💙"
        ));
        assertTrue(contenidoPlano(mensaje).contains("\n\n"));
        assertTrue(contenidoHtml(mensaje).contains("El equipo de NautiFlow"));
        assertIdentidadVisualCompartida(mensaje, "¡Tus documentos fueron aprobados!");
        assertFalse(contenidoPlano(mensaje).contains("estado operativo"));
    }

    @Test
    void confirmacionDeRegistroUsaElDisenoCompartidoYConservaElDestinatario() throws Exception {
        service.enviarConfirmacionPorCorreo(12L, "ana@example.com");

        MimeMessage mensaje = capturarMimeMensaje();
        assertEquals("ana@example.com", mensaje.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("Solicitud de registro recibida - NautiFlow", mensaje.getSubject());
        assertTrue(contenidoPlano(mensaje).contains("su estado es PENDIENTE"));
        assertTrue(contenidoHtml(mensaje).contains("Recibimos correctamente tus datos y documentos."));
        assertIdentidadVisualCompartida(mensaje, "¡Solicitud recibida!");
    }

    @Test
    void notificacionDeResenaConComentarioEsNeutralYConservaElTexto() throws Exception {
        String comentario = "No fue lo esperado.\n  La salida se retrasó <30 min>.";
        service.notificarResena(
                12L, "ana@example.com", "Ana Pérez", 2, "Zona Norte", "Zona Sur", comentario
        );

        MimeMessage mensaje = capturarMimeMensaje();
        String plano = contenidoPlano(mensaje);
        assertEquals("ana@example.com", mensaje.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("¡Nueva reseña en NautiFlow!", mensaje.getSubject());
        assertTrue(plano.contains("⭐ ¡Nueva reseña en NautiFlow!"));
        assertTrue(plano.contains("Ana Pérez"));
        assertTrue(plano.contains("2 estrellas"));
        assertTrue(plano.contains("Zona Norte"));
        assertTrue(plano.contains("Zona Sur"));
        assertTrue(plano.contains("Comentario del pasajero:\n\n" + comentario));
        assertFalse(plano.toLowerCase().contains("felicidades"));
        assertFalse(plano.toLowerCase().contains("gracias por compartir"));
        assertFalse(plano.contains("idViaje"));
        assertFalse(plano.contains("idReseña"));
        String html = contenidoHtml(mensaje);
        assertTrue(html.contains("Ana Pérez"));
        assertTrue(html.contains("2 estrellas"));
        assertTrue(html.contains("Zona Norte"));
        assertTrue(html.contains("Zona Sur"));
        assertTrue(html.contains("Comentario del pasajero:"));
        assertTrue(html.contains("No fue lo esperado.\n  La salida se retrasó &lt;30 min&gt;."));
        assertFalse(html.toLowerCase().contains("felicidades"));
        assertFalse(html.toLowerCase().contains("gracias por compartir"));
        assertIdentidadVisualCompartida(mensaje, "¡Nueva reseña en NautiFlow!");
    }

    @Test
    void notificacionDeResenaSinComentarioOmiteLaSeccionYConservaPuntuacion() throws Exception {
        service.notificarResena(
                12L, "ana@example.com", "Ana Pérez", 5, "Zona Norte", "Zona Sur", "  "
        );

        MimeMessage mensaje = capturarMimeMensaje();
        String plano = contenidoPlano(mensaje);
        String html = contenidoHtml(mensaje);
        assertEquals("ana@example.com", mensaje.getRecipients(Message.RecipientType.TO)[0].toString());
        assertTrue(plano.contains("Ana Pérez ha calificado con 5 estrellas"));
        assertFalse(plano.contains("Comentario del pasajero"));
        assertFalse(html.contains("Comentario del pasajero"));
        assertFalse(plano.toLowerCase().contains("felicidades"));
        assertFalse(plano.toLowerCase().contains("agradecemos"));
    }

    @Test
    void nuevoViajeSeEnvíaAlCorreoDelLancheroConDetallesEnHtml() throws Exception {
        service.notificarViajeProgramado(new ViajeProgramadoEvent(
                25L,
                12L,
                "lanchero@example.com",
                "María Pérez",
                "Zona <Norte>",
                "Zona Sur",
                OffsetDateTime.parse("2026-11-02T22:22:22.637859Z"),
                2,
                "Ana Pérez",
                "+5491123456789"
        ));

        MimeMessage mensaje = capturarMimeMensaje();
        assertEquals("lanchero@example.com", mensaje.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("¡Tienes un nuevo viaje programado en NautiFlow!", mensaje.getSubject());
        String html = contenidoHtml(mensaje);
        assertTrue(html.contains("María Pérez"));
        assertTrue(html.contains("Zona &lt;Norte&gt;"));
        assertTrue(html.contains("Zona Sur"));
        assertTrue(html.contains("2/11/2026 5:22pm"));
        assertTrue(contenidoPlano(mensaje).contains("Fecha y hora de inicio: 2/11/2026 5:22pm"));
        assertFalse(html.contains("2026-11-02T22:22:22.637859Z"));
        assertTrue(html.contains("2"));
        assertTrue(html.contains("Ana Pérez"));
        assertTrue(html.contains("+5491123456789"));
        assertIdentidadVisualCompartida(mensaje, "¡Nuevo viaje programado!");
    }

    @Test
    void falloAlEnviarCorreoDeNuevoViajeSeRegistraSinPropagarse() {
        doThrow(new org.springframework.mail.MailSendException("smtp failure"))
                .when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> service.notificarViajeProgramado(new ViajeProgramadoEvent(
                25L, 12L, "lanchero@example.com", "María Pérez", "Origen", "Destino",
                OffsetDateTime.parse("2026-11-02T22:22:22.637859Z"),
                2, "Ana Pérez", "+5491123456789"
        )));
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

    private MimeMessage capturarMimeMensaje() {
        var captor = org.mockito.ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }

    private void assertIdentidadVisualCompartida(MimeMessage mensaje, String titulo) throws Exception {
        String html = contenidoHtml(mensaje);
        assertTrue(html.contains("NAUTIFLOW"));
        assertTrue(html.contains(titulo));
        assertTrue(html.contains("background-color:#f1f5f9"));
        assertTrue(html.contains("background-color:#ffffff"));
        assertTrue(html.contains("background-color:#f8fafc"));
        assertTrue(html.contains("border-radius:16px"));
        assertTrue(html.contains("El equipo de NautiFlow"));
        assertTrue(html.contains("name=\"viewport\""));
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
