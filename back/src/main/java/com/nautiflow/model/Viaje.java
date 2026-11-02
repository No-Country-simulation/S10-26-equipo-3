package com.nautiflow.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

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

    public Long getIdViaje() { return idViaje; }
    public void setIdViaje(Long idViaje) { this.idViaje = idViaje; }

    public Lancha getLancha() { return lancha; }
    public void setLancha(Lancha lancha) { this.lancha = lancha; }

    public Zona getZonaOrigen() { return zonaOrigen; }
    public void setZonaOrigen(Zona zonaOrigen) { this.zonaOrigen = zonaOrigen; }

    public Zona getZonaDestino() { return zonaDestino; }
    public void setZonaDestino(Zona zonaDestino) { this.zonaDestino = zonaDestino; }

    public OffsetDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(OffsetDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }

    public OffsetDateTime getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(OffsetDateTime fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }

    public Integer getCantidadPasajeros() { return cantidadPasajeros; }
    public void setCantidadPasajeros(Integer cantidadPasajeros) { this.cantidadPasajeros = cantidadPasajeros; }

    public String getTuristaNombreCompleto() { return turistaNombreCompleto; }
    public void setTuristaNombreCompleto(String turistaNombreCompleto) {
        this.turistaNombreCompleto = turistaNombreCompleto;
    }

    public String getTuristaWhatsapp() { return turistaWhatsapp; }
    public void setTuristaWhatsapp(String turistaWhatsapp) { this.turistaWhatsapp = turistaWhatsapp; }

    public String getTokenResena() { return tokenResena; }
    public void setTokenResena(String tokenResena) { this.tokenResena = tokenResena; }

    public Resena getResena() { return resena; }
    public void setResena(Resena resena) { this.resena = resena; }

    public Set<TrayectoRetornoVacio> getTrayectosRetorno() { return trayectosRetorno; }
    public void setTrayectosRetorno(Set<TrayectoRetornoVacio> trayectosRetorno) {
        this.trayectosRetorno = trayectosRetorno;
    }
}
