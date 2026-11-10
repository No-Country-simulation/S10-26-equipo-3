package com.nautiflow.model;

import com.nautiflow.util.ZonaNegocio;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
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

    @Column(name = "turista_nombre_completo", length = 201)
    private String turistaNombreCompleto;

    @Column(name = "turista_whatsapp", length = 20)
    private String turistaWhatsapp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTramoVacio estado = EstadoTramoVacio.ACTIVO;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

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

    public Zona getZonaOrigen() { return zonaOrigen; }
    public void setZonaOrigen(Zona zonaOrigen) { this.zonaOrigen = zonaOrigen; }

    public Zona getZonaDestino() { return zonaDestino; }
    public void setZonaDestino(Zona zonaDestino) { this.zonaDestino = zonaDestino; }

    public OffsetDateTime getFechaHoraRetorno() { return fechaHoraRetorno; }
    public void setFechaHoraRetorno(OffsetDateTime fechaHoraRetorno) {
        this.fechaHoraRetorno = fechaHoraRetorno;
    }

    public LocalDate getFecha() {
        return fechaHoraRetorno.atZoneSameInstant(ZonaNegocio.ZONA).toLocalDate();
    }

    public LocalTime getHoraRetorno() {
        return fechaHoraRetorno.atZoneSameInstant(ZonaNegocio.ZONA).toLocalTime();
    }

    public String getTuristaNombreCompleto() { return turistaNombreCompleto; }
    public void setTuristaNombreCompleto(String turistaNombreCompleto) {
        this.turistaNombreCompleto = turistaNombreCompleto;
    }

    public String getTuristaWhatsapp() { return turistaWhatsapp; }
    public void setTuristaWhatsapp(String turistaWhatsapp) { this.turistaWhatsapp = turistaWhatsapp; }

    /**
     * Obtiene estado actual del registro.
     */
    public EstadoTramoVacio getEstado() { return estado; }
    /**
     * Establece estado actual del registro.
     */
    public void setEstado(EstadoTramoVacio estado) { this.estado = estado; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
