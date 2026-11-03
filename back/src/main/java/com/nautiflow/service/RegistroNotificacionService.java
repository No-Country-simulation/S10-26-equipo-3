package com.nautiflow.service;

public interface RegistroNotificacionService {
    String prepararPayloadConfirmacion(String correo);

    void enviarConfirmacionPorCorreo(Long idLanchero, String payloadJson, String claveIdempotencia);

    void enviarDecisionAprobada(Long idLanchero, String nombreCompleto, String correo);

    void enviarDecisionRechazada(Long idLanchero, String nombreCompleto, String correo, String motivoPersistido);

    void notificarResena(
            Long idLanchero,
            String correo,
            String nombreTurista,
            int estrellas,
            String zonaOrigen,
            String zonaDestino,
            String comentario
    );

    void notificarViajeProgramado(ViajeProgramadoEvent viaje);
}
