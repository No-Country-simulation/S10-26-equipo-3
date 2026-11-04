package com.nautiflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Implementación de las notificaciones por correo con Resend: confirmación de registro, decisiones, reseñas y viajes programados.
 */
@Service
public class ResendRegistroNotificacionService implements RegistroNotificacionService {
    private static final Logger logger = LoggerFactory.getLogger(ResendRegistroNotificacionService.class);
    private static final String ASUNTO_REGISTRO = "Solicitud de registro recibida - NautiFlow";
    private static final String TEXTO_REGISTRO = """
            ¡Hola! 👋

            ¡Gracias por sumarte a NautiFlow! Recibimos correctamente tus datos y documentos.
            Nuestro equipo revisará tu solicitud; por ahora, su estado es PENDIENTE.

            Te avisaremos por este medio cuando tengamos novedades.

            ¡Nos alegra que quieras navegar con nosotros! 🚤

            El equipo de NautiFlow
            """;
    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter FORMATO_FECHA_VIAJE =
            DateTimeFormatter.ofPattern("d/M/uuuu h:mma", Locale.ROOT);

    private final ResendEmailClient resendEmailClient;

    /**
     * Inyecta el cliente de Resend.
     */
    public ResendRegistroNotificacionService(ResendEmailClient resendEmailClient) {
        this.resendEmailClient = resendEmailClient;
    }

    /**
     * Arma el correo de confirmación de registro y devuelve el JSON listo para enviar.
     */
    @Override
    public String prepararPayloadConfirmacion(String correo) {
        String contenidoHtml = """
                <p>¡Hola! 👋</p>
                <p>¡Gracias por sumarte a NautiFlow! Recibimos correctamente tus datos y documentos.</p>
                <p>Nuestro equipo revisará tu solicitud; por ahora, su estado es <strong>PENDIENTE</strong>.</p>
                <p>Te avisaremos por este medio cuando tengamos novedades.</p>
                <p>¡Nos alegra que quieras navegar con nosotros! 🚤</p>
                """;
        return resendEmailClient.crearCuerpoJson(new CorreoTransaccional(
                correo,
                ASUNTO_REGISTRO,
                TEXTO_REGISTRO,
                CorreoHtmlTemplate.renderizar("¡Solicitud recibida!", "⚓", contenidoHtml)
        ));
    }

    /**
     * Envía el correo de confirmación y registra el tiempo que tardó.
     */
    @Override
    public void enviarConfirmacionPorCorreo(Long idLanchero, String payloadJson, String claveIdempotencia) {
        long inicioEnvio = System.nanoTime();
        try {
            resendEmailClient.enviar(payloadJson, claveIdempotencia);
            logger.info("Correo de confirmación enviado para lanchero {}.", idLanchero);
        } finally {
            logger.info("Tiempo total de envío de confirmación de registro: {} ms", milisegundosDesde(inicioEnvio));
        }
    }

    /**
     * Calcula los milisegundos transcurridos desde un instante en nanosegundos.
     */
    private long milisegundosDesde(long inicioNano) {
        return (System.nanoTime() - inicioNano) / 1_000_000;
    }

    /**
     * Envía el correo de aprobación con el nombre del lanchero.
     */
    @Override
    public void enviarDecisionAprobada(Long idLanchero, String nombreCompleto, String correo) {
        String nombreHtml = CorreoHtmlTemplate.escapar(nombreCompleto);
        enviarNotificacionEstrictaHtml(
                idLanchero,
                correo,
                "Documentos aprobados - NautiFlow",
                "🎉",
                "¡Tus documentos fueron aprobados!",
                """
                        ¡Hola, %s! 👋

                        🎉 ¡Felicidades! Tus documentos han sido aprobados.

                        ¡Nos alegra mucho darte la bienvenida al equipo de NautiFlow! 🚤💙

                        Estamos felices de que formes parte de esta comunidad. ¡Que este sea el comienzo de muchas buenas experiencias juntos!

                        El equipo de NautiFlow
                        """.formatted(nombreCompleto),
                """
                        <p>¡Hola, <strong>%s</strong>! 👋</p>
                        <p>🎉 ¡Felicidades! Tus documentos han sido aprobados.</p>
                        <p>¡Nos alegra mucho darte la bienvenida al equipo de NautiFlow! 🚤💙</p>
                        <p>Estamos felices de que formes parte de esta comunidad. ¡Que este sea el comienzo de muchas buenas experiencias juntos!</p>
                        """.formatted(nombreHtml)
        );
    }

