package com.nautiflow.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

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

    public Long getIdTramo() { return idTramo; }
    public void setIdTramo(Long idTramo) { this.idTramo = idTramo; }

    public Viaje getViaje() { return viaje; }
    public void setViaje(Viaje viaje) { this.viaje = viaje; }

    public Zona getZonaOrigen() { return zonaOrigen; }
    public void setZonaOrigen(Zona zonaOrigen) { this.zonaOrigen = zonaOrigen; }

    public Zona getZonaDestino() { return zonaDestino; }
    public void setZonaDestino(Zona zonaDestino) { this.zonaDestino = zonaDestino; }

    public OffsetDateTime getFechaHoraRetorno() { return fechaHoraRetorno; }
    public void setFechaHoraRetorno(OffsetDateTime fechaHoraRetorno) {
        this.fechaHoraRetorno = fechaHoraRetorno;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
