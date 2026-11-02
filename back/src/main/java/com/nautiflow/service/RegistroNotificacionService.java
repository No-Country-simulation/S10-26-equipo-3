package com.nautiflow.service;

public interface RegistroNotificacionService {
    void enviarConfirmacionPorCorreo(Long idLanchero, String correo);
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