    /**
     * Envía el correo de rechazo con el motivo persistido.
     */
    @Override
    public void enviarDecisionRechazada(
            Long idLanchero,
            String nombreCompleto,
            String correo,
            String motivoPersistido
    ) {
        String motivo = motivoPersistido == null ? "No especificado" : motivoPersistido;
        String nombreHtml = CorreoHtmlTemplate.escapar(nombreCompleto);
        String motivoHtml = CorreoHtmlTemplate.escapar(motivo);
        enviarNotificacionEstrictaHtml(
                idLanchero,
                correo,
                "Documentos rechazados - NautiFlow",
                "📄",
                "Actualización sobre tus documentos",
                """
                        ¡Hola, %s! 👋

                        Lamentamos contarte que, después de revisar tus documentos, no pudimos aprobar tu solicitud en esta ocasión.

                        📄 Motivo: %s

                        Te invitamos a revisar la información y realizar los ajustes necesarios para que podamos evaluar nuevamente tu solicitud.

                        ¡Esperamos verte pronto formando parte de NautiFlow! 🚤💙

                        El equipo de NautiFlow
                        """.formatted(nombreCompleto, motivo),
                """
                        <p>¡Hola, <strong>%s</strong>! 👋</p>
                        <p>Lamentamos contarte que, después de revisar tus documentos, <strong>no pudimos aprobar tu solicitud en esta ocasión.</strong></p>
                        <p>📄 <strong>Motivo:</strong> %s</p>
                        <p>Te invitamos a revisar la información y realizar los ajustes necesarios para que podamos evaluar nuevamente tu solicitud.</p>
                        <p>¡Esperamos verte pronto formando parte de NautiFlow! 🚤💙</p>
                        """.formatted(nombreHtml, motivoHtml)
        );
    }

    /**
     * Envía al lanchero el correo con la calificación y el comentario de la reseña.
     */
    @Override
    public void notificarResena(
            Long idLanchero,
            String correo,
            String nombreTurista,
            int estrellas,
            String zonaOrigen,
            String zonaDestino,
            String comentario
    ) {
        String comentarioPlano = comentario != null && !comentario.isBlank()
                ? "\n\nComentario del pasajero:\n\n" + comentario
                : "";
        String comentarioHtml = comentario != null && !comentario.isBlank()
                ? """
                <tr>
                  <td style="padding:12px">
                    <strong>Comentario del pasajero:</strong>
                    <div style="margin-top:8px;white-space:pre-wrap;overflow-wrap:anywhere">%s</div>
                  </td>
                </tr>
                """.formatted(CorreoHtmlTemplate.escapar(comentario))
                : "";
        String texto = """
                ⭐ ¡Nueva reseña en NautiFlow!

                %s ha calificado con %s estrellas su viaje desde %s hasta %s.%s

                El equipo de NautiFlow
                """.formatted(nombreTurista, estrellas, zonaOrigen, zonaDestino, comentarioPlano);
        String html = """
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0"
                       style="width:100%%;border-collapse:collapse">
                  <tr>
                    <td style="padding:12px">
                      <strong>%s</strong> ha calificado con <strong>%s estrellas</strong> su viaje desde
                      <strong>%s</strong> hasta <strong>%s</strong>.
                    </td>
                  </tr>
                  %s
                </table>
                """.formatted(
                CorreoHtmlTemplate.escapar(nombreTurista),
                estrellas,
                CorreoHtmlTemplate.escapar(zonaOrigen),
                CorreoHtmlTemplate.escapar(zonaDestino),
                comentarioHtml
        );
        enviarNotificacionEstrictaHtml(
                idLanchero,
                correo,
                "¡Nueva reseña en NautiFlow!",
                "⭐",
                "¡Nueva reseña en NautiFlow!",
                texto,
                html
        );
    }

