package com.nautiflow.service;

/**
 * Contrato de las notificaciones por correo que usa la aplicación. Permite cambiar el proveedor sin tocar los servicios de negocio.
 */
public interface RegistroNotificacionService {
    /**
     * Construye el JSON del correo de confirmación de registro.
     */
    String prepararPayloadConfirmacion(String correo);

    /**
     * Envía el correo de confirmación usando el payload y la clave de idempotencia indicados.
     */
    void enviarConfirmacionPorCorreo(Long idLanchero, String payloadJson, String claveIdempotencia);

    /**
     * Notifica al lanchero que su solicitud fue aprobada.
     */
    void enviarDecisionAprobada(Long idLanchero, String nombreCompleto, String correo);

    /**
     * Notifica al lanchero que su solicitud fue rechazada e incluye el motivo.
     */
    void enviarDecisionRechazada(Long idLanchero, String nombreCompleto, String correo, String motivoPersistido);

    /**
     * Avisa al lanchero que recibió una nueva reseña.
     */
    void notificarResena(
            Long idLanchero,
            String correo,
            String nombreTurista,
            int estrellas,
            String zonaOrigen,
            String zonaDestino,
            String comentario,
            Double promedio
    );

    /**
     * Avisa al lanchero que se le asignó un nuevo viaje.
     */
    void notificarViajeProgramado(ViajeProgramadoEvent viaje);
}
