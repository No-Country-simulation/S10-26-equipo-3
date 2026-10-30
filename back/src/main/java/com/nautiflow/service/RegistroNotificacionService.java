package com.nautiflow.service;

public interface RegistroNotificacionService {
    void enviarConfirmacionPorCorreo(Long idLanchero, String correo);
}