    /**
     * Envía al lanchero los detalles del viaje recién programado; si falla, solo registra el error.
     */
    @Override
    public void notificarViajeProgramado(ViajeProgramadoEvent viaje) {
        String fechaHoraInicio = formatearFechaHoraViaje(viaje.fechaHoraInicio());
        String asunto = "¡Tienes un nuevo viaje programado en NautiFlow!";
        String texto = """
                Hola, %s:

                ¡Tenemos buenas noticias! Se ha programado un nuevo viaje para ti.

                Origen: %s
                Destino: %s
                Fecha y hora de inicio: %s
                Cantidad de pasajeros: %s
                Nombre del turista: %s
                WhatsApp del turista: %s

                Te invitamos a revisar los detalles de tu viaje y estar preparado para brindar una excelente experiencia.

                ¡Gracias por formar parte de NautiFlow!

                El equipo de NautiFlow
                """.formatted(viaje.nombreLanchero(), viaje.zonaOrigen(), viaje.zonaDestino(),
                fechaHoraInicio, viaje.cantidadPasajeros(), viaje.turistaNombreCompleto(),
                viaje.turistaWhatsapp());
        String html = """
                <p>Hola, <strong>%s</strong>:</p>
                <p>¡Tenemos buenas noticias! Se ha programado un nuevo viaje para ti.</p>
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0"
                       style="width:100%%;border-collapse:collapse">
                  <tr><td style="padding:10px;border-bottom:1px solid #e2e8f0"><strong>Origen</strong></td><td style="padding:10px;border-bottom:1px solid #e2e8f0">%s</td></tr>
                  <tr><td style="padding:10px;border-bottom:1px solid #e2e8f0"><strong>Destino</strong></td><td style="padding:10px;border-bottom:1px solid #e2e8f0">%s</td></tr>
                  <tr><td style="padding:10px;border-bottom:1px solid #e2e8f0"><strong>Fecha y hora de inicio</strong></td><td style="padding:10px;border-bottom:1px solid #e2e8f0">%s</td></tr>
                  <tr><td style="padding:10px;border-bottom:1px solid #e2e8f0"><strong>Pasajeros</strong></td><td style="padding:10px;border-bottom:1px solid #e2e8f0">%s</td></tr>
                  <tr><td style="padding:10px;border-bottom:1px solid #e2e8f0"><strong>Turista</strong></td><td style="padding:10px;border-bottom:1px solid #e2e8f0">%s</td></tr>
                  <tr><td style="padding:10px"><strong>WhatsApp</strong></td><td style="padding:10px">%s</td></tr>
                </table>
                <p style="margin-top:24px">Te invitamos a revisar los detalles de tu viaje y estar preparado para brindar una excelente experiencia.</p>
                <p>¡Gracias por formar parte de NautiFlow!</p>
                """.formatted(
                CorreoHtmlTemplate.escapar(viaje.nombreLanchero()),
                CorreoHtmlTemplate.escapar(viaje.zonaOrigen()),
                CorreoHtmlTemplate.escapar(viaje.zonaDestino()),
                CorreoHtmlTemplate.escapar(fechaHoraInicio),
                CorreoHtmlTemplate.escapar(String.valueOf(viaje.cantidadPasajeros())),
                CorreoHtmlTemplate.escapar(viaje.turistaNombreCompleto()),
                CorreoHtmlTemplate.escapar(viaje.turistaWhatsapp())
        );

        try {
            resendEmailClient.enviar(new CorreoTransaccional(
                    viaje.correoLanchero(),
                    asunto,
                    texto,
                    CorreoHtmlTemplate.renderizar("¡Nuevo viaje programado!", "🚤", html)
            ));
            logger.info("Correo de nuevo viaje {} enviado al lanchero {}.",
                    viaje.idViaje(), viaje.idLanchero());
        } catch (ResendEmailException exception) {
            logger.error("No se pudo enviar el correo del viaje {}: categoría={}, HTTP {}.",
                    viaje.idViaje(), exception.getCategory(), exception.getHttpStatus());
        }
    }

    /**
     * Convierte la fecha del viaje a la zona horaria de Bogotá y la presenta en formato legible.
     */
    private String formatearFechaHoraViaje(OffsetDateTime fechaHoraInicio) {
        return FORMATO_FECHA_VIAJE.format(fechaHoraInicio.atZoneSameInstant(ZONA_NEGOCIO))
                .toLowerCase(Locale.ROOT);
    }

    /**
     * Envía una notificación; si Resend falla, lanza un 503 para que la operación no se confirme.
     */
    private void enviarNotificacionEstrictaHtml(
            Long idLanchero,
            String correo,
            String asunto,
            String emoji,
            String titulo,
            String textoPlano,
            String contenidoHtml
    ) {
        try {
            resendEmailClient.enviar(new CorreoTransaccional(
                    correo,
                    asunto,
                    textoPlano,
                    CorreoHtmlTemplate.renderizar(titulo, emoji, contenidoHtml)
            ));
            logger.info("Notificación enviada para lanchero {}.", idLanchero);
        } catch (ResendEmailException exception) {
            logger.error("No se pudo enviar la notificación para lanchero {}: categoría={}, HTTP {}.",
                    idLanchero, exception.getCategory(), exception.getHttpStatus());
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo enviar la notificación. La operación no fue confirmada.",
                    exception
            );
        }
    }
}
