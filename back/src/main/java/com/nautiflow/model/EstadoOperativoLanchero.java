package com.nautiflow.model;

/**
 * Estados operativos de un lanchero: ACTIVO puede recibir viajes, INACTIVO no puede, EN_VIAJE está ocupado
 * y EN_RETORNO tiene un retorno vacío comprometido.
 */
public enum EstadoOperativoLanchero {
    ACTIVO,
    INACTIVO,
    EN_VIAJE,
    EN_RETORNO
}
