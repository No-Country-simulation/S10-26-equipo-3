package com.nautiflow.model;

import jakarta.persistence.*;
import com.nautiflow.util.FechaHoraUtc;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa a un lanchero registrado, su estado de solicitud, su estado operativo, su lancha y sus documentos.
 */
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

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud", nullable = false, length = 20)
    private EstadoSolicitud estadoSolicitud = EstadoSolicitud.PENDIENTE;

    @Column(name = "fecha_registro", nullable = false)
    private OffsetDateTime fechaRegistro = FechaHoraUtc.ahora();

    @Column(name = "fecha_decision")
    private OffsetDateTime fechaDecision;

    @Column(name = "motivo_rechazo", length = 500)
    private String motivoRechazo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_viaje", nullable = false, length = 20)
    private EstadoOperativoLanchero estadoViaje = EstadoOperativoLanchero.INACTIVO;

    @OneToOne(mappedBy = "lanchero", fetch = FetchType.LAZY)
    private Lancha lancha;

    @OneToMany(mappedBy = "lanchero")
    private List<Documento> documentos = new ArrayList<>();

    /**
     * Crea un lanchero vacío. JPA usa este constructor al reconstruir entidades.
     */
    public Lanchero() {
    }

    // Getters y Setters
    /**
     * Obtiene identificador del lanchero.
     */
    public Long getIdLanchero() {
        return idLanchero;
    }

    /**
     * Establece identificador del lanchero.
     */
    public void setIdLanchero(Long idLanchero) {
        this.idLanchero = idLanchero;
    }

    /**
     * Obtiene nombre completo de la persona.
     */
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    /**
     * Establece nombre completo de la persona.
     */
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    /**
     * Obtiene número de WhatsApp de contacto.
     */
    public String getWhatsapp() {
        return whatsapp;
    }

    /**
     * Establece número de WhatsApp de contacto.
     */
    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    /**
     * Obtiene correo electrónico de contacto.
     */
    public String getCorreo() {
        return correo;
    }

    /**
     * Establece correo electrónico de contacto.
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * Obtiene hash de la contraseña del lanchero.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Establece hash de la contraseña del lanchero.
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Obtiene estado de la solicitud de registro.
     */
    public EstadoSolicitud getEstadoSolicitud() {
        return estadoSolicitud;
    }

    /**
     * Establece estado de la solicitud de registro.
     */
    public void setEstadoSolicitud(EstadoSolicitud estadoSolicitud) {
        this.estadoSolicitud = estadoSolicitud;
    }

    /**
     * Obtiene fecha y hora en que se registró la solicitud.
     */
    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    /**
     * Establece fecha y hora en que se registró la solicitud.
     */
    public void setFechaRegistro(OffsetDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    /**
     * Obtiene fecha y hora en que se tomó la decisión.
     */
    public OffsetDateTime getFechaDecision() {
        return fechaDecision;
    }

    /**
     * Establece fecha y hora en que se tomó la decisión.
     */
    public void setFechaDecision(OffsetDateTime fechaDecision) {
        this.fechaDecision = fechaDecision;
    }

    /**
     * Obtiene motivo por el cual se rechazó la solicitud.
     */
    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    /**
     * Establece motivo por el cual se rechazó la solicitud.
     */
    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    /**
     * Obtiene estado operativo del lanchero.
     */
    public EstadoOperativoLanchero getEstadoViaje() {
        return estadoViaje;
    }

    /**
     * Establece estado operativo del lanchero.
     */
    public void setEstadoViaje(EstadoOperativoLanchero estadoViaje) {
        this.estadoViaje = estadoViaje;
    }

    /**
     * Obtiene embarcación asociada.
     */
    public Lancha getLancha() {
        return lancha;
    }

    /**
     * Establece embarcación asociada.
     */
    public void setLancha(Lancha lancha) {
        this.lancha = lancha;
    }

    /**
     * Obtiene documentos asociados a la solicitud.
     */
    public List<Documento> getDocumentos() {
        return documentos;
    }

    /**
     * Establece documentos asociados a la solicitud.
     */
    public void setDocumentos(List<Documento> documentos) {
        this.documentos = documentos;
    }

}
