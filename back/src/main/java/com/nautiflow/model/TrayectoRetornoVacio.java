package com.nautiflow.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Entidad que representa un trayecto de retorno sin pasajeros, usado para bloquear la disponibilidad de una lancha.
 */
@Entity
@Table(name = "trayecto_retorno_vacio")
public class TrayectoRetornoVacio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tramo")
    private Long idTramo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_viaje", nullable = false)
    private Viaje viaje;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_zona_origen", nullable = false)
    private Zona zonaOrigen;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_zona_destino", nullable = false)
    private Zona zonaDestino;

    @Column(name = "fecha_hora_retorno", nullable = false)
    private OffsetDateTime fechaHoraRetorno;

    @Column(nullable = false, length = 20)
    private String estado = "ACTIVO";

    /**
     * Obtiene identificador del trayecto.
     */
    public Long getIdTramo() { return idTramo; }
    /**
     * Establece identificador del trayecto.
     */
    public void setIdTramo(Long idTramo) { this.idTramo = idTramo; }

    /**
     * Obtiene viaje asociado.
     */
    public Viaje getViaje() { return viaje; }
    /**
     * Establece viaje asociado.
     */
    public void setViaje(Viaje viaje) { this.viaje = viaje; }

    /**
     * Obtiene nombre de la zona de origen.
     */
    public Zona getZonaOrigen() { return zonaOrigen; }
    /**
     * Establece nombre de la zona de origen.
     */
    public void setZonaOrigen(Zona zonaOrigen) { this.zonaOrigen = zonaOrigen; }

    /**
     * Obtiene nombre de la zona de destino.
     */
    public Zona getZonaDestino() { return zonaDestino; }
    /**
     * Establece nombre de la zona de destino.
     */
    public void setZonaDestino(Zona zonaDestino) { this.zonaDestino = zonaDestino; }

    /**
     * Obtiene fecha y hora prevista para el retorno.
     */
    public OffsetDateTime getFechaHoraRetorno() { return fechaHoraRetorno; }
    /**
     * Establece fecha y hora prevista para el retorno.
     */
    public void setFechaHoraRetorno(OffsetDateTime fechaHoraRetorno) {
        this.fechaHoraRetorno = fechaHoraRetorno;
    }

    /**
     * Obtiene estado actual del registro.
     */
    public String getEstado() { return estado; }
    /**
     * Establece estado actual del registro.
     */
    public void setEstado(String estado) { this.estado = estado; }
}
