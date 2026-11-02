package com.nautiflow.model;

import jakarta.persistence.*;
import com.nautiflow.time.FechaHoraUtc;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lanchero")
public class Lanchero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lanchero")
    private Long idLanchero;

    @Column(name = "nombre_completo", nullable = false, length = 201)
    private String nombreCompleto;

    @Column(nullable = false, length = 20)
    private String whatsapp;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "estado_solicitud", nullable = false, length = 20)
    private String estadoSolicitud = "PENDIENTE";

    @Column(name = "fecha_registro", nullable = false)
    private OffsetDateTime fechaRegistro = FechaHoraUtc.ahora();

    @Column(name = "fecha_decision")
    private OffsetDateTime fechaDecision;

    @Column(name = "motivo_rechazo", length = 500)
    private String motivoRechazo;

    @Column(name = "estado_viaje", nullable = false, length = 20)
    private String estadoViaje = "INACTIVO";

    @OneToOne(mappedBy = "lanchero", fetch = FetchType.LAZY)
    private Lancha lancha;

    @OneToMany(mappedBy = "lanchero")
    private List<Documento> documentos = new ArrayList<>();

    public Lanchero() {}

    // Getters y Setters
    public Long getIdLanchero() { return idLanchero; }
    public void setIdLanchero(Long idLanchero) { this.idLanchero = idLanchero; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getEstadoSolicitud() { return estadoSolicitud; }
    public void setEstadoSolicitud(String estadoSolicitud) { this.estadoSolicitud = estadoSolicitud; }

    public OffsetDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(OffsetDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public OffsetDateTime getFechaDecision() { return fechaDecision; }
    public void setFechaDecision(OffsetDateTime fechaDecision) { this.fechaDecision = fechaDecision; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public String getEstadoViaje() { return estadoViaje; }
    public void setEstadoViaje(String estadoViaje) { this.estadoViaje = estadoViaje; }

    public Lancha getLancha() { return lancha; }
    public void setLancha(Lancha lancha) { this.lancha = lancha; }

    public List<Documento> getDocumentos() { return documentos; }
    public void setDocumentos(List<Documento> documentos) { this.documentos = documentos; }

}
