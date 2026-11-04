package com.nautiflow.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa un viaje realizado por una lancha entre dos zonas, con los datos del turista y el token para dejar reseña.
 */
@Entity
@Table(name = "viaje")
public class Viaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_viaje")
    private Long idViaje;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_lancha", nullable = false)
    private Lancha lancha;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_zona_origen", nullable = false)
    private Zona zonaOrigen;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_zona_destino", nullable = false)
    private Zona zonaDestino;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private OffsetDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private OffsetDateTime fechaHoraFin;

    @Column(name = "cantidad_pasajeros", nullable = false)
    private Integer cantidadPasajeros;

    @Column(name = "turista_nombre_completo", nullable = false, length = 201)
    private String turistaNombreCompleto;

    @Column(name = "turista_whatsapp", nullable = false, length = 20)
    private String turistaWhatsapp;

    @Column(name = "token_resena", unique = true, length = 64)
    private String tokenResena;

    @OneToOne(mappedBy = "viaje", fetch = FetchType.LAZY)
    private Resena resena;

    @OneToMany(mappedBy = "viaje")
    private Set<TrayectoRetornoVacio> trayectosRetorno = new HashSet<>();

    /**
     * Obtiene identificador del viaje.
     */
    public Long getIdViaje() { return idViaje; }
    /**
     * Establece identificador del viaje.
     */
    public void setIdViaje(Long idViaje) { this.idViaje = idViaje; }

    /**
     * Obtiene embarcación asociada.
     */
    public Lancha getLancha() { return lancha; }
    /**
     * Establece embarcación asociada.
     */
    public void setLancha(Lancha lancha) { this.lancha = lancha; }

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
     * Obtiene fecha y hora de inicio del viaje.
     */
    public OffsetDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    /**
     * Establece fecha y hora de inicio del viaje.
     */
    public void setFechaHoraInicio(OffsetDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }

    /**
     * Obtiene fecha y hora de finalización del viaje.
     */
    public OffsetDateTime getFechaHoraFin() { return fechaHoraFin; }
    /**
     * Establece fecha y hora de finalización del viaje.
     */
    public void setFechaHoraFin(OffsetDateTime fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }

    /**
     * Obtiene cantidad de pasajeros del viaje.
     */
    public Integer getCantidadPasajeros() { return cantidadPasajeros; }
    /**
     * Establece cantidad de pasajeros del viaje.
     */
    public void setCantidadPasajeros(Integer cantidadPasajeros) { this.cantidadPasajeros = cantidadPasajeros; }

    /**
     * Obtiene nombre completo del turista.
     */
    public String getTuristaNombreCompleto() { return turistaNombreCompleto; }
    /**
     * Establece nombre completo del turista.
     */
    public void setTuristaNombreCompleto(String turistaNombreCompleto) {
        this.turistaNombreCompleto = turistaNombreCompleto;
    }

    /**
     * Obtiene número de WhatsApp del turista.
     */
    public String getTuristaWhatsapp() { return turistaWhatsapp; }
    /**
     * Establece número de WhatsApp del turista.
     */
    public void setTuristaWhatsapp(String turistaWhatsapp) { this.turistaWhatsapp = turistaWhatsapp; }

    /**
     * Obtiene token único para registrar la reseña del viaje.
     */
    public String getTokenResena() { return tokenResena; }
    /**
     * Establece token único para registrar la reseña del viaje.
     */
    public void setTokenResena(String tokenResena) { this.tokenResena = tokenResena; }

    /**
     * Obtiene datos de la reseña registrada, o null si aún no existe.
     */
    public Resena getResena() { return resena; }
    /**
     * Establece datos de la reseña registrada, o null si aún no existe.
     */
    public void setResena(Resena resena) { this.resena = resena; }

    /**
     * Obtiene trayectos de retorno asociados al viaje.
     */
    public Set<TrayectoRetornoVacio> getTrayectosRetorno() { return trayectosRetorno; }
    /**
     * Establece trayectos de retorno asociados al viaje.
     */
    public void setTrayectosRetorno(Set<TrayectoRetornoVacio> trayectosRetorno) {
        this.trayectosRetorno = trayectosRetorno;
    }
}
